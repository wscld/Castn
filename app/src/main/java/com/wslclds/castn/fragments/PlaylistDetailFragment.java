package com.wslclds.castn.fragments;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.google.gson.Gson;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.IAdapter;
import com.mikepenz.fastadapter.IItem;
import com.mikepenz.fastadapter.adapters.ItemAdapter;
import com.mikepenz.fastadapter.listeners.OnClickListener;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Arrays;

import butterknife.BindView;
import butterknife.ButterKnife;
import com.wslclds.castn.activities.EpisodeDetailActivity;
import com.wslclds.castn.activities.MainActivity;
import com.wslclds.castn.extensions.drag.DragDropUtil;
import com.wslclds.castn.extensions.drag.ItemTouchCallback;
import com.wslclds.castn.extensions.drag.SimpleDragCallback;
import com.wslclds.castn.factory.DatabaseManager;
import com.wslclds.castn.factory.objects.Episode;
import com.wslclds.castn.factory.objects.Playlist;
import com.wslclds.castn.factory.objects.PlaylistEpisode;
import com.wslclds.castn.factory.objects.ToolbarEvent;
import com.wslclds.castn.builders.AlertWithInputBuilder;
import com.wslclds.castn.builders.PopUpMenuBuilder;
import com.wslclds.castn.items.PlaylistEpisodeItem;
import com.wslclds.castn.R;
import me.yokeyword.fragmentation.SupportFragment;

/**
 * A simple {@link Fragment} subclass.
 */
public class PlaylistDetailFragment extends SupportFragment {

    Playlist playlist;
    DatabaseManager databaseManager;
    ItemAdapter itemAdapter;
    FastAdapter fastAdapter;
    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    @BindView(R.id.playButton)
    Button playButton;
    @BindView(R.id.optionsButton)
    ImageButton optionsButton;
    @BindView(R.id.header)
    LinearLayout header;

    int REMOVE_REQUEST_CODE = 3;

    public static PlaylistDetailFragment newInstance(String playlistId) {

        Bundle args = new Bundle();
        args.putString("playlistId",playlistId);
        PlaylistDetailFragment fragment = new PlaylistDetailFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public PlaylistDetailFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_playlist_detail, container, false);
        ButterKnife.bind(this,view);
        return view;
    }

    @Override
    public void onLazyInitView(@Nullable Bundle savedInstanceState) {
        super.onLazyInitView(savedInstanceState);
        this.databaseManager = new DatabaseManager(getContext());
        this.playlist = databaseManager.getPlaylist(getArguments().getString("playlistId"));
        sendToolbarStatus();

        recyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));
        itemAdapter = new ItemAdapter();
        fastAdapter = FastAdapter.with(itemAdapter);
        recyclerView.setAdapter(fastAdapter);
        ViewCompat.setNestedScrollingEnabled(recyclerView,false);

        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MainActivity)getContext()).playAudio(playlist.getId());
            }
        });

        fastAdapter.withOnClickListener(new OnClickListener() {
            @Override
            public boolean onClick(@javax.annotation.Nullable View v, IAdapter adapter, IItem item, int position) {
                PlaylistEpisodeItem playlistEpisodeItem = (PlaylistEpisodeItem)item;
                Episode episode = new Gson().fromJson(playlistEpisodeItem.getPlaylistEpisode().getEpisodeJson(),Episode.class);
                Intent intent = new Intent(getContext(),EpisodeDetailActivity.class);
                intent.putExtra("episode",new Gson().toJson(episode));
                intent.putExtra("playlistId",playlist.getId());
                startActivityForResult(intent,REMOVE_REQUEST_CODE);
                return true;
            }
        });

        SimpleDragCallback touchCallback = new SimpleDragCallback(new ItemTouchCallback() {
            @Override
            public boolean itemTouchOnMove(int oldPosition, int newPosition) {
                DragDropUtil.onMove(itemAdapter, oldPosition, newPosition);
                return true;
            }

            @Override
            public void itemTouchDropped(int oldPosition, int newPosition) {

            }
        });

        optionsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopUpMenuBuilder popUpMenuBuilder = new PopUpMenuBuilder(getContext(), view, new ArrayList<>(Arrays.asList("Edit title")), new PopUpMenuBuilder.WithOnClickListener() {
                    @Override
                    public void onClickListener(int position) {
                        AlertWithInputBuilder alertWithInputBuilder = new AlertWithInputBuilder(getContext(), "Edit title", null,"Title",playlist.getName(), new AlertWithInputBuilder.OnAction() {
                            @Override
                            public void OnSubmit(String string) {
                                if(string != null && string.length() > 0){
                                    playlist.setName(string);
                                    sendToolbarStatus();
                                    databaseManager.editPlaylist(playlist.getId(),string,playlist.isPublicAvailable());
                                }
                            }
                        });
                        alertWithInputBuilder.show();
                    }
                });
                popUpMenuBuilder.showMenu();
            }
        });

        ItemTouchHelper touchHelper = new ItemTouchHelper(touchCallback);
        touchHelper.attachToRecyclerView(recyclerView);
        loadEpisodes();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REMOVE_REQUEST_CODE && resultCode == RESULT_OK){
            itemAdapter.clear();
            loadEpisodes();
        }
    }

    private void loadEpisodes(){
        @SuppressLint("StaticFieldLeak") AsyncTask asyncTask = new AsyncTask() {
            ArrayList<PlaylistEpisodeItem> items;
            @Override
            protected Object doInBackground(Object[] objects) {
                items = new ArrayList<>();
                ArrayList<PlaylistEpisode> episodes = databaseManager.getRawPlaylistEpisodes(playlist.getId());
                for(PlaylistEpisode playlistEpisode : episodes){
                    PlaylistEpisodeItem playlistEpisodeItem = new PlaylistEpisodeItem(playlistEpisode);
                    items.add(playlistEpisodeItem);
                }
                return null;
            }

            @Override
            protected void onPostExecute(Object o) {
                if(itemAdapter != null){
                    itemAdapter.add(items);
                }
            }
        };
        asyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    public void onDestroy() {
        if(itemAdapter != null){
            for(int i = 0; i < itemAdapter.getAdapterItems().size(); i++){
                PlaylistEpisodeItem playlistEpisodeItem = (PlaylistEpisodeItem) itemAdapter.getAdapterItem(i);
                databaseManager.updatePlaylistEpisodePosition(playlistEpisodeItem.getPlaylistEpisode().getId(),i);
            }
        }
        super.onDestroy();
    }

    private void sendToolbarStatus(){
        ToolbarEvent toolbarEvent = new ToolbarEvent();
        if(playlist != null){
            toolbarEvent.setTitle(playlist.getName());
        }
        toolbarEvent.setHome(false);
        EventBus.getDefault().post(toolbarEvent);
    }

    @Override
    public void onSupportVisible() {
        super.onSupportVisible();
        sendToolbarStatus();
    }
}

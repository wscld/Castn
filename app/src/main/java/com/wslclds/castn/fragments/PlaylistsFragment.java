package com.wslclds.castn.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.IAdapter;
import com.mikepenz.fastadapter.IItem;
import com.mikepenz.fastadapter.adapters.ItemAdapter;
import com.mikepenz.fastadapter.listeners.OnClickListener;
import com.mikepenz.fastadapter.listeners.OnLongClickListener;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

import com.wslclds.castn.builders.AlertBuilder;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.wslclds.castn.factory.DatabaseManager;
import com.wslclds.castn.factory.objects.Playlist;
import com.wslclds.castn.factory.objects.ToolbarEvent;
import com.wslclds.castn.builders.AlertWithInputBuilder;
import com.wslclds.castn.helpers.Helper;
import com.wslclds.castn.items.PlaylistItem;
import com.wslclds.castn.R;
import me.yokeyword.fragmentation.SupportFragment;


public class PlaylistsFragment extends SupportFragment {


    private ItemAdapter itemAdapter;
    private FastAdapter fastAdapter;
    private DatabaseManager databaseManager;
    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    @BindView(R.id.fab)
    FloatingActionButton fab;

    public PlaylistsFragment() {
        // Required empty public constructor
    }

    public static PlaylistsFragment newInstance() {
        Bundle args = new Bundle();
        PlaylistsFragment fragment = new PlaylistsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_playlists, container, false);
        ButterKnife.bind(this,view);
        return view;
    }

    @Override
    public void onLazyInitView(@Nullable Bundle savedInstanceState) {
        super.onLazyInitView(savedInstanceState);
        databaseManager = new DatabaseManager(getContext());
        recyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));
        itemAdapter = new ItemAdapter();
        fastAdapter = FastAdapter.with(itemAdapter);
        recyclerView.setAdapter(fastAdapter);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertWithInputBuilder alertWithInputBuilder = new AlertWithInputBuilder(getContext(), "Create new playlist", null,"Playlist name",null, new AlertWithInputBuilder.OnAction() {
                    @Override
                    public void OnSubmit(String string) {
                        Playlist playlist = Helper.createPlaylistObject(string,true);
                        databaseManager.createPlaylist(playlist);
                        loadPlaylists();
                    }
                });
                alertWithInputBuilder.show();
            }
        });

        fastAdapter.withOnClickListener(new OnClickListener() {
            @Override
            public boolean onClick(@javax.annotation.Nullable View v, IAdapter adapter, IItem item, int position) {
                PlaylistItem playlistItem = (PlaylistItem) item;
                start(PlaylistDetailFragment.newInstance(playlistItem.getPlaylist().getId()));
                return true;
            }
        });

        fastAdapter.withOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v, IAdapter adapter, IItem item, int position) {
                PlaylistItem playlistItem = (PlaylistItem)item;
                new AlertBuilder(getContext(), "Delete " + playlistItem.getPlaylist().getName(), "Are you sure you want to delete this playlist?", new AlertBuilder.onButtonClick2() {

                    @Override
                    public void onConfirm() {
                        itemAdapter.remove(position);
                        databaseManager.deletePlaylist(playlistItem.getPlaylist().getId());
                        Toast.makeText(getContext(),"Playlist deleted",Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onCancel() {

                    }
                }).show();
                return true;
            }
        });

        loadPlaylists();
    }

    private void loadPlaylists(){
        itemAdapter.clear();
        ArrayList<Playlist> playlists = databaseManager.getPlaylists();
        for(Playlist playlist : playlists){
            PlaylistItem playlistItem = new PlaylistItem(playlist);
            itemAdapter.add(playlistItem);
        }
    }

    @Override
    public void onSupportVisible() {
        super.onSupportVisible();
        ToolbarEvent toolbarEvent = new ToolbarEvent();
        toolbarEvent.setTitle("Playlists");
        toolbarEvent.setHome(false);
        EventBus.getDefault().post(toolbarEvent);
    }
}

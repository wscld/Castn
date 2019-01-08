package com.wslclds.castn.activities;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.gson.Gson;
import com.liuguangqiang.swipeback.SwipeBackLayout;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.IAdapter;
import com.mikepenz.fastadapter.IItem;
import com.mikepenz.fastadapter.adapters.ItemAdapter;
import com.mikepenz.fastadapter.listeners.OnClickListener;

import java.util.ArrayList;

import javax.annotation.Nullable;

import com.wslclds.castn.helpers.ThemeHelper;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.wslclds.castn.extensions.drag.DragDropUtil;
import com.wslclds.castn.extensions.drag.ItemTouchCallback;
import com.wslclds.castn.extensions.drag.SimpleDragCallback;
import com.wslclds.castn.factory.DatabaseManager;
import com.wslclds.castn.factory.objects.Episode;
import com.wslclds.castn.factory.objects.PlaylistEpisode;
import com.wslclds.castn.helpers.Helper;
import com.wslclds.castn.items.PlaylistEpisodeItem;
import com.wslclds.castn.R;
import com.wslclds.castn.services.AudioPlayerService;

public class EpisodeQueueActivity extends AppCompatActivity implements SwipeBackLayout.SwipeBackListener {

    private DatabaseManager databaseManager;
    private ItemAdapter itemAdapter;
    ThemeHelper themeHelper;
    private String playlistId;
    private AudioPlayerService audioPlayerService;
    boolean isBound = false;
    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    @BindView(R.id.clear)
    Button clear;
    @BindView(R.id.swipeBackLayout)
    SwipeBackLayout swipeBackLayout;
    @BindView(R.id.playlistTitle)
    TextView playlistTitle;
    @BindView(R.id.nestedScrollView)
    NestedScrollView nestedScrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        themeHelper = new ThemeHelper(this);
        themeHelper.apply(false);
        setContentView(R.layout.activity_playlist);
        ButterKnife.bind(this);
        setDragEdge(SwipeBackLayout.DragEdge.TOP);

        nestedScrollView.setBackgroundResource(themeHelper.getThemeColor(R.color.background));

        playlistId = getIntent().getStringExtra("playlistId");
        databaseManager = new DatabaseManager(this);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        itemAdapter = new ItemAdapter();
        FastAdapter fastAdapter = FastAdapter.with(itemAdapter);
        recyclerView.setAdapter(fastAdapter);
        ViewCompat.setNestedScrollingEnabled(recyclerView,false);


        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                databaseManager.clearPlaylist(playlistId);
                loadPlaylist();
            }
        });

        if(!playlistId.equals(Helper.DEFAULT_PLAYLIST_ID)){
            clear.setVisibility(View.GONE);
            playlistTitle.setText(databaseManager.getPlaylist(playlistId).getName());
        }else {
            clear.setVisibility(View.VISIBLE);
            playlistTitle.setText("Coming next...");
        }

        fastAdapter.withOnClickListener(new OnClickListener() {
            @Override
            public boolean onClick(@Nullable View v, IAdapter adapter, IItem item, int position) {
                @SuppressLint("StaticFieldLeak") AsyncTask asyncTask = new AsyncTask() {
                    Episode episode;
                    @Override
                    protected Object doInBackground(Object[] objects) {
                        PlaylistEpisodeItem playlistEpisodeItem = (PlaylistEpisodeItem)item;
                        episode = new Gson().fromJson(playlistEpisodeItem.getPlaylistEpisode().getEpisodeJson(),Episode.class);
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Object o) {
                        if(episode != null && position != 0){
                            playAudio(episode);
                            finish();
                        }

                    }
                };
                asyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                return false;
            }
        });

        SimpleDragCallback touchCallback = new SimpleDragCallback(new ItemTouchCallback() {
            @Override
            public boolean itemTouchOnMove(int oldPosition, int newPosition) {
                if(newPosition == 0 || oldPosition == 0) {
                    return false;
                }else {
                    DragDropUtil.onMove(itemAdapter, oldPosition, newPosition);
                    for(int i = 0; i < itemAdapter.getAdapterItems().size(); i++){
                        PlaylistEpisodeItem playlistEpisodeItem = (PlaylistEpisodeItem) itemAdapter.getAdapterItem(i);
                        databaseManager.updatePlaylistEpisodePosition(playlistEpisodeItem.getPlaylistEpisode().getId(),i);
                    }
                    return true;
                }
            }

            @Override
            public void itemTouchDropped(int oldPosition, int newPosition) {

            }
        });
        ItemTouchHelper touchHelper = new ItemTouchHelper(touchCallback);
        touchHelper.attachToRecyclerView(recyclerView);

        loadPlaylist();
    }


    private void loadPlaylist(){
        itemAdapter.clear();
        databaseManager.cleanUpQueue();
        ArrayList<PlaylistEpisode> playlistEpisodes = databaseManager.getRawPlaylistEpisodes(playlistId);
        for(PlaylistEpisode playlistEpisode : playlistEpisodes){
            PlaylistEpisodeItem playlistEpisodeItem = new PlaylistEpisodeItem(playlistEpisode);
            itemAdapter.add(playlistEpisodeItem);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        makeBind();
    }

    @Override
    public void onStop() {
        super.onStop();
        removeBind();
    }

    //player binding
    private void makeBind(){
        Intent intent = new Intent(this , AudioPlayerService.class);
        startService(intent);
        bindService(intent , boundServiceConnection,BIND_AUTO_CREATE);
    }
    private void removeBind(){
        if(isBound){
            unbindService(boundServiceConnection);
            isBound = false;
        }
    }
    private ServiceConnection boundServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {

            AudioPlayerService.AudioPlayerBinder binderBridge = (AudioPlayerService.AudioPlayerBinder) service ;
            audioPlayerService = binderBridge.getService();
            isBound = true;

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBound = false;
            audioPlayerService = null;
        }
    };

    //player logic
    public void playAudio(Episode episode){
        if(audioPlayerService != null){
            databaseManager.addEpisodeToPlaylist(episode,playlistId);
            audioPlayerService.setAndPlay(playlistId);
        }
    }



    public void setDragEdge(SwipeBackLayout.DragEdge dragEdge) {
        swipeBackLayout.setDragEdge(dragEdge);
    }

    @Override
    public void onViewPositionChanged(float fractionAnchor, float fractionScreen) {

    }
}

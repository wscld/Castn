package com.wslclds.castn.fragments;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.gson.Gson;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.IAdapter;
import com.mikepenz.fastadapter.IItem;
import com.mikepenz.fastadapter.adapters.ItemAdapter;
import com.mikepenz.fastadapter.listeners.OnClickListener;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;

import com.wslclds.castn.activities.EpisodeDetailActivity;
import com.wslclds.castn.activities.MainActivity;
import com.wslclds.castn.factory.objects.Time;
import com.wslclds.castn.helpers.Helper;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.wslclds.castn.factory.DatabaseManager;
import com.wslclds.castn.factory.objects.Download;
import com.wslclds.castn.factory.objects.Episode;
import com.wslclds.castn.factory.objects.ToolbarEvent;
import com.wslclds.castn.items.EpisodeItem;
import com.wslclds.castn.R;
import com.wslclds.castn.items.StorageStatsItem;

import me.yokeyword.fragmentation.SupportFragment;

/**
 * A simple {@link Fragment} subclass.
 */
public class EpisodeListFragment extends SupportFragment {

    public static final int TYPE_DOWNLOADED = 0;
    public static final int TYPE_EPISODE_SEARCH = 1;
    public static final int TYPE_UNFINISHED = 3;
    private int EPISODE_DETAIL_REQUEST_ID = 1;

    private int type;
    private ItemAdapter itemAdapter;
    private DatabaseManager databaseManager;
    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;

    public EpisodeListFragment() {
        // Required empty public constructor
    }

    public static EpisodeListFragment newInstance(int type, String url, String term) {
        Bundle args = new Bundle();
        args.putInt("type",type);
        args.putString("term",term);
        args.putString("url",url);
        EpisodeListFragment fragment = new EpisodeListFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public static EpisodeListFragment newInstance(int type, String term) {
        Bundle args = new Bundle();
        args.putInt("type",type);
        args.putString("term",term);
        EpisodeListFragment fragment = new EpisodeListFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public static EpisodeListFragment newInstance(int type) {
        Bundle args = new Bundle();
        args.putInt("type",type);
        EpisodeListFragment fragment = new EpisodeListFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_episode_list, container, false);
        ButterKnife.bind(this,view);
        type = getArguments().getInt("type",0);

        return view;
    }

    @Override
    public void onLazyInitView(@Nullable Bundle savedInstanceState) {
        super.onLazyInitView(savedInstanceState);

        databaseManager = new DatabaseManager(getContext());
        recyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));
        itemAdapter = new ItemAdapter();
        FastAdapter fastAdapter = FastAdapter.with(itemAdapter);
        recyclerView.setAdapter(fastAdapter);

        fastAdapter.withOnClickListener(new OnClickListener() {
            @Override
            public boolean onClick(@javax.annotation.Nullable View v, IAdapter adapter, IItem item, int position) {
                EpisodeItem episodeItem = (EpisodeItem)item;
                Episode episode = episodeItem.getEpisode();
                if(episode != null){
                    Intent intent = new Intent(getContext(),EpisodeDetailActivity.class);
                    intent.putExtra("episode",new Gson().toJson(episode));
                    startActivityForResult(intent,EPISODE_DETAIL_REQUEST_ID);
                }
                return true;
            }
        });

        if(type == TYPE_DOWNLOADED){
            loadDownloaded();
        }else if(type == TYPE_UNFINISHED){
            loadUnfinished();
        }else if(type == TYPE_EPISODE_SEARCH){
            String url = getArguments().getString("url");
            String term  = getArguments().getString("term");
            loadEpisodeSearch(url,term);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == EPISODE_DETAIL_REQUEST_ID && resultCode == RESULT_OK){
            ((MainActivity)getContext()).playAudio(Helper.DEFAULT_PLAYLIST_ID);
        }
    }

    private void loadDownloaded(){
        itemAdapter.add(new StorageStatsItem());
        ArrayList<Download> downloadeds = databaseManager.getDownloaded();
        ArrayList<EpisodeItem> episodesItems = new ArrayList<>();
        for(Download download : downloadeds){
            Episode episode = new Gson().fromJson(download.getEpisodeJson(),Episode.class);
            episodesItems.add(new EpisodeItem(episode));
        }
        itemAdapter.add(episodesItems);
    }

    private void loadUnfinished(){
        ArrayList<Time> unfinisheds = databaseManager.getUnfinishedEpisodes();
        ArrayList<EpisodeItem> episodesItems = new ArrayList<>();
        for(Time time : unfinisheds){
            Episode episode = databaseManager.getEpisode(time.getEnclosureUrl());
            if(episode != null){
                episodesItems.add(new EpisodeItem(episode,(int) time.getElapsedTime(),(int) time.getTotalTime()));
            }
        }
        itemAdapter.add(episodesItems);
    }

    private void loadEpisodeSearch(String url, String term){
        @SuppressLint("StaticFieldLeak") AsyncTask asyncTask = new AsyncTask() {
            ArrayList<EpisodeItem> episodeItems;
            @Override
            protected Object doInBackground(Object[] objects) {
                episodeItems = new ArrayList<>();
                ArrayList<Episode> episodes;
                DatabaseManager databaseManager = new DatabaseManager(getContext());
                if(url != null){
                    episodes = databaseManager.searchEpisodes(url,term);
                }else {
                    episodes = databaseManager.searchEpisodes(term);
                }
                for(Episode episode : episodes){
                    episodeItems.add(new EpisodeItem(episode));
                }
                return null;
            }

            @Override
            protected void onPostExecute(Object o) {
                super.onPostExecute(o);
                itemAdapter.add(episodeItems);
            }
        };
        asyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    public void onSupportVisible() {
        super.onSupportVisible();
        ToolbarEvent toolbarEvent = new ToolbarEvent();
        if(type == TYPE_DOWNLOADED){
            toolbarEvent.setTitle("Downloaded");
        }else if(type == TYPE_UNFINISHED){
            toolbarEvent.setTitle("In Progress");
        }else if(type == TYPE_EPISODE_SEARCH){
            toolbarEvent.setTitle(getArguments().getString("term"));
        }
        EventBus.getDefault().post(toolbarEvent);
    }
}

package com.wslclds.castn.fragments;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.gson.Gson;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.IItem;
import com.mikepenz.fastadapter.adapters.ItemAdapter;
import com.mikepenz.fastadapter.listeners.ClickEventHook;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.annotation.Nullable;

import butterknife.BindView;
import butterknife.ButterKnife;
import com.wslclds.castn.activities.EpisodeDetailActivity;
import com.wslclds.castn.activities.MainActivity;
import com.wslclds.castn.factory.DatabaseManager;
import com.wslclds.castn.helpers.Helper;
import com.wslclds.castn.factory.objects.Episode;
import com.wslclds.castn.factory.objects.PodcastAndEpisode;
import com.wslclds.castn.factory.objects.ToolbarEvent;
import com.wslclds.castn.factory.objects.UpdateEvent;
import com.wslclds.castn.items.FooterItem;
import com.wslclds.castn.items.TimelineItem;
import com.wslclds.castn.R;
import me.yokeyword.fragmentation.SupportFragment;

/**
 * A simple {@link Fragment} subclass.
 */
public class TimelineFragment extends SupportFragment {

    private int EPISODE_DETAIL_REQUEST_ID = 1;
    private ItemAdapter<TimelineItem> itemAdapter;
    private ItemAdapter<FooterItem> loadMoreItemItemAdapter;
    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    @BindView(R.id.swipeBackLayout)
    SwipeRefreshLayout swipeRefreshLayout;

    public static TimelineFragment newInstance() {

        Bundle args = new Bundle();

        TimelineFragment fragment = new TimelineFragment();
        fragment.setArguments(args);
        return fragment;
    }


    public TimelineFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_timeline, container, false);
        ButterKnife.bind(this,view);
        return view;
    }

    @Override
    public void onLazyInitView(@android.support.annotation.Nullable Bundle savedInstanceState) {
        super.onLazyInitView(savedInstanceState);
        recyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));
        itemAdapter = new ItemAdapter<>();
        loadMoreItemItemAdapter = new ItemAdapter<>();
        FastAdapter fastAdapter = FastAdapter.with(Arrays.asList(itemAdapter, loadMoreItemItemAdapter));
        recyclerView.setAdapter(fastAdapter);


        fastAdapter.withEventHook(new ClickEventHook() {
            @Nullable
            @Override
            public List<View> onBindMany(RecyclerView.ViewHolder viewHolder) {
                if (viewHolder instanceof TimelineItem.ViewHolder) {
                    List<View> list = new ArrayList<>();
                    list.add(((TimelineItem.ViewHolder) viewHolder).podcastLayout);
                    list.add(((TimelineItem.ViewHolder) viewHolder).episodeLayout);
                    list.add(((TimelineItem.ViewHolder) viewHolder).episodeLayout);
                    return list;
                }else if (viewHolder instanceof FooterItem.ViewHolder) {
                    List<View> list = new ArrayList<>();
                    list.add(((FooterItem.ViewHolder) viewHolder).loadMoreLayout);
                    return list;
                }
                return null;
            }

            @Override
            public void onClick(View v, int position, FastAdapter fastAdapter, IItem item) {
                if(v.getId() == R.id.podcastLayout){
                    ((SupportFragment) getParentFragment()).start(PodcastFragment.newInstance(((TimelineItem) item).getPodcastAndEpisode().getPodcast().getUrl()));
                }else if(v.getId() == R.id.episodeLayout){
                    TimelineItem timelineItem = (TimelineItem) item;
                    Episode episode = timelineItem.getPodcastAndEpisode().getEpisode();
                    if(episode != null){
                        Intent intent = new Intent(getContext(),EpisodeDetailActivity.class);
                        intent.putExtra("episode",new Gson().toJson(episode));
                        startActivityForResult(intent,EPISODE_DETAIL_REQUEST_ID);
                    }
                }else if(v.getId() == R.id.loadMoreLayout){
                    if(itemAdapter.getAdapterItemCount() > 0){
                        long lastPubDate = ((TimelineItem)itemAdapter.getAdapterItem(itemAdapter.getAdapterItemCount()-1)).getPodcastAndEpisode().getEpisode().getPubDate();
                        loadData(lastPubDate,15,0,false);
                    }else {
                        //loadData(new Date().getTime(),15,0,false);
                        ((MainFragment) getParentFragment()).changeIndex(2);
                    }
                }
            }
        });

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadData(new Date().getTime(),50,500,true);
            }
        });
        loadData(new Date().getTime(),50,1000,false);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == EPISODE_DETAIL_REQUEST_ID && resultCode == RESULT_OK){
            ((MainActivity)getContext()).playAudio(Helper.DEFAULT_PLAYLIST_ID);
        }
    }

    private void loadData(final long lastDate, final int limit, int delay, boolean clear){
        swipeRefreshLayout.setRefreshing(true);
        if(delay > 0){
            recyclerView.setVisibility(View.GONE);
        }
        if(clear){
            itemAdapter.clear();
        }
        loadMoreItemItemAdapter.clear();
        @SuppressLint("StaticFieldLeak") AsyncTask asyncTask = new AsyncTask() {
            ArrayList timelineItems;
            @Override
            protected Object doInBackground(Object[] objects) {
                DatabaseManager databaseManager = new DatabaseManager(getContext());
                //AdRequest adRequest = new AdRequest.Builder().addTestDevice("8DEC2D4B262561CBA8526DF1C945BFCF").build();
                //TimelineItemAd timelineItemAd = new TimelineItemAd(adRequest);
                timelineItems = new ArrayList<>();
                ArrayList<PodcastAndEpisode> podcastAndEpisodes = databaseManager.getTimeline(lastDate,limit);
                for(PodcastAndEpisode podcastAndEpisode : podcastAndEpisodes){
                    TimelineItem timelineItem = new TimelineItem(podcastAndEpisode);
                    timelineItems.add(timelineItem);
                    //if(((MainActivity)getContext()).showAds && timelineItems.size()%5 == 0){
                        //timelineItems.add(timelineItemAd);
                    //}
                }
                return null;
            }

            @Override
            protected void onPostExecute(Object o) {
                itemAdapter.add(timelineItems);
                if(timelineItems.size() == 0){
                    loadMoreItemItemAdapter.add(new FooterItem("Find new podcasts"));
                }else {
                    loadMoreItemItemAdapter.add(new FooterItem("Load more"));
                }
                if(swipeRefreshLayout.isRefreshing() && delay == 0){
                    swipeRefreshLayout.setRefreshing(false);
                }
            }
        };
        asyncTask.execute();
        new Handler().postDelayed(() -> {
            recyclerView.setVisibility(View.VISIBLE);
            if(swipeRefreshLayout.isRefreshing()){
                swipeRefreshLayout.setRefreshing(false);
            }
        },delay);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(UpdateEvent updateEvent) {
        if(updateEvent.shouldUpdate()){
            loadData(new Date().getTime(),50,0,true);
        }
    }

    public void scrollTop(){
        recyclerView.smoothScrollToPosition(0);
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onSupportVisible() {
        super.onSupportVisible();
        ToolbarEvent toolbarEvent = new ToolbarEvent();
        toolbarEvent.setTitle("Timeline");
        toolbarEvent.setHome(true);
        EventBus.getDefault().post(toolbarEvent);
    }

}

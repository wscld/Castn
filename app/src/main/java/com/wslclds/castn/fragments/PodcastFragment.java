package com.wslclds.castn.fragments;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.arlib.floatingsearchview.FloatingSearchView;
import com.arlib.floatingsearchview.suggestions.model.SearchSuggestion;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.gson.Gson;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.IAdapter;
import com.mikepenz.fastadapter.IItem;
import com.mikepenz.fastadapter.adapters.ItemAdapter;
import com.mikepenz.fastadapter.listeners.OnClickListener;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import com.wslclds.castn.activities.EpisodeDetailActivity;
import com.wslclds.castn.activities.MainActivity;
import com.wslclds.castn.activities.PodcastDetailActivity;
import com.wslclds.castn.builders.AlertBuilder;
import com.wslclds.castn.builders.PopUpMenuBuilder;
import com.wslclds.castn.factory.DatabaseManager;
import com.wslclds.castn.helpers.Helper;
import com.wslclds.castn.factory.objects.Episode;
import com.wslclds.castn.factory.objects.Podcast;
import com.wslclds.castn.factory.objects.ToolbarEvent;
import com.wslclds.castn.factory.objects.UpdateEvent;
import com.wslclds.castn.factory.Parser;
import com.wslclds.castn.GlideApp;
import com.wslclds.castn.items.EpisodeItem;
import com.wslclds.castn.items.FooterItem;
import com.wslclds.castn.R;
import com.wslclds.castn.items.TimelineItem;

import me.yokeyword.fragmentation.SupportFragment;

/**
 * A simple {@link Fragment} subclass.
 */
public class PodcastFragment extends SupportFragment {

    String currentUrl;
    DatabaseManager databaseManager;
    ItemAdapter<EpisodeItem> itemAdapter;
    ItemAdapter<FooterItem> loadMoreItemItemAdapter;
    Podcast podcast;
    ArrayList<Episode> episodes;
    int themeColor;
    @BindView(R.id.description)
    TextView description;
    @BindView(R.id.image)
    ImageView image;
    @BindView(R.id.topLayout)
    LinearLayout topLayout;
    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    @BindView(R.id.subscribe)
    Button subscribe;
    @BindView(R.id.episodeCount)
    TextView episodeCount;
    @BindView(R.id.loadingEpisodeCount)
    ProgressBar loadingEpisodeCount;
    @BindView(R.id.searchView)
    FloatingSearchView searchView;
    @BindView(R.id.subscribedLayout)
    LinearLayout subscribedLayout;
    @BindView(R.id.moreOptionsButton)
    ImageButton moreOptionsButton;
    @BindView(R.id.swipeRefreshLayout)
    SwipeRefreshLayout swipeRefreshLayout;

    AsyncTask loadDataTask;
    AsyncTask getSomeEpisodesTask;
    AsyncTask getAllEpisodesTask;
    AsyncTask getEpisodesOfflineTask;
    AsyncTask loadNewEpisodesTask;
    AsyncTask loadEpisodesTask;
    AsyncTask loadPodcastDataTask;
    private int EPISODE_DETAIL_REQUEST_ID = 1;

    public static PodcastFragment newInstance(String url) {

        Bundle args = new Bundle();
        args.putString("url",url);
        PodcastFragment fragment = new PodcastFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public PodcastFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_podcast, container, false);
        ButterKnife.bind(this,view);
        themeColor = getResources().getColor(R.color.colorPrimaryDark);
        currentUrl = getArguments().getString("url");
        databaseManager = new DatabaseManager(getContext());
        setLoadingEpisodes(true);
        setLoadingEpisodeCount(true);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));
        itemAdapter = new ItemAdapter<>();
        loadMoreItemItemAdapter = new ItemAdapter<>();
        FastAdapter fastAdapter = FastAdapter.with(Arrays.asList(itemAdapter, loadMoreItemItemAdapter));
        recyclerView.setAdapter(fastAdapter);
        ViewCompat.setNestedScrollingEnabled(recyclerView,false);
        loadMoreItemItemAdapter.add(new FooterItem("Load more"));


        if(databaseManager.isSubscribed(currentUrl)){
            subscribe.setText("unsubscribe");
            subscribedLayout.setVisibility(View.VISIBLE);
        }else {
            subscribe.setText("subscribe");
            subscribedLayout.setVisibility(View.GONE);
        }

        searchView.setDimBackground(false);
        searchView.findViewById(R.id.search_bar_left_action_container).setVisibility(View.GONE);
        searchView.setOnSearchListener(new FloatingSearchView.OnSearchListener() {
            @Override
            public void onSuggestionClicked(SearchSuggestion searchSuggestion) {

            }

            @Override
            public void onSearchAction(String currentQuery) {
                start(EpisodeListFragment.newInstance(EpisodeListFragment.TYPE_EPISODE_SEARCH,currentUrl,currentQuery));
            }
        });

        subscribe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setLoadingEpisodes(true);
                setLoadingEpisodeCount(true);
                if(podcast != null) {
                    if(databaseManager.isSubscribed(currentUrl)){
                        databaseManager.unsubscribe(currentUrl, new DatabaseManager.DatabaseUpdateListener() {
                            @Override
                            public void onSuccess() {
                                updateSubscribe();
                            }

                            @Override
                            public void onFailure() {
                                Toast.makeText(getContext(),"Unsubscription failed",Toast.LENGTH_SHORT).show();
                            }
                        });
                    }else if(episodes != null && podcast != null){
                        databaseManager.subscribe(currentUrl, podcast, episodes, new DatabaseManager.DatabaseUpdateListener() {
                            @Override
                            public void onSuccess() {
                                updateSubscribe();
                                databaseManager.updatePodcastPubDate(podcast.getUrl());
                            }

                            @Override
                            public void onFailure() {
                                Toast.makeText(getContext(),"Subscription failed",Toast.LENGTH_SHORT).show();

                            }
                        });
                    }
                }
            }
        });

        moreOptionsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<String> items = new ArrayList<>();
                items.add("Download");

                new PopUpMenuBuilder(getContext(), v, items, new PopUpMenuBuilder.WithOnClickListener() {
                    @Override
                    public void onClickListener(int position) {
                        if (position == 0){
                            new AlertBuilder(getContext(), "Are you sure?", "This will download only the latest and unlistened episodes", new AlertBuilder.onButtonClick2() {
                                @Override
                                public void onConfirm() {
                                    if(episodes.size() > 5){
                                        Helper helper = new Helper(getContext());
                                        helper.makeDownload(episodes.subList(0,5));
                                    }else if(episodes.size() > 0){
                                        Helper helper = new Helper(getContext());
                                        helper.makeDownload(episodes);
                                    }
                                }

                                @Override
                                public void onCancel() {

                                }
                            }).show();
                        }
                    }
                }).showMenu();
            }
        });

        fastAdapter.withOnClickListener(new OnClickListener() {
            @Override
            public boolean onClick(@javax.annotation.Nullable View v, IAdapter adapter, IItem item, int position) {
                if(item.getClass() == FooterItem.class){
                    loadEpisodes(10,0);
                }else {
                    EpisodeItem episodeItem = (EpisodeItem)item;
                    Episode episode = episodeItem.getEpisode();
                    if(episode != null){
                        Intent intent = new Intent(getContext(),EpisodeDetailActivity.class);
                        intent.putExtra("episode",new Gson().toJson(episode));
                        startActivityForResult(intent,EPISODE_DETAIL_REQUEST_ID);
                    }
                    //EpisodeItem episodeItem = (EpisodeItem)item;
                    //new Helper().makeDownload(getContext(),episodeItem.getEpisode());
                }
                return true;
            }
        });
        loadData();


        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefreshLayout.setRefreshing(false);
                if(!isLoading()){
                    setLoadingEpisodes(true);
                    setLoadingEpisodeCount(true);
                    getAllEpisodes();
                }
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == EPISODE_DETAIL_REQUEST_ID && resultCode == RESULT_OK){
            ((MainActivity)getContext()).playAudio(Helper.DEFAULT_PLAYLIST_ID);
        }
    }

    private void updateSubscribe(){
        setLoadingEpisodes(false);
        setLoadingEpisodeCount(false);
        sendUpdate();
        if(databaseManager.isSubscribed(currentUrl)){
            subscribe.setText("unsubscribe");
            subscribedLayout.setVisibility(View.VISIBLE);
        }else if(episodes != null && podcast != null){
            subscribe.setText("subscribe");
            subscribedLayout.setVisibility(View.GONE);
        }
    }

    private void setLoadingEpisodes(boolean loading){
        if(loading){
            subscribe.setEnabled(false);
            subscribe.setAlpha(0.4f);
        }else {
            subscribe.setEnabled(true);
            subscribe.setAlpha(1.0f);
        }
    }

    private void setLoadingEpisodeCount(boolean loading){
        if(loading){
            loadingEpisodeCount.setVisibility(View.VISIBLE);
        }else {
            loadingEpisodeCount.setVisibility(View.GONE);
        }
    }

    private boolean isLoading(){
        if(subscribe.isEnabled() && loadingEpisodeCount.getVisibility() == View.GONE){
            return false;
        }else {
            return true;
        }
    }

    @SuppressLint("StaticFieldLeak")
    private void loadData(){
        loadDataTask = new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] objects) {
                DatabaseManager databaseManager = new DatabaseManager(getContext());
                if(databaseManager.isSubscribed(currentUrl)){
                    podcast = databaseManager.getPodcast(currentUrl);
                }else {
                    podcast = new Parser(getContext()).parsePodcast(currentUrl);
                }
                return null;
            }

            @Override
            protected void onPostExecute(Object o) {
                loadPodcastData();
                if(databaseManager.isSubscribed(currentUrl)){
                    getEpisodesOffline(false,false,false);
                }else {
                    getSomeEpisodes();
                }
            }
        };
        loadDataTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @SuppressLint("StaticFieldLeak")
    private void getEpisodesOffline(boolean onlyDownloaded, boolean onlyUnfinished, boolean reverse){
        getEpisodesOfflineTask = new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] objects) {
                DatabaseManager databaseManager = new DatabaseManager(getContext());
                if(reverse){
                    episodes = databaseManager.getEpisodesAscending(currentUrl);
                }else {
                    episodes = databaseManager.getEpisodes(currentUrl);
                }
                if(onlyDownloaded){
                    ArrayList<Episode> tempEpisodes = new ArrayList<>();
                    for(Episode episode : episodes){
                        if(databaseManager.getDownloadStatus(episode.getEnclosureUrl()) == Helper.STATE_DOWNLOADED){
                            tempEpisodes.add(episode);
                        }
                    }
                    episodes.clear();
                    episodes = tempEpisodes;
                    tempEpisodes.clear();
                }

                if(onlyUnfinished){
                    ArrayList<Episode> tempEpisodes = new ArrayList<>();
                    for(Episode episode : episodes){
                        if(!databaseManager.isEpisodeListened(episode.getEnclosureUrl())){
                            tempEpisodes.add(episode);
                        }
                    }
                    episodes.clear();
                    episodes = tempEpisodes;
                    tempEpisodes.clear();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Object o) {
                setLoadingEpisodes(false);
                episodeCount.setText(episodes.size()+" episodes");

                if(podcast != null){
                    //load podcast
                    loadPodcastData();
                    //load episodes
                    loadEpisodes(10,500);
                }
                if(episodes.size() == 0){
                    getSomeEpisodes();
                }else {
                    loadNewEpisodes();
                }
            }
        };
        getEpisodesOfflineTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @SuppressLint("StaticFieldLeak")
    private void getSomeEpisodes(){
        getSomeEpisodesTask = new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] objects) {
                episodes = new Parser(getContext()).parseEpisodes(currentUrl,true);
                return null;
            }

            @Override
            protected void onPostExecute(Object o) {
                episodeCount.setText(episodes.size()+" episodes");

                if(podcast != null){
                    //load podcast
                    loadPodcastData();
                    //load episodes
                    loadEpisodes(10,500);
                }
                getAllEpisodes();
            }
        };
        getSomeEpisodesTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @SuppressLint("StaticFieldLeak")
    private void getAllEpisodes(){
        getAllEpisodesTask = new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] objects) {
                episodes = new Parser(getContext()).parseEpisodes(currentUrl,false);
                if(databaseManager.isSubscribed(currentUrl)){
                    databaseManager.storeEpisodes(episodes);
                }
                return null;
            }

            @Override
            protected void onPostExecute(Object o) {
                setLoadingEpisodes(false);
                setLoadingEpisodeCount(false);
                episodeCount.setText(episodes.size()+" episodes");

                if(podcast != null){
                    //load podcast
                    loadPodcastData();
                    //load episodes
                    loadEpisodes(10,500);
                }
            }
        };
        getAllEpisodesTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @SuppressLint("StaticFieldLeak")
    private void loadNewEpisodes(){
        loadNewEpisodesTask = new AsyncTask() {
            ArrayList<Episode> newEpisodes;
            ArrayList<EpisodeItem> episodeItems;
            @Override
            protected Object doInBackground(Object[] objects) {
                DatabaseManager databaseManager = new DatabaseManager(getContext());
                Episode latestEpisode = databaseManager.getLatestStoredEpisode(currentUrl);
                if(latestEpisode != null){
                    episodes.addAll(0,new Parser(getContext()).parseEpisodesUntil(currentUrl,latestEpisode.getPubDate(),10*DateUtils.MINUTE_IN_MILLIS));
                }
                newEpisodes = new ArrayList<>();

                for(Episode episode: episodes){
                    if(latestEpisode == null || episode.getPubDate() == latestEpisode.getPubDate()) {
                        break;
                    }else {
                        newEpisodes.add(episode);
                    }
                }

                if(databaseManager.isSubscribed(currentUrl)){
                    databaseManager.storeNewEpisodes(currentUrl,episodes);
                }

                episodeItems = createArrayOfItems(newEpisodes);
                return null;
            }

            @Override
            protected void onPostExecute(Object o) {
                setLoadingEpisodeCount(false);
                databaseManager.updatePodcastPubDate(podcast.getUrl());
                episodeCount.setText(episodes.size()+" episodes");

                if(episodeItems.size() > 0){
                    itemAdapter.add(0,episodeItems);
                    sendUpdate();
                }
            }
        };
        loadNewEpisodesTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void loadEpisodes(final int limit, int delay){
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @SuppressLint("StaticFieldLeak")
            @Override
            public void run() {
                int finalLimit = limit;
                if(episodes != null && itemAdapter != null){
                    finalLimit = finalLimit+itemAdapter.getAdapterItemCount();
                    if(finalLimit > episodes.size()){
                        finalLimit = episodes.size();
                    }

                    final int finalFinalLimit = finalLimit;
                    loadEpisodesTask = new AsyncTask() {
                        List<Episode> episodesToShow;
                        ArrayList<EpisodeItem> episodeItems;
                        @Override
                        protected Object doInBackground(Object[] objects) {
                            episodesToShow = episodes.subList(itemAdapter.getAdapterItemCount(), finalFinalLimit);
                            episodeItems = createArrayOfItems(episodesToShow);
                            return null;
                        }

                        @Override
                        protected void onPostExecute(Object o) {
                            itemAdapter.add(episodeItems);
                            if(episodes.size() > 0 && episodeItems.size() == 0){
                                loadMoreItemItemAdapter.clear();
                            }
                        }
                    };
                    loadEpisodesTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }
            }
        },delay);
    }

    @SuppressLint("StaticFieldLeak")
    private void loadPodcastData(){
        if(podcast != null) {
            int color = podcast.getColor();
            int darker = Helper.darker(color,0.7f);
            themeColor = darker;

            loadPodcastDataTask = new AsyncTask() {
                int color;
                int darker;
                String podcastDescription;
                String podcastImage;
                @Override
                protected Object doInBackground(Object[] objects) {
                    podcastDescription = podcast.getPlainDescription();
                    podcastImage = podcast.getImage();
                    color = podcast.getColor();
                    darker = Helper.darker(color,0.7f);
                    return null;
                }

                @Override
                protected void onPostExecute(Object o) {
                    description.setText(podcastDescription);
                    description.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if(podcast != null){
                                Intent intent = new Intent(getContext(),PodcastDetailActivity.class);
                                intent.putExtra("podcast",new Gson().toJson(podcast));
                                startActivity(intent);
                            }
                        }
                    });
                    if(getContext() != null && color != 0){
                        themeColor = darker;
                        topLayout.setBackgroundColor(darker);
                        GlideApp.with(getContext()).load(podcastImage).diskCacheStrategy(DiskCacheStrategy.ALL).placeholder(R.drawable.castn_icon_2).override(200,200).into(image);
                        sendToolbarStatus();
                    }else if(getContext() != null){
                        GlideApp.with(getContext()).asBitmap().load(podcastImage).diskCacheStrategy(DiskCacheStrategy.ALL).placeholder(R.drawable.castn_icon_2).override(200,200).into(new SimpleTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                if(getContext() != null){
                                    GlideApp.with(getContext()).load(resource).diskCacheStrategy(DiskCacheStrategy.ALL).placeholder(R.drawable.castn_icon_2).override(200,200).into(image);
                                    Palette.from(resource).generate(new Palette.PaletteAsyncListener() {
                                        @Override
                                        public void onGenerated(@NonNull Palette palette) {
                                            if(getContext() != null){
                                                color = palette.getDominantColor(getResources().getColor(R.color.colorPrimary));
                                                darker = Helper.darker(color,0.7f);
                                                themeColor = darker;
                                                databaseManager.setColor(currentUrl,color);
                                                topLayout.setBackgroundColor(darker);
                                                sendToolbarStatus();
                                            }
                                        }
                                    });
                                }
                            }
                        });
                    }
                }

            };
            loadPodcastDataTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    private void destroyAsyncTask(AsyncTask asyncTask){
        if(asyncTask != null && asyncTask.getStatus() == AsyncTask.Status.RUNNING){
            asyncTask.cancel(true);
        }
    }

    private ArrayList<EpisodeItem> createArrayOfItems(ArrayList<Episode> episodes){
        ArrayList<EpisodeItem> episodeItems = new ArrayList<>();
        for(Episode episode : episodes){
            EpisodeItem episodeItem = new EpisodeItem(episode);
            episodeItems.add(episodeItem);
        }
        return episodeItems;
    }
    private ArrayList<EpisodeItem> createArrayOfItems(List<Episode> episodes){
        ArrayList<EpisodeItem> episodeItems = new ArrayList<>();
        for(Episode episode : episodes){
            EpisodeItem episodeItem = new EpisodeItem(episode);
            episodeItems.add(episodeItem);
        }
        return episodeItems;
    }

    private void sendToolbarStatus(){
        ToolbarEvent toolbarEvent = new ToolbarEvent();
        toolbarEvent.setColor(themeColor);
        if(podcast != null){
            toolbarEvent.setTitle(podcast.getTitle());
        }else {
            toolbarEvent.setTitle("Podcast");
        }
        EventBus.getDefault().post(toolbarEvent);
    }

    private void sendUpdate(){
        UpdateEvent updateEvent = new UpdateEvent();
        updateEvent.setShouldUpdate(true);
        EventBus.getDefault().post(updateEvent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        destroyAsyncTask(getSomeEpisodesTask);
        destroyAsyncTask(loadDataTask);
        destroyAsyncTask(loadEpisodesTask);
        destroyAsyncTask(getEpisodesOfflineTask);
        destroyAsyncTask(loadNewEpisodesTask);
        destroyAsyncTask(loadPodcastDataTask);
    }

    @Override
    public void onSupportVisible() {
        super.onSupportVisible();
        sendToolbarStatus();
    }
}

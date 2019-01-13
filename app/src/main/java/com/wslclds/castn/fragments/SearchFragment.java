package com.wslclds.castn.fragments;


import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.arlib.floatingsearchview.FloatingSearchView;
import com.arlib.floatingsearchview.suggestions.model.SearchSuggestion;
import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.IAdapter;
import com.mikepenz.fastadapter.IItem;
import com.mikepenz.fastadapter.adapters.ItemAdapter;
import com.mikepenz.fastadapter.listeners.OnClickListener;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import com.mikepenz.iconics.IconicsDrawable;
import com.wslclds.castn.factory.DataGetter;
import com.wslclds.castn.factory.dataRetriever.SearchApi;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.wslclds.castn.factory.DatabaseManager;
import com.wslclds.castn.factory.objects.Podcast;
import com.wslclds.castn.factory.objects.ToolbarEvent;
import com.wslclds.castn.items.PodcastItem;
import com.wslclds.castn.R;
import com.wslclds.castn.items.SearchSuggestionItem;
import com.wslclds.castn.items.TextHeaderItem;

import me.yokeyword.fragmentation.SupportFragment;

/**
 * A simple {@link Fragment} subclass.
 */
public class SearchFragment extends SupportFragment {

    DatabaseManager databaseManager;
    ItemAdapter itemAdapter;
    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    @BindView(R.id.searchView)
    FloatingSearchView searchView;
    @BindView(R.id.progress)
    ProgressBar progressBar;

    public static SearchFragment newInstance() {

        Bundle args = new Bundle();

        SearchFragment fragment = new SearchFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public SearchFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);
        ButterKnife.bind(this,view);
        return view;
    }

    @Override
    public void onLazyInitView(@android.support.annotation.Nullable Bundle savedInstanceState) {
        super.onLazyInitView(savedInstanceState);
        databaseManager = new DatabaseManager(getContext());
        recyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));
        itemAdapter = new ItemAdapter();
        FastAdapter fastAdapter = FastAdapter.with(itemAdapter);
        recyclerView.setAdapter(fastAdapter);

        fastAdapter.withOnClickListener(new OnClickListener() {
            @Override
            public boolean onClick(@Nullable View v, IAdapter adapter, IItem item, int position) {
                if(item.getClass() == PodcastItem.class){
                    PodcastItem podcastItem = (PodcastItem)item;
                    ((SupportFragment) getParentFragment()).start(PodcastFragment.newInstance(podcastItem.getPodcast().getUrl()));
                }
                return true;
            }
        });

        showTopPodcasts();
        setupSearch();
    }

    private void setupSearch(){
        searchView.setOnSearchListener(new FloatingSearchView.OnSearchListener() {
            @Override
            public void onSuggestionClicked(SearchSuggestion searchSuggestion) {
                searchPodcasts(searchSuggestion.getBody());
            }

            @Override
            public void onSearchAction(String currentQuery) {
                searchPodcasts(currentQuery);
            }
        });
        searchView.setOnQueryChangeListener(new FloatingSearchView.OnQueryChangeListener() {
            @Override
            public void onSearchTextChanged(String oldQuery, String newQuery) {
                ArrayList<Podcast> podcasts = databaseManager.searchPodcasts(newQuery);
                List<SearchSuggestionItem> searchSuggestions = new ArrayList<>();
                for(Podcast podcast : podcasts){
                    searchSuggestions.add(new SearchSuggestionItem(podcast.getTitle()));
                }
                searchView.swapSuggestions(searchSuggestions);
            }
        });
    }

    @Override
    public boolean onBackPressedSupport() {
        if(searchView.getQuery().length() > 0){
            searchView.clearQuery();
            itemAdapter.clear();
            showTopPodcasts();
            return true;
        }else {
            return super.onBackPressedSupport();
        }
    }

    private void searchPodcasts(final String term){
        if(term.contains("http://") || term.contains("https://")){
            ((SupportFragment) getParentFragment()).start(PodcastFragment.newInstance(term));
        }else{
            progressBar.setVisibility(View.VISIBLE);
            itemAdapter.removeRange(0,itemAdapter.getAdapterItemCount());
            @SuppressLint("StaticFieldLeak") AsyncTask asyncTask = new AsyncTask() {
                ArrayList<Podcast> podcasts;
                @Override
                protected Object doInBackground(Object[] objects) {
                    SearchApi searchApi = new SearchApi(getContext());
                    podcasts = searchApi.findPodcastWithTerm(term);
                    return null;
                }

                @Override
                protected void onPostExecute(Object o) {
                    progressBar.setVisibility(View.GONE);
                    itemAdapter.clear();
                    for(Podcast podcast : podcasts){
                        PodcastItem podcastItem = new PodcastItem(podcast);
                        itemAdapter.add(podcastItem);
                    }
                }
            };
            asyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    private void showTopPodcasts(){
        DataGetter dataGetter = new DataGetter(getContext());
        @SuppressLint("StaticFieldLeak") AsyncTask asyncTask = new AsyncTask() {
            String json;
            @Override
            protected Object doInBackground(Object[] objects) {
                String country = getContext().getResources().getConfiguration().locale.getCountry();
                if(country.equals("BR")){
                    json = dataGetter.getJson("https://firestore.googleapis.com/v1beta1/projects/castn-74856/databases/(default)/documents/top/BR/general");
                }else {
                    json = dataGetter.getJson("https://firestore.googleapis.com/v1beta1/projects/castn-74856/databases/(default)/documents/top/US/general");
                }
                return null;
            }

            @Override
            protected void onPostExecute(Object o) {
                TextHeaderItem textHeaderItem = new TextHeaderItem("Top Podcasts",new IconicsDrawable(getContext(),CommunityMaterial.Icon.cmd_star));
                itemAdapter.add(textHeaderItem);
                super.onPostExecute(o);
                try {
                    JSONObject mainObject = new JSONObject(json);
                    JSONArray mainArray = mainObject.getJSONArray("documents");
                    for(int i = 0; i < mainArray.length(); i++){
                        JSONObject fieldObject = mainArray.getJSONObject(i).getJSONObject("fields");
                        String title = fieldObject.getJSONObject("title").getString("stringValue");
                        String url = fieldObject.getJSONObject("url").getString("stringValue");
                        String cover = fieldObject.getJSONObject("cover").getString("stringValue");

                        Podcast podcast = new Podcast();
                        podcast.setUrl(url);
                        podcast.setTitle(title);
                        podcast.setImage(cover);
                        PodcastItem podcastItem = new PodcastItem(podcast);
                        itemAdapter.add(podcastItem);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };
        asyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public void scrollTop(){
        recyclerView.smoothScrollToPosition(0);
    }


    @Override
    public void onSupportVisible() {
        super.onSupportVisible();
        ToolbarEvent toolbarEvent = new ToolbarEvent();
        toolbarEvent.setTitle("Search");
        toolbarEvent.setHome(true);
        EventBus.getDefault().post(toolbarEvent);
    }
}

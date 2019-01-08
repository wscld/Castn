package com.wslclds.castn.fragments;


import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.IAdapter;
import com.mikepenz.fastadapter.IItem;
import com.mikepenz.fastadapter.adapters.ItemAdapter;
import com.mikepenz.fastadapter.listeners.OnClickListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import com.wslclds.castn.factory.DatabaseManager;
import com.wslclds.castn.factory.objects.Podcast;
import com.wslclds.castn.factory.objects.ToolbarEvent;
import com.wslclds.castn.factory.objects.UpdateEvent;
import com.wslclds.castn.items.SubscriptionItem;
import com.wslclds.castn.R;
import me.yokeyword.fragmentation.SupportFragment;

/**
 * A simple {@link Fragment} subclass.
 */
public class SubscriptionsFragment extends SupportFragment {

    ItemAdapter itemAdapter;
    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    @BindView(R.id.swipeRefreshLayout)
    SwipeRefreshLayout swipeRefreshLayout;

    public static SubscriptionsFragment newInstance() {

        Bundle args = new Bundle();

        SubscriptionsFragment fragment = new SubscriptionsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public SubscriptionsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_subscriptions, container, false);
        ButterKnife.bind(this,view);
        return view;
    }

    @Override
    public void onLazyInitView(@Nullable Bundle savedInstanceState) {
        super.onLazyInitView(savedInstanceState);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(),3));
        itemAdapter = new ItemAdapter();
        FastAdapter fastAdapter = FastAdapter.with(itemAdapter);
        recyclerView.setAdapter(fastAdapter);

        fastAdapter.withOnClickListener(new OnClickListener() {
            @Override
            public boolean onClick(@javax.annotation.Nullable View v, IAdapter adapter, IItem item, int position) {
                ((SupportFragment) getParentFragment()).start(PodcastFragment.newInstance(((SubscriptionItem) item).getPodcast().getUrl()));
                return false;
            }
        });

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadSubscriptions();
            }
        });
        loadSubscriptions();
    }

    private void loadSubscriptions(){
        swipeRefreshLayout.setRefreshing(true);
        itemAdapter.clear();
        @SuppressLint("StaticFieldLeak") AsyncTask asyncTask = new AsyncTask() {
            ArrayList<Podcast> podcasts;
            @Override
            protected Object doInBackground(Object[] objects) {
                DatabaseManager databaseManager = new DatabaseManager(getContext());
                podcasts = databaseManager.getPodcasts();
                return null;
            }

            @Override
            protected void onPostExecute(Object o) {
                for(Podcast podcast : podcasts){
                    if(podcast != null){
                        SubscriptionItem subscriptionItem = new SubscriptionItem(podcast);
                        itemAdapter.add(subscriptionItem);
                    }
                }
                swipeRefreshLayout.setRefreshing(false);
            }
        };
        asyncTask.execute();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(UpdateEvent updateEvent) {
        if(updateEvent.shouldUpdate()){
            loadSubscriptions();
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
        toolbarEvent.setTitle("Subscriptions");
        toolbarEvent.setHome(true);
        EventBus.getDefault().post(toolbarEvent);
    }
}

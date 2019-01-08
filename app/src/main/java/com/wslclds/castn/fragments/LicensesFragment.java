package com.wslclds.castn.fragments;


import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
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

import java.util.ArrayList;

import com.wslclds.castn.R;
import com.wslclds.castn.builders.AlertBuilder;
import com.wslclds.castn.factory.DatabaseManager;
import com.wslclds.castn.factory.dataRetriever.LicenseApi;
import com.wslclds.castn.factory.objects.License;
import com.wslclds.castn.factory.objects.ToolbarEvent;
import com.wslclds.castn.items.LicenseItem;
import butterknife.BindView;
import butterknife.ButterKnife;
import me.yokeyword.fragmentation.SupportFragment;

/**
 * A simple {@link Fragment} subclass.
 */
public class LicensesFragment extends SupportFragment {

    DatabaseManager databaseManager;
    ItemAdapter itemAdapter;
    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;

    public static LicensesFragment newInstance() {

        Bundle args = new Bundle();

        LicensesFragment fragment = new LicensesFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public LicensesFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_licenses, container, false);
        ButterKnife.bind(this,view);
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
                LicenseItem licenseItem = (LicenseItem)item;
                AlertBuilder alertBuilder = new AlertBuilder(getContext(),licenseItem.getLicense().getRepo(),"loading...");
                alertBuilder.show();
                @SuppressLint("StaticFieldLeak") AsyncTask asyncTask = new AsyncTask() {
                    String licenseBody;
                    @Override
                    protected Object doInBackground(Object[] objects) {
                        licenseBody = new LicenseApi(getContext()).getLicenseFromGithub(licenseItem.getLicense().getUrl());
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Object o) {
                        alertBuilder.updateDescription(licenseBody);
                    }
                };
                asyncTask.execute();
                return true;
            }
        });

        loadLicenses();
    }

    private void loadLicenses(){
        @SuppressLint("StaticFieldLeak") AsyncTask asyncTask = new AsyncTask() {
            ArrayList<License> licenses = new ArrayList<>();
            @Override
            protected Object doInBackground(Object[] objects) {
                licenses = new ArrayList<>();
                String[] urls = new String[]{
                        "https://github.com/jhy/jsoup",
                        "https://github.com/YoKeyword/Fragmentation",
                        "https://github.com/JakeWharton/butterknife",
                        "https://github.com/hdodenhof/CircleImageView",
                        "https://github.com/bumptech/glide",
                        "https://github.com/mikepenz/FastAdapter",
                        "https://github.com/google/gson",
                        "https://github.com/firebase/firebase-jobdispatcher-android",
                        "https://github.com/greenrobot/EventBus",
                        "https://github.com/Ashok-Varma/BottomNavigation",
                        "https://github.com/umano/AndroidSlidingUpPanel",
                        "https://github.com/Blogcat/Android-ExpandableTextView",
                        "https://github.com/bskim45/MaxHeightScrollView",
                        "https://github.com/amitshekhariitbhu/Fast-Android-Networking",
                        "https://github.com/liuguangqiang/SwipeBack",
                        "https://github.com/arimorty/floatingsearchview",
                        "https://github.com/rengwuxian/MaterialEditText",
                        "https://github.com/MiguelCatalan/MaterialSearchView",
                        "https://github.com/yanzhenjie/AndPermission",
                        "https://github.com/mikepenz/Android-Iconics"
                };


                for(int i = 0; i < urls.length; i++){
                    Uri uri = Uri.parse(urls[i]);
                    License license = new License();
                    String author = uri.getPathSegments().get(0);
                    String repo = uri.getPathSegments().get(1);

                    license.setAuthor(author);
                    license.setRepo(repo);
                    license.setUrl(uri.toString());

                    licenses.add(license);
                }

                return null;
            }

            @Override
            protected void onPostExecute(Object o) {
                for(License license : licenses){
                    itemAdapter.add(new LicenseItem(license));
                }
            }
        };
        asyncTask.execute();
    }

    @Override
    public void onSupportVisible() {
        super.onSupportVisible();
        ToolbarEvent toolbarEvent = new ToolbarEvent();
        toolbarEvent.setTitle("Licenses");
        toolbarEvent.setHome(false);
        EventBus.getDefault().post(toolbarEvent);
    }
}

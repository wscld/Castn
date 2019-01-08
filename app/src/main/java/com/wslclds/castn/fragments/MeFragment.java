package com.wslclds.castn.fragments;


import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.IAdapter;
import com.mikepenz.fastadapter.IItem;
import com.mikepenz.fastadapter.adapters.ItemAdapter;
import com.mikepenz.fastadapter.listeners.OnClickListener;
import com.mikepenz.iconics.IconicsDrawable;

import org.greenrobot.eventbus.EventBus;

import com.wslclds.castn.activities.MainActivity;
import com.wslclds.castn.factory.DatabaseManager;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.wslclds.castn.factory.objects.ToolbarEvent;
import com.wslclds.castn.items.MenuItem;
import com.wslclds.castn.R;
import me.yokeyword.fragmentation.SupportFragment;

/**
 * A simple {@link Fragment} subclass.
 */
public class MeFragment extends SupportFragment {

    DatabaseManager databaseManager;
    ItemAdapter itemAdapter;
    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;

    public static MeFragment newInstance() {

        Bundle args = new Bundle();

        MeFragment fragment = new MeFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public MeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_me, container, false);
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
                if(position == 0){
                    ((SupportFragment) getParentFragment()).start(PlaylistsFragment.newInstance());
                }else if(position == 1){
                    ((SupportFragment) getParentFragment()).start(EpisodeListFragment.newInstance(EpisodeListFragment.TYPE_DOWNLOADED));
                }else if(position == 2){
                    ((SupportFragment) getParentFragment()).start(EpisodeListFragment.newInstance(EpisodeListFragment.TYPE_UNFINISHED));
                }else if(position == 3){
                    ((SupportFragment) getParentFragment()).start(SettingsFragment.newInstance());
                }else if(position == 4){
                    ((SupportFragment) getParentFragment()).start(AboutFragment.newInstance());
                }
                return true;
            }
        });

        loadMenuItems();
    }


    private void loadMenuItems(){
        //playlists
        MenuItem i1 = new MenuItem("Playlists",new IconicsDrawable(getContext()).icon(CommunityMaterial.Icon.cmd_view_list).color(Color.WHITE).paddingDp(9));
        itemAdapter.add(i1);
        //downloaded
        MenuItem i2 = new MenuItem("Downloaded",new IconicsDrawable(getContext()).icon(CommunityMaterial.Icon.cmd_download).color(Color.WHITE).paddingDp(9));
        itemAdapter.add(i2);
        //in progress
        MenuItem i3 = new MenuItem("In Progress",new IconicsDrawable(getContext()).icon(CommunityMaterial.Icon.cmd_loading).color(Color.WHITE).paddingDp(9));
        itemAdapter.add(i3);
        //settings
        MenuItem i4 = new MenuItem("Settings",new IconicsDrawable(getContext()).icon(CommunityMaterial.Icon.cmd_settings).color(Color.WHITE).paddingDp(9));
        itemAdapter.add(i4);
        //about
        MenuItem i5 = new MenuItem("About",new IconicsDrawable(getContext(),CommunityMaterial.Icon.cmd_information_variant).color(Color.WHITE).paddingDp(9));
        itemAdapter.add(i5);
        //remove ads
        //MenuItem i5 = new MenuItem("Remove ads",new IconicsDrawable(getContext()).icon(CommunityMaterial.Icon.cmd_star).color(Color.WHITE).paddingDp(9));
        //itemAdapter.add(i5);
        // backup
        //MenuItem i6 = new MenuItem("Backup",new IconicsDrawable(getContext(),CommunityMaterial.Icon.cmd_backup_restore).color(Color.WHITE).paddingDp(5));
        //itemAdapter.add(i6);
    }

    @Override
    public void onSupportVisible() {
        super.onSupportVisible();
        ToolbarEvent toolbarEvent = new ToolbarEvent();
        toolbarEvent.setTitle("Me");
        toolbarEvent.setHome(true);
        EventBus.getDefault().post(toolbarEvent);
    }
}

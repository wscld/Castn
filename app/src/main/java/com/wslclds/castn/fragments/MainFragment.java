package com.wslclds.castn.fragments;


import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ashokvarma.bottomnavigation.BottomNavigationBar;
import com.ashokvarma.bottomnavigation.BottomNavigationItem;
import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import java.util.ArrayList;

import com.wslclds.castn.helpers.ThemeHelper;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.wslclds.castn.R;
import me.yokeyword.fragmentation.SupportFragment;

/**
 * A simple {@link Fragment} subclass.
 */
public class MainFragment extends SupportFragment {

    ArrayList<SupportFragment> fragments;
    @BindView(R.id.bottomNav)
    BottomNavigationBar bottomNav;

    public static MainFragment newInstance(String appLinkData) {
        Bundle args = new Bundle();
        args.putString("appLinkData",appLinkData);
        MainFragment fragment = new MainFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public MainFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        ButterKnife.bind(this,view);
        fragments = new ArrayList<>();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (findFragment(TimelineFragment.class) == null) {
            fragments.add(TimelineFragment.newInstance());
            fragments.add(SubscriptionsFragment.newInstance());
            fragments.add(SearchFragment.newInstance());
            fragments.add(DownloadsFragment.newInstance());
            fragments.add(MeFragment.newInstance());
            loadMultipleRootFragment(R.id.mainLayout,0,fragments.get(0),fragments.get(1),fragments.get(2),fragments.get(3),fragments.get(4));
        }else {
            fragments.add(0,findFragment(TimelineFragment.class));
            fragments.add(1,findFragment(SubscriptionsFragment.class));
            fragments.add(2,findFragment(SearchFragment.class));
            fragments.add(3,findFragment(DownloadsFragment.class));
            fragments.add(4,findFragment(MeFragment.class));
        }
        showHideFragment(fragments.get(0));

        String appLinkData = getArguments().getString("appLinkData");
        if(appLinkData != null){
            String query = Uri.parse(appLinkData).getQueryParameter("feed");
            start(PodcastFragment.newInstance(query));
        }
    }

    @Override
    public void onLazyInitView(@Nullable Bundle savedInstanceState) {
        super.onLazyInitView(savedInstanceState);

        ThemeHelper themeHelper = new ThemeHelper(getContext());
        bottomNav
                .addItem(new BottomNavigationItem(new IconicsDrawable(getContext(),CommunityMaterial.Icon.cmd_format_float_left).paddingDp(5), "Home"))
                .addItem(new BottomNavigationItem(new IconicsDrawable(getContext(),CommunityMaterial.Icon.cmd_view_module).paddingDp(5), "Subscriptions"))
                .addItem(new BottomNavigationItem(new IconicsDrawable(getContext(),CommunityMaterial.Icon.cmd_earth).paddingDp(5), "Discover"))
                .addItem(new BottomNavigationItem(new IconicsDrawable(getContext(),CommunityMaterial.Icon.cmd_download).paddingDp(5), "Downloads"))
                .addItem(new BottomNavigationItem(new IconicsDrawable(getContext(),CommunityMaterial.Icon.cmd_account).paddingDp(5), "Me"))
                .setMode(BottomNavigationBar.MODE_FIXED_NO_TITLE)
                .setActiveColor(themeHelper.getThemeColor(R.color.colorPrimary))
                .setInActiveColor(themeHelper.getThemeColor(R.color.bottomNavDefault))
                .setBarBackgroundColor(themeHelper.getThemeColor(R.color.bottomNavSelected))
                .setTabSelectedListener(new BottomNavigationBar.OnTabSelectedListener() {
                    @Override
                    public void onTabSelected(int position) {
                        switch (position){
                            case 0:
                                showHideFragment(fragments.get(0));
                                break;
                            case 1:
                                showHideFragment(fragments.get(1));
                                break;
                            case 2:
                                showHideFragment(fragments.get(2));
                                break;
                            case 3:
                                showHideFragment(fragments.get(3));
                                break;
                            case 4:
                                showHideFragment(fragments.get(4));
                                break;
                        }
                    }

                    @Override
                    public void onTabUnselected(int position) {

                    }

                    @Override
                    public void onTabReselected(int position) {
                        switch (position){
                            case 0:
                                ((TimelineFragment)fragments.get(0)).scrollTop();
                                break;
                            case 1:
                                ((SubscriptionsFragment)fragments.get(1)).scrollTop();
                                break;
                            case 2:
                                ((SearchFragment)fragments.get(2)).scrollTop();
                                break;
                            case 3:
                                ((DownloadsFragment)fragments.get(3)).scrollTop();
                                break;
                        }
                    }
                })
                .initialise();
    }

    public void changeIndex(int i){
        bottomNav.selectTab(i);
    }
}

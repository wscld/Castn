package com.wslclds.castn.fragments;


import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.customtabs.CustomTabsIntent;
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

import com.wslclds.castn.R;
import com.wslclds.castn.builders.AlertBuilder;
import com.wslclds.castn.builders.AlertWithInputBuilder;
import com.wslclds.castn.builders.AlertWithListBuilder;
import com.wslclds.castn.factory.objects.ToolbarEvent;
import com.wslclds.castn.helpers.Helper;
import com.wslclds.castn.helpers.ThemeHelper;
import com.wslclds.castn.items.MenuItem;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.yokeyword.fragmentation.SupportFragment;

/**
 * A simple {@link Fragment} subclass.
 */
public class SettingsFragment extends SupportFragment {

    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    ItemAdapter itemAdapter;

    public static SettingsFragment newInstance() {

        Bundle args = new Bundle();

        SettingsFragment fragment = new SettingsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public SettingsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        ButterKnife.bind(this,view);
        return view;
    }

    @Override
    public void onLazyInitView(@Nullable Bundle savedInstanceState) {
        super.onLazyInitView(savedInstanceState);

        ThemeHelper themeHelper = new ThemeHelper(getContext());
        Helper helper = new Helper(getContext());

        recyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));
        itemAdapter = new ItemAdapter();
        FastAdapter fastAdapter = FastAdapter.with(itemAdapter);
        recyclerView.setAdapter(fastAdapter);

        MenuItem i1 = new MenuItem("Theme",new IconicsDrawable(getContext(),CommunityMaterial.Icon.cmd_palette).color(Color.WHITE).paddingDp(9),false);
        itemAdapter.add(i1);

        MenuItem i2 = new MenuItem("Automatic download",new IconicsDrawable(getContext(),CommunityMaterial.Icon.cmd_download).color(Color.WHITE).paddingDp(9),true);
        i2.setSwitchValue(helper.isAutomaticDownload());
        itemAdapter.add(i2);

        MenuItem i3 = new MenuItem("Backup Manager",new IconicsDrawable(getContext(),CommunityMaterial.Icon.cmd_backup_restore).color(Color.WHITE).paddingDp(9));
        itemAdapter.add(i3);

        MenuItem i4 = new MenuItem("Licenses",new IconicsDrawable(getContext(),CommunityMaterial.Icon.cmd_format_list_bulleted).color(Color.WHITE).paddingDp(9));
        itemAdapter.add(i4);

        MenuItem i5 = new MenuItem("Feedback",new IconicsDrawable(getContext(),CommunityMaterial.Icon.cmd_message_draw).color(Color.WHITE).paddingDp(9));
        itemAdapter.add(i5);

        MenuItem i6 = new MenuItem("About",new IconicsDrawable(getContext(),CommunityMaterial.Icon.cmd_information_variant).color(Color.WHITE).paddingDp(9));
        itemAdapter.add(i6);


        fastAdapter.withOnClickListener(new OnClickListener() {
            @Override
            public boolean onClick(@javax.annotation.Nullable View v, IAdapter adapter, IItem item, int position) {
                if(position == 0){
                    ArrayList<IItem> items = new ArrayList<>();
                    MenuItem ai1 = new MenuItem("Light",new IconicsDrawable(getContext(),CommunityMaterial.Icon.cmd_circle).color(getResources().getColor(R.color.colorPrimary)).paddingDp(0),false);
                    items.add(ai1);
                    MenuItem ai2 = new MenuItem("Dark",new IconicsDrawable(getContext(),CommunityMaterial.Icon.cmd_circle).color(getResources().getColor(R.color.darkThemeColorPrimary)).paddingDp(0),false);
                    items.add(ai2);
                    MenuItem ai3 = new MenuItem("Dark OLED",new IconicsDrawable(getContext(),CommunityMaterial.Icon.cmd_circle).color(getResources().getColor(R.color.AMOLEDThemeColorPrimary)).paddingDp(0),false);
                    items.add(ai3);

                    new AlertWithListBuilder(getContext(), items, null, null, false, new AlertWithListBuilder.OnAction() {
                        @Override
                        public void onClick(IItem item, int position) {
                            if(position == 0){
                                themeHelper.setTheme(ThemeHelper.THEME_LIGHT);
                            }else if(position == 1){
                                themeHelper.setTheme(ThemeHelper.THEME_DARK);
                            }else{
                                themeHelper.setTheme(ThemeHelper.THEME_AMOLED);
                            }
                            getActivity().recreate();
                        }
                    }).show();
                }else if(position == 1){
                    if(!helper.isAutomaticDownload()) {
                        new AlertBuilder(getContext(), "Enable automatic downloads?", "This will automatically download new episodes when available. This may cause high network usage.", new AlertBuilder.onButtonClick2() {
                            @Override
                            public void onConfirm() {
                                helper.setAutomaticDownload(true);
                                i2.setSwitchValue(helper.isAutomaticDownload());
                                fastAdapter.notifyAdapterItemChanged(1);
                            }

                            @Override
                            public void onCancel() {

                            }
                        }).show();
                    }else{
                        helper.setAutomaticDownload(false);
                    }
                    i2.setSwitchValue(helper.isAutomaticDownload());
                    fastAdapter.notifyAdapterItemChanged(1);
                }else if(position == 2){
                    start(BackupFragment.newInstance());
                }else if(position == 3){
                    start(LicensesFragment.newInstance());
                }else if(position == 4){
                    CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
                    CustomTabsIntent customTabsIntent = builder.build();
                    customTabsIntent.launchUrl(getContext(), Uri.parse("https://docs.google.com/forms/d/e/1FAIpQLSeo5IvSkT5hB14aSHin6dnJ5YhDLUxqH-fY7VRpKAD3J0O-tg/viewform?usp=sf_link"));
                }else if(position == 5){
                    start(AboutFragment.newInstance());
                }

                return true;
            }
        });
    }

    @Override
    public void onSupportVisible() {
        super.onSupportVisible();
        ToolbarEvent toolbarEvent = new ToolbarEvent();
        toolbarEvent.setTitle("Settings");
        toolbarEvent.setHome(false);
        EventBus.getDefault().post(toolbarEvent);
    }
}

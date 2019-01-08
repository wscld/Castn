package com.wslclds.castn.fragments;


import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
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
import com.wslclds.castn.R;
import com.wslclds.castn.activities.MainActivity;
import com.wslclds.castn.builders.AlertBuilder;
import com.wslclds.castn.factory.DatabaseBackupManager;
import com.wslclds.castn.factory.DatabaseManager;
import com.wslclds.castn.helpers.Helper;
import com.wslclds.castn.items.MenuItem;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.Permission;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;
import me.yokeyword.fragmentation.SupportFragment;

/**
 * A simple {@link Fragment} subclass.
 */
public class BackupFragment extends SupportFragment{

    private static final int PICKFILE_RESULT_CODE = 1;
    private DatabaseBackupManager databaseBackupManager;
    private DatabaseManager databaseManager;

    ItemAdapter itemAdapter;
    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;

    public BackupFragment() {
        // Required empty public constructor
    }

    public static BackupFragment newInstance() {
        Bundle args = new Bundle();
        BackupFragment fragment = new BackupFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_backup, container, false);
        ButterKnife.bind(this,view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        databaseManager = new DatabaseManager(getContext());
        databaseBackupManager = new DatabaseBackupManager(getContext());

        recyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));
        itemAdapter = new ItemAdapter();
        FastAdapter fastAdapter = FastAdapter.with(itemAdapter);
        recyclerView.setAdapter(fastAdapter);

        fastAdapter.withOnClickListener(new OnClickListener() {
            @Override
            public boolean onClick(@javax.annotation.Nullable View v, IAdapter adapter, IItem item, int position) {
                if(position == 0){
                    if (android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
                        AndPermission.with(getContext())
                                .runtime()
                                .permission(Permission.Group.STORAGE)
                                .onGranted(permissions -> {
                                    databaseBackupManager.backup();
                                    new AlertBuilder(getContext(), "Success", "Backup data stored in" + databaseBackupManager.getStorageFolder()).show();
                                })
                                .onDenied(permissions -> {
                                    new AlertBuilder(getContext(), "Permission denied", "Storage permission is needed").show();
                                })
                                .start();
                    }else{
                        databaseBackupManager.backup();
                        new AlertBuilder(getContext(), "Success", "Backup data stored in" + databaseBackupManager.getStorageFolder()).show();
                    }
                }else if(position == 1){
                    if (android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
                        AndPermission.with(getContext())
                                .runtime()
                                .permission(Permission.Group.STORAGE)
                                .onGranted(permissions -> {
                                    databaseBackupManager.restore();
                                    new AlertBuilder(getContext(), "Success", "Please restart the app to apply the changes", new AlertBuilder.onButtonClick() {
                                        @Override
                                        public void onNeutral() {

                                        }
                                    }).show();
                                })
                                .onDenied(permissions -> {
                                    new AlertBuilder(getContext(), "Permission denied", "Storage permission is needed").show();
                                })
                                .start();
                    }else {
                        databaseBackupManager.restore();
                        new AlertBuilder(getContext(), "Success", "Please restart the app to apply the changes", new AlertBuilder.onButtonClick() {
                            @Override
                            public void onNeutral() {

                            }
                        }).show();
                    }
                }else if(position == 2){
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setType("file/*");
                    startActivityForResult(intent,PICKFILE_RESULT_CODE);
                }
                return true;
            }
        });

        loadItems();
    }

    private void restartApp() {
        Intent intent = new Intent(getContext().getApplicationContext(), MainActivity.class);
        int mPendingIntentId = 1111;
        PendingIntent mPendingIntent = PendingIntent.getActivity(getContext().getApplicationContext(), mPendingIntentId, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager mgr = (AlarmManager) getContext().getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent);
        System.exit(0);
    }

    private void loadItems(){
        MenuItem i1 = new MenuItem("Backup",new IconicsDrawable(getContext()).icon(CommunityMaterial.Icon.cmd_content_save).color(Color.WHITE).paddingDp(9));
        itemAdapter.add(i1);
        MenuItem i2 = new MenuItem("Restore",new IconicsDrawable(getContext()).icon(CommunityMaterial.Icon.cmd_backup_restore).color(Color.WHITE).paddingDp(9));
        itemAdapter.add(i2);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode){
            case PICKFILE_RESULT_CODE:
                if(resultCode==RESULT_OK){
                    String filePath = data.getData().getPath();
                    new Helper(getContext()).startOPMLImport(filePath);
                }
                break;

        }
    }

    @Override
    public void onSupportVisible() {
        super.onSupportVisible();
    }
}

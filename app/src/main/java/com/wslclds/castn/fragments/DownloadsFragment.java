package com.wslclds.castn.fragments;


import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.ColorStateList;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.IItem;
import com.mikepenz.fastadapter.adapters.ItemAdapter;
import com.mikepenz.fastadapter.listeners.ClickEventHook;
import com.mikepenz.fastadapter.listeners.OnBindViewHolderListener;
import com.mikepenz.iconics.IconicsDrawable;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.wslclds.castn.builders.PopUpMenuBuilder;
import com.wslclds.castn.helpers.ThemeHelper;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.wslclds.castn.activities.EpisodeDetailActivity;
import com.wslclds.castn.activities.MainActivity;
import com.wslclds.castn.factory.DatabaseManager;
import com.wslclds.castn.factory.objects.Download;
import com.wslclds.castn.factory.objects.ToolbarEvent;
import com.wslclds.castn.GlideApp;
import com.wslclds.castn.helpers.Helper;
import com.wslclds.castn.builders.AlertBuilder;
import com.wslclds.castn.items.DownloadItem;
import com.wslclds.castn.items.TextHeaderItem;
import com.wslclds.castn.R;
import com.wslclds.castn.items.TimelineItem;
import com.wslclds.castn.services.DownloadService;
import me.yokeyword.fragmentation.SupportFragment;

import static android.content.Context.BIND_AUTO_CREATE;

/**
 * A simple {@link Fragment} subclass.
 */
public class DownloadsFragment extends SupportFragment {

    private int EPISODE_DETAIL_REQUEST_ID = 1;

    boolean isBound = false;
    int prevPercentage;
    private DownloadService downloadService;
    ItemAdapter itemAdapterQueue;
    ItemAdapter itemAdapterDownloaded;
    ItemAdapter itemAdapterHeader1;
    ItemAdapter itemAdapterHeader2;
    FastAdapter fastAdapter;
    DatabaseManager databaseManager;
    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    @BindView(R.id.title)
    TextView downloadTitle;
    @BindView(R.id.progress)
    ProgressBar downloadProgress;
    @BindView(R.id.image)
    ImageView downloadImage;
    @BindView(R.id.optionsButton)
    ImageButton optionsButton;
    @BindView(R.id.currentDownloadLayout)
    RelativeLayout currentDownloadLayout;

    public static DownloadsFragment newInstance() {

        Bundle args = new Bundle();

        DownloadsFragment fragment = new DownloadsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public DownloadsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_downloads, container, false);
        ButterKnife.bind(this,view);
        databaseManager = new DatabaseManager(getContext());
        return view;
    }

    @Override
    public void onLazyInitView(@Nullable Bundle savedInstanceState) {
        super.onLazyInitView(savedInstanceState);
        recyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));
        itemAdapterQueue = new ItemAdapter();
        itemAdapterDownloaded = new ItemAdapter();
        itemAdapterHeader1 = new ItemAdapter();
        itemAdapterHeader2 = new ItemAdapter();
        fastAdapter = FastAdapter.with(new ArrayList(Arrays.asList(itemAdapterHeader1,itemAdapterQueue,itemAdapterHeader2,itemAdapterDownloaded)));
        recyclerView.setAdapter(fastAdapter);

        fastAdapter.withEventHook(new ClickEventHook() {
            @javax.annotation.Nullable
            @Override
            public List<View> onBindMany(RecyclerView.ViewHolder viewHolder) {
                if (viewHolder instanceof DownloadItem.ViewHolder) {
                    List<View> list = new ArrayList<>();
                    list.add(((DownloadItem.ViewHolder) viewHolder).optionsButton);
                    list.add(((DownloadItem.ViewHolder) viewHolder).mainLayout);
                    return list;
                }
                return null;
            }

            @Override
            public void onClick(View v, int position, FastAdapter fastAdapter, IItem item) {
                if(v.getId() == R.id.mainLayout){
                    Intent intent = new Intent(getContext(),EpisodeDetailActivity.class);
                    intent.putExtra("episode",((DownloadItem)item).getDownload().getEpisodeJson());
                    startActivityForResult(intent,EPISODE_DETAIL_REQUEST_ID);
                }else if(v.getId() == R.id.optionsButton){
                    new PopUpMenuBuilder(getContext(), v, new ArrayList<String>(Arrays.asList("Remove")), new PopUpMenuBuilder.WithOnClickListener() {
                        @Override
                        public void onClickListener(int position) {
                            new AlertBuilder(getContext(), "Delete download?", null, new AlertBuilder.onButtonClick() {
                                @Override
                                public void onNeutral() {
                                    DownloadItem downloadItem = (DownloadItem)item;
                                    new Helper(getContext()).deleteDownload(downloadItem.getDownload());
                                    databaseManager.removeDownload(downloadItem.getDownload().getEnclosureUrl());
                                    if(isBound){
                                        downloadService.updateQueue();
                                    }
                                }
                            }).show();
                        }
                    }).showMenu();
                }
            }
        });

        currentDownloadLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isBound){
                    Intent intent = new Intent(getContext(),EpisodeDetailActivity.class);
                    intent.putExtra("episode", downloadService.getCurrentDownload().getEpisodeJson());
                    startActivityForResult(intent,EPISODE_DETAIL_REQUEST_ID);
                }
            }
        });

        optionsButton.setImageDrawable(new IconicsDrawable(getContext(),CommunityMaterial.Icon.cmd_close));
        optionsButton.setBackgroundTintList(ColorStateList.valueOf(new ThemeHelper(getContext()).getTextColor()));
        optionsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isBound){
                    AlertBuilder alertBuilder = new AlertBuilder(getContext(), "Stop download?", null, new AlertBuilder.onButtonClick() {
                        @Override
                        public void onNeutral() {
                            if(isBound){
                                downloadService.stopDownload();
                            }
                        }
                    });
                    alertBuilder.show();
                }
            }
        });

        itemAdapterHeader1.add(new TextHeaderItem("Download Queue",new IconicsDrawable(getContext(),CommunityMaterial.Icon.cmd_view_list)));
        itemAdapterHeader2.add(new TextHeaderItem("Downloaded",new IconicsDrawable(getContext(),CommunityMaterial.Icon.cmd_download)));


        fastAdapter.withEventHook(new ClickEventHook() {
            @javax.annotation.Nullable
            @Override
            public View onBind(RecyclerView.ViewHolder viewHolder) {
                if (viewHolder instanceof TextHeaderItem.ViewHolder) {
                    return ((TextHeaderItem.ViewHolder) viewHolder).button;
                }
                return null;
            }

            @Override
            public void onClick(View v, int position, FastAdapter fastAdapter, IItem item) {
                new AlertBuilder(getContext(), "Clear download queue?", null, new AlertBuilder.onButtonClick2() {
                    @Override
                    public void onConfirm() {
                        if(isBound){
                            for(Download download : downloadService.getDownloadQueue()) {
                                if(!download.getEnclosureUrl().equals(downloadService.getCurrentDownload().getEnclosureUrl())){
                                    databaseManager.removeDownload(download.getEnclosureUrl());
                                }
                            }
                            downloadService.updateQueue();
                        }
                    }

                    @Override
                    public void onCancel() {

                    }
                }).show();
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

    private void loadDownloadQueue(){
        if(isBound && itemAdapterQueue != null){
            itemAdapterQueue.clear();

            ArrayList<Download> downloadQueue = downloadService.getDownloadQueue();
            if(downloadQueue.size() > 1){
                itemAdapterHeader1.set(0,new TextHeaderItem("Download Queue","clear",new IconicsDrawable(getContext(),CommunityMaterial.Icon.cmd_view_list)));
            }else{
                itemAdapterHeader1.set(0,new TextHeaderItem("Download Queue",new IconicsDrawable(getContext(),CommunityMaterial.Icon.cmd_view_list)));
            }

            for(int i = 0; i < downloadQueue.size(); i++){
                if(i > 0){
                    DownloadItem downloadItem = new DownloadItem(downloadQueue.get(i));
                    downloadItem.setProgressVisible(true);
                    itemAdapterQueue.add(downloadItem);
                }
            }
        }
        loadDownloaded();
    }

    private void loadDownloaded(){
        if(itemAdapterDownloaded != null){
            itemAdapterDownloaded.clear();

            ArrayList<Download> downloads = databaseManager.getDownloaded();
            for(int i = 0; i < downloads.size(); i++){
                itemAdapterDownloaded.add(new DownloadItem(downloads.get(i)));
            }
        }
    }

    //download binding
    private void makeBind(){
        Intent intent = new Intent(getContext() , DownloadService.class);
        getContext().startService(intent);
        getContext().bindService(intent , boundServiceConnection,BIND_AUTO_CREATE);
    }
    private void removeBind(){
        if(isBound){
            getContext().unbindService(boundServiceConnection);
            isBound = false;
        }
    }

    private ServiceConnection boundServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {

            DownloadService.DownloadServiceBinder binderBridge = (DownloadService.DownloadServiceBinder) service ;
            downloadService = binderBridge.getService();
            isBound = true;
            loadDownloadQueue();
            setupUI();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBound = false;
            downloadService = null;
        }
    };

    private void setupUI(){
        if(isBound){
            DownloadService.DownloadListener downloadListener = new DownloadService.DownloadListener() {
                @Override
                public void onProgress(String uri, long totalBytes, long downloadedBytes) {
                    setupCurrentDownload(totalBytes, downloadedBytes);
                }

                @Override
                public void onDownloadFailed(String uri, int errorCode, String errorMessage) {
                    AlertBuilder alertBuilder = new AlertBuilder(getContext(), "Download failed", errorMessage);
                    alertBuilder.show();
                    setupCurrentDownload(0, 0);
                }

                @Override
                public void onDownloadComplete(String uri) {
                    setupCurrentDownload(0, 0);
                }

                @Override
                public void onDownloadQueueUpdate() {
                    setupCurrentDownload(0, 0);
                    loadDownloadQueue();
                }
            };
            downloadService.getDownloadListener(downloadListener);
        }
    }

    private void setupCurrentDownload(long totalBytes, long downloadedBytes){
        Download download = downloadService.getCurrentDownload();
        if(download != null && isBound) {
            int percentage = Math.round(((float)downloadedBytes/totalBytes)*100);
            int max = 100;

            downloadProgress.setMax(max);

            currentDownloadLayout.setVisibility(View.VISIBLE);
            if(!download.getTitle().equals(downloadTitle.getText())){
                downloadTitle.setText(download.getTitle());
                GlideApp.with(getContext()).load(download.getImage()).into(downloadImage);
            }

            if(percentage != prevPercentage){
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    downloadProgress.setProgress(percentage,true);
                }else {
                    downloadProgress.setProgress(percentage);
                }
                prevPercentage = percentage;
            }
        }else {
            currentDownloadLayout.setVisibility(View.GONE);
        }
    }

    public void scrollTop(){
        recyclerView.smoothScrollToPosition(0);
    }


    @Override
    public void onSupportVisible() {
        super.onSupportVisible();
        ToolbarEvent toolbarEvent = new ToolbarEvent();
        toolbarEvent.setTitle("Downloads");
        toolbarEvent.setHome(true);
        EventBus.getDefault().post(toolbarEvent);

        if(!isBound){
            makeBind();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        removeBind();
    }
}

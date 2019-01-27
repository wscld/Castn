package com.wslclds.castn.items;

import android.os.Environment;
import android.os.StatFs;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.items.AbstractItem;
import com.wslclds.castn.R;
import com.wslclds.castn.helpers.Helper;

import java.io.File;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class StorageStatsItem extends AbstractItem<StorageStatsItem, StorageStatsItem.ViewHolder> {
    @NonNull
    @Override
    public StorageStatsItem.ViewHolder getViewHolder(View v) {
        return new ViewHolder(v);
    }

    @Override
    public int getType() {
        return R.id.storage_stats_item_id;
    }

    @Override
    public int getLayoutRes() {
        return R.layout.item_storage_stats;
    }

    public class ViewHolder extends FastAdapter.ViewHolder<StorageStatsItem> {
        @BindView(R.id.podcastsSize)
        TextView podcastsSize;
        @BindView(R.id.episodeCount)
        TextView episodeCount;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this,itemView);
        }

        @Override
        public void bindView(StorageStatsItem item, List<Object> payloads) {
            File mainDir = Environment.getExternalStorageDirectory();
            File castnFolder = new File(mainDir,"Castn");
            File podcastsFolder = new File(castnFolder,"Podcasts");
            long folderSize = getFolderSize(podcastsFolder);
            int epsCount = 0;
            if(podcastsFolder.exists() && podcastsFolder.isDirectory()){
                epsCount = podcastsFolder.listFiles().length;
            }
            podcastsSize.setText(Helper.bytesIntoHumanReadable(folderSize));
            if(epsCount == 1){
                episodeCount.setText(epsCount +" Episode");
            }else {
                episodeCount.setText(epsCount +" Episodes");
            }
        }

        @Override
        public void unbindView(StorageStatsItem item) {

        }

        private long getFolderSize(File folder){
            long size = 0;
            if(folder.exists() && folder.isDirectory()){
                for(File f : folder.listFiles()){
                    if(f.isFile()){
                        size+=f.length();
                    }
                }
            }
            return size;
        }
    }
}

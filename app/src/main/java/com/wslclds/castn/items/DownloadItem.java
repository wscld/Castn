package com.wslclds.castn.items;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.items.AbstractItem;
import com.mikepenz.iconics.IconicsDrawable;

import java.util.List;

import com.wslclds.castn.factory.objects.Episode;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.wslclds.castn.factory.objects.Download;
import com.wslclds.castn.GlideApp;
import com.wslclds.castn.R;

public class DownloadItem extends AbstractItem<DownloadItem, DownloadItem.ViewHolder> {
    private int progress;
    private Download download;
    private boolean progressVisible;

    public DownloadItem(Download download) {
        this.download = download;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public void setProgressVisible(boolean progressVisible) {
        this.progressVisible = progressVisible;
    }

    public Download getDownload() {
        return download;
    }

    public int getProgress() {
        return progress;
    }

    @NonNull
    @Override
    public ViewHolder getViewHolder(View v) {
        return new ViewHolder(v);
    }

    @Override
    public int getType() {
        return R.id.download_item_id;
    }

    @Override
    public int getLayoutRes() {
        return R.layout.item_download;
    }

    public class ViewHolder extends FastAdapter.ViewHolder<DownloadItem> {
        @BindView(R.id.title)
        TextView title;
        @BindView(R.id.image)
        ImageView image;
        @BindView(R.id.progress)
        ProgressBar progressBar;
        @BindView(R.id.description)
        TextView description;
        @BindView(R.id.optionsButton)
        public ImageButton optionsButton;
        @BindView(R.id.mainLayout)
        public RelativeLayout mainLayout;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this,itemView);
        }

        @Override
        public void bindView(DownloadItem item, List payloads) {
            optionsButton.setImageDrawable(new IconicsDrawable(itemView.getContext(),CommunityMaterial.Icon.cmd_dots_vertical));
            title.setText(item.download.getTitle());
            GlideApp.with(itemView.getContext()).load(item.download.getImage()).override(200,200).thumbnail(0.3f).placeholder(R.drawable.castn_icon_2).centerCrop().into(image);
            if(item.progress > 0){
                progressBar.setIndeterminate(false);
                progressBar.setProgress(item.progress);
            }else {
                progressBar.setIndeterminate(true);
            }
            if(item.progressVisible){
                progressBar.setVisibility(View.VISIBLE);
                description.setVisibility(View.GONE);
            }else {
                progressBar.setVisibility(View.GONE);
                description.setVisibility(View.VISIBLE);
            }

            @SuppressLint("StaticFieldLeak") AsyncTask asyncTask = new AsyncTask() {
                String descriptionText;
                @Override
                protected Object doInBackground(Object[] objects) {
                    descriptionText = new Gson().fromJson(item.download.getEpisodeJson(),Episode.class).getPlainDescription();
                    if(descriptionText.length() > 200){
                        descriptionText = descriptionText.substring(0,200);
                    }
                    return null;
                }

                @Override
                protected void onPostExecute(Object o) {
                    description.setText(descriptionText);
                }
            };
            asyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }

        @Override
        public void unbindView(DownloadItem item) {

        }
    }
}

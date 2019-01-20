package com.wslclds.castn.items;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.items.AbstractItem;

import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import com.wslclds.castn.factory.DatabaseManager;
import com.wslclds.castn.factory.objects.Episode;
import com.wslclds.castn.GlideApp;
import com.wslclds.castn.R;
import com.wslclds.castn.helpers.Helper;

public class EpisodeItem extends AbstractItem<EpisodeItem, EpisodeItem.ViewHolder> {
    private Episode episode;
    private int progress;
    private int total;

    public EpisodeItem(Episode episode){
        this.episode = episode;
        this.progress = 0;
        this.total = 0;
    }
    public EpisodeItem(Episode episode, int progress, int total){
        this.episode = episode;
        this.total = total;
        this.progress = progress;
    }

    public Episode getEpisode() {
        return episode;
    }

    @NonNull
    @Override
    public ViewHolder getViewHolder(View v) {
        return new ViewHolder(v);
    }

    @Override
    public int getType() {
        return R.id.episode_item_id;
    }

    @Override
    public int getLayoutRes() {
        return R.layout.item_episode;
    }

    public class ViewHolder extends FastAdapter.ViewHolder<EpisodeItem> {
        @BindView(R.id.episodeTitle)
        TextView title;
        @BindView(R.id.description)
        TextView description;
        @BindView(R.id.date)
        TextView date;
        @BindView(R.id.duration)
        TextView duration;
        @BindView(R.id.image)
        ImageView image;
        @BindView(R.id.progress)
        ProgressBar progressBar;
        @BindView(R.id.listenedIcon)
        CardView listenedIcon;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this,itemView);
        }

        @Override
        public void bindView(final EpisodeItem item, List<Object> payloads) {
            if(item.episode != null){
                String episodeTitle = item.episode.getTitle();
                String episodeImage = item.episode.getImage();

                title.setText(episodeTitle);
                GlideApp.with(itemView.getContext()).asBitmap().load(episodeImage).override(300,300).centerCrop().thumbnail(0.3f).placeholder(R.drawable.castn_icon_2).into(image);

                if(item.progress > 0){
                    progressBar.setVisibility(View.VISIBLE);
                    progressBar.setMax(item.total);
                    progressBar.setProgress(item.progress);
                }else {
                    progressBar.setVisibility(View.GONE);
                }

                @SuppressLint("StaticFieldLeak") AsyncTask asyncTask = new AsyncTask() {
                    private String episodeDuration;
                    private String episodeDate;
                    String episodeDescription;
                    boolean listened;

                    @Override
                    protected Object doInBackground(Object[] objects) {
                        DatabaseManager databaseManager = new DatabaseManager(itemView.getContext());

                        listened = databaseManager.isEpisodeListened(item.episode.getEnclosureUrl());
                        episodeDescription = item.episode.getPlainDescription();
                        if(episodeDescription.length() > 400){
                            episodeDescription = episodeDescription.substring(0,400)+"...";
                        }
                        episodeDate = (DateUtils.getRelativeTimeSpanString(item.episode.getPubDate(),new Date().getTime(),DateUtils.MINUTE_IN_MILLIS)).toString();
                        episodeDuration = Helper.formatDuration(item.episode.getDuration());
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Object o) {
                        duration.setText(episodeDuration);
                        date.setText(episodeDate);
                        description.setText(episodeDescription);
                        if(listened){
                            listenedIcon.setVisibility(View.VISIBLE);
                        }else {
                            listenedIcon.setVisibility(View.GONE);
                        }
                    }
                };
                asyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        }

        @Override
        public void unbindView(EpisodeItem item) {

        }
    }
}

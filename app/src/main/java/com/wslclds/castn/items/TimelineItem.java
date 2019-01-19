package com.wslclds.castn.items;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.CardView;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.items.AbstractItem;

import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import com.wslclds.castn.factory.DatabaseManager;
import com.wslclds.castn.factory.objects.Episode;
import com.wslclds.castn.factory.objects.Podcast;
import com.wslclds.castn.factory.objects.PodcastAndEpisode;
import com.wslclds.castn.GlideApp;
import com.wslclds.castn.helpers.Helper;
import com.wslclds.castn.R;

public class TimelineItem extends AbstractItem<TimelineItem,TimelineItem.ViewHolder> {

    PodcastAndEpisode podcastAndEpisode;

    public TimelineItem (PodcastAndEpisode podcastAndEpisode){
        this.podcastAndEpisode  = podcastAndEpisode;
    }

    public PodcastAndEpisode getPodcastAndEpisode() {
        return podcastAndEpisode;
    }

    @NonNull
    @Override
    public ViewHolder getViewHolder(View v) {
        return new ViewHolder(v);
    }

    @Override
    public int getType() {
        return R.id.timeline_item_id;
    }

    @Override
    public int getLayoutRes() {
        return R.layout.item_timeline;
    }

    public class ViewHolder extends FastAdapter.ViewHolder<TimelineItem> {
        @BindView(R.id.podcastTitle)
        TextView pdTitle;
        @BindView(R.id.episodeTitle)
        TextView epTitle;
        @BindView(R.id.description)
        TextView description;
        @BindView(R.id.podcastImage)
        ImageView pdImage;
        @BindView(R.id.episodeImage)
        ImageView epImage;
        @BindView(R.id.episodeImageCard)
        CardView epImageCard;
        @BindView(R.id.date)
        TextView date;
        @BindView(R.id.duration)
        TextView duration;
        @BindView(R.id.podcastLayout)
        public LinearLayout podcastLayout;
        @BindView(R.id.episodeLayout)
        public LinearLayout episodeLayout;
        @BindView(R.id.mainCard)
        CardView mainCard;
        @BindView(R.id.listenedIcon)
        CardView listenedIcon;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this,itemView);
        }

        @Override
        public void bindView(final TimelineItem item, List<Object> payloads) {

            Episode episode = item.podcastAndEpisode.getEpisode();
            Podcast podcast = item.podcastAndEpisode.getPodcast();

            @SuppressLint("StaticFieldLeak") AsyncTask asyncTask = new AsyncTask() {
                private String episodeDuration;
                private String episodeDate;
                String episodeDescription;
                boolean listened;

                @Override
                protected Object doInBackground(Object[] objects) {
                    DatabaseManager databaseManager = new DatabaseManager(itemView.getContext());

                    listened = databaseManager.isEpisodeListened(episode.getEnclosureUrl());
                    episodeDescription = episode.getPlainDescription();
                    if(episodeDescription.length() > 400){
                        episodeDescription = episodeDescription.substring(0,400)+"...";
                    }
                    episodeDate = (DateUtils.getRelativeTimeSpanString(episode.getPubDate(),new Date().getTime(),DateUtils.MINUTE_IN_MILLIS)).toString();
                    episodeDuration = Helper.formatDuration(episode.getDuration());
                    return null;
                }

                @Override
                protected void onPostExecute(Object o) {
                    duration.setText(episodeDuration);
                    date.setText(episodeDate);
                    if(episodeDescription.length() > 0){
                        description.setVisibility(View.VISIBLE);
                        description.setText(episodeDescription);
                    }else {
                        description.setVisibility(View.GONE);
                    }
                    if(listened){
                        listenedIcon.setVisibility(View.VISIBLE);
                    }else {
                        listenedIcon.setVisibility(View.GONE);
                    }
                }
            };
            asyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

            String episodeImage = episode.getImage();
            String podcastImage = podcast.getImage();
            String episodeTitle = episode.getTitle();
            String podcastTitle = podcast.getTitle();


            epTitle.setText(episodeTitle);
            pdTitle.setText(podcastTitle);
            GlideApp.with(itemView.getContext()).load(podcastImage).override(200,200).diskCacheStrategy(DiskCacheStrategy.ALL).centerCrop().thumbnail(0.4f).into(pdImage);
            if(!podcastImage.equals(episodeImage)){
                epImage.setVisibility(View.VISIBLE);
                epImageCard.setVisibility(View.VISIBLE);
                GlideApp.with(itemView.getContext()).load(episodeImage).diskCacheStrategy(DiskCacheStrategy.ALL).centerCrop().thumbnail(0.5f).into(epImage);
            }else {
                epImage.setVisibility(View.GONE);
                epImageCard.setVisibility(View.GONE);
            }

        }

        @Override
        public void unbindView(TimelineItem item) {
            epTitle.setText(null);
            pdTitle.setText(null);
            duration.setText(null);
            date.setText(null);
            description.setText(null);
            epImage.setImageBitmap(null);
            pdImage.setImageBitmap(null);
        }
    }
}

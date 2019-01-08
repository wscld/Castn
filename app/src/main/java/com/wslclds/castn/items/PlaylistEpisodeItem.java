package com.wslclds.castn.items;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.items.AbstractItem;

import java.util.Date;
import java.util.List;

import com.wslclds.castn.helpers.Helper;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.wslclds.castn.factory.objects.Episode;
import com.wslclds.castn.factory.objects.PlaylistEpisode;
import com.wslclds.castn.GlideApp;
import com.wslclds.castn.R;


public class PlaylistEpisodeItem extends AbstractItem<PlaylistEpisodeItem,PlaylistEpisodeItem.ViewHolder> {
    private PlaylistEpisode playlistEpisode;

    public PlaylistEpisodeItem(PlaylistEpisode playlistEpisode){
        this.playlistEpisode = playlistEpisode;
    }

    public PlaylistEpisode getPlaylistEpisode() {
        return playlistEpisode;
    }

    @NonNull
    @Override
    public ViewHolder getViewHolder(View v) {
        return new ViewHolder(v);
    }

    @Override
    public int getType() {
        return R.id.playlist_episode_item_id;
    }

    @Override
    public int getLayoutRes() {
        return R.layout.item_episode;
    }

    public class ViewHolder extends FastAdapter.ViewHolder<PlaylistEpisodeItem> {

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

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this,itemView);
        }

        @Override
        public void bindView(PlaylistEpisodeItem item, List<Object> payloads) {
            @SuppressLint("StaticFieldLeak") AsyncTask asyncTask = new AsyncTask() {
                Episode episode;
                @Override
                protected Object doInBackground(Object[] objects) {
                    episode = new Gson().fromJson(item.playlistEpisode.getEpisodeJson(),Episode.class);
                    return null;
                }

                @Override
                protected void onPostExecute(Object o) {
                    String episodeTitle = episode.getTitle();
                    String episodeDescription = episode.getPlainDescription();
                    String episodeDuration = Helper.formatDuration(episode.getDuration());
                    String epsodeDate = DateUtils.getRelativeTimeSpanString(episode.getPubDate(),new Date().getTime(),DateUtils.MINUTE_IN_MILLIS).toString();
                    String episodeImage = episode.getImage();

                    if(episodeDescription.length() > 400){
                        episodeDescription = episodeDescription.substring(0,400)+"...";
                    }

                    title.setText(episodeTitle);
                    description.setText(episodeDescription);
                    duration.setText(episodeDuration);
                    date.setText(epsodeDate);
                    GlideApp.with(itemView.getContext()).asBitmap().load(episodeImage).override(300,300).centerCrop().into(image);
                }
            };
            asyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }

        @Override
        public void unbindView(PlaylistEpisodeItem item) {

        }
    }
}

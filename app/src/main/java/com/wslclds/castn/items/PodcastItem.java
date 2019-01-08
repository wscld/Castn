package com.wslclds.castn.items;

import android.support.annotation.NonNull;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.items.AbstractItem;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import com.wslclds.castn.factory.objects.Podcast;
import com.wslclds.castn.GlideApp;
import com.wslclds.castn.R;

public class PodcastItem extends AbstractItem<PodcastItem,PodcastItem.ViewHolder> {
    private Podcast podcast;
    public PodcastItem(Podcast podcast){
        this.podcast = podcast;
    }

    public Podcast getPodcast() {
        return podcast;
    }

    @NonNull
    @Override
    public ViewHolder getViewHolder(View v) {
        return new ViewHolder(v);
    }

    @Override
    public int getType() {
        return R.id.podcast_item_id;
    }

    @Override
    public int getLayoutRes() {
        return R.layout.item_podcast;
    }

    public class ViewHolder extends FastAdapter.ViewHolder<PodcastItem> {
        @BindView(R.id.title)
        TextView title;
        @BindView(R.id.image)
        ImageView image;
        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this,itemView);
        }

        @Override
        public void bindView(PodcastItem item, List<Object> payloads) {
            title.setText(item.podcast.getTitle());
            GlideApp.with(itemView.getContext()).load(item.podcast.getImage()).into(image);
        }

        @Override
        public void unbindView(PodcastItem item) {

        }
    }
}

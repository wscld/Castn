package com.wslclds.castn.items;

import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.items.AbstractItem;
import com.wslclds.castn.R;
import com.wslclds.castn.factory.objects.Episode;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ResumeItem extends AbstractItem<ResumeItem, ResumeItem.ViewHolder> {
    private Episode episode;

    public ResumeItem(Episode episode){
        this.episode = episode;
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
        return R.id.resume_item_id;
    }

    @Override
    public int getLayoutRes() {
        return R.layout.item_resume;
    }

    public class ViewHolder extends FastAdapter.ViewHolder<ResumeItem> {
        @BindView(R.id.card)
        CardView card;
        @BindView(R.id.title)
        TextView title;
        @BindView(R.id.author)
        TextView author;
        @BindView(R.id.image)
        ImageView image;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this,itemView);
        }

        @Override
        public void bindView(ResumeItem item, List<Object> payloads) {
            title.setText(item.episode.getTitle());
            author.setText(item.episode.getPodcastTitle());
        }

        @Override
        public void unbindView(ResumeItem item) {

        }
    }
}

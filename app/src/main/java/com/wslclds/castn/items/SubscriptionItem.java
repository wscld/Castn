package com.wslclds.castn.items;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.graphics.Palette;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.items.AbstractItem;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import com.wslclds.castn.factory.DatabaseManager;
import com.wslclds.castn.helpers.Helper;
import com.wslclds.castn.factory.objects.Podcast;
import com.wslclds.castn.GlideApp;
import com.wslclds.castn.R;

public class SubscriptionItem extends AbstractItem<SubscriptionItem,SubscriptionItem.ViewHolder> {
    private Podcast podcast;

    public SubscriptionItem(Podcast podcast){
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
        return R.id.subscription_item_id;
    }

    @Override
    public int getLayoutRes() {
        return R.layout.item_subscription;
    }

    public class ViewHolder extends FastAdapter.ViewHolder<SubscriptionItem> {
        @BindView(R.id.image)
        ImageView image;
        @BindView(R.id.title)
        TextView title;
        @BindView(R.id.author)
        TextView author;
        @BindView(R.id.layout)
        LinearLayout layout;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this,itemView);
        }

        @Override
        public void bindView(SubscriptionItem item, List<Object> payloads) {
            title.setText(item.podcast.getTitle());
            author.setText(item.podcast.getAuthor());
            if(item.podcast.getColor() != 0) {
                GlideApp.with(itemView.getContext()).load(item.podcast.getImage()).diskCacheStrategy(DiskCacheStrategy.ALL).into(image);
                layout.setBackgroundColor(Helper.darker(item.podcast.getColor(), 0.7f));
            }else{
                GlideApp.with(itemView.getContext()).asBitmap().override(200,200).load(item.podcast.getImage()).diskCacheStrategy(DiskCacheStrategy.ALL).into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        image.setImageBitmap(resource);
                        Palette.from(resource).generate(new Palette.PaletteAsyncListener() {
                            @Override
                            public void onGenerated(@NonNull Palette palette) {
                                int color = palette.getDominantColor(itemView.getContext().getResources().getColor(R.color.colorPrimary));
                                int darker = Helper.darker(color,0.7f);
                                DatabaseManager databaseManager = new DatabaseManager(itemView.getContext());
                                databaseManager.setColor(item.podcast.getUrl(),color);
                                layout.setBackgroundColor(darker);
                                item.podcast.setColor(color);
                            }
                        });
                    }
                });
            }
        }

        @Override
        public void unbindView(SubscriptionItem item) {

        }
    }
}

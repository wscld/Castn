package com.wslclds.castn.items;

import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.IAdapter;
import com.mikepenz.fastadapter.IItem;
import com.mikepenz.fastadapter.adapters.ItemAdapter;
import com.mikepenz.fastadapter.items.AbstractItem;
import com.mikepenz.fastadapter.listeners.OnClickListener;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import com.wslclds.castn.fragments.PodcastFragment;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.wslclds.castn.R;
import com.wslclds.castn.factory.objects.Podcast;
import me.yokeyword.fragmentation.SupportFragment;

public class PodcastGroupItem extends AbstractItem<PodcastGroupItem,PodcastGroupItem.ViewHolder> {

    private ArrayList<Podcast> podcasts;
    private String title;
    private SupportFragment supportFragment;

    public PodcastGroupItem(SupportFragment supportFragment, ArrayList<Podcast> podcasts, String title){
        this.podcasts = podcasts;
        this.title = title;
        this.supportFragment = supportFragment;
    }

    @NonNull
    @Override
    public ViewHolder getViewHolder(View v) {
        return new ViewHolder(v);
    }

    @Override
    public int getType() {
        return R.id.podcast_group_item_id;
    }

    @Override
    public int getLayoutRes() {
        return R.layout.item_podcast_group;
    }

    public class ViewHolder extends FastAdapter.ViewHolder<PodcastGroupItem> {
        public ItemAdapter itemAdapter;
        public FastAdapter fastAdapter;
        @BindView(R.id.title)
        TextView titleView;
        @BindView(R.id.recyclerView)
        public RecyclerView recyclerView;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this,itemView);
        }

        @Override
        public void bindView(PodcastGroupItem item, List<Object> payloads) {
            titleView.setText(item.title);

            recyclerView.setLayoutManager(new LinearLayoutManager(itemView.getContext(),LinearLayoutManager.HORIZONTAL, false));
            itemAdapter = new ItemAdapter();
            fastAdapter = FastAdapter.with(itemAdapter);
            recyclerView.setAdapter(fastAdapter);

            fastAdapter.withOnClickListener(new OnClickListener() {
                @Override
                public boolean onClick(@Nullable View v, IAdapter adapter, IItem item, int position) {
                    PodcastGridItem podcastGridItem = (PodcastGridItem)item;
                    ((SupportFragment)supportFragment.getParentFragment()).start(PodcastFragment.newInstance(podcastGridItem.getPodcast().getUrl()));
                    return true;
                }
            });

            for(Podcast podcast : item.podcasts){
                PodcastGridItem podcastItem = new PodcastGridItem(podcast);
                itemAdapter.add(podcastItem);
            }
        }

        @Override
        public void unbindView(PodcastGroupItem item) {

        }
    }
}

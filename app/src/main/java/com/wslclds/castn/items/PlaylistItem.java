package com.wslclds.castn.items;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.items.AbstractItem;
import com.mikepenz.iconics.IconicsDrawable;

import java.util.List;

import com.wslclds.castn.helpers.ThemeHelper;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.wslclds.castn.factory.objects.Playlist;
import com.wslclds.castn.R;

public class PlaylistItem extends AbstractItem<PlaylistItem,PlaylistItem.ViewHolder> {
    Playlist playlist;

    public PlaylistItem(Playlist playlist){
        this.playlist = playlist;
    }

    public Playlist getPlaylist() {
        return playlist;
    }

    @NonNull
    @Override
    public ViewHolder getViewHolder(View v) {
        return new ViewHolder(v);
    }

    @Override
    public int getType() {
        return R.id.playlist_item_id;
    }

    @Override
    public int getLayoutRes() {
        return R.layout.item_playlist;
    }

    public class ViewHolder extends FastAdapter.ViewHolder<PlaylistItem> {
        @BindView(R.id.title)
        TextView title;
        @BindView(R.id.image)
        ImageView image;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this,itemView);
        }

        @Override
        public void bindView(PlaylistItem item, List<Object> payloads) {
            title.setText(item.playlist.getName());
            image.setImageDrawable(new IconicsDrawable(itemView.getContext()).icon(CommunityMaterial.Icon.cmd_playlist_play).color(Color.WHITE).paddingDp(9));
            //image.setColorFilter(new ThemeHelper(itemView.getContext()).getTextColor(), PorterDuff.Mode.SRC_ATOP);

        }

        @Override
        public void unbindView(PlaylistItem item) {

        }
    }
}

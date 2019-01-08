package com.wslclds.castn.items;

import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.items.AbstractItem;

import java.util.List;

import com.wslclds.castn.helpers.ThemeHelper;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.wslclds.castn.R;

public class TextHeaderItem  extends AbstractItem<TextHeaderItem,TextHeaderItem.ViewHolder> {

    private String text;
    private Drawable icon;

    public TextHeaderItem(String text, Drawable icon){
        this.text = text;
        this.icon = icon;
    }

    @NonNull
    @Override
    public ViewHolder getViewHolder(View v) {
        return new ViewHolder(v);
    }

    @Override
    public int getType() {
        return R.id.text_header_item_id;
    }

    @Override
    public int getLayoutRes() {
        return R.layout.item_text_header;
    }

    public class ViewHolder  extends FastAdapter.ViewHolder<TextHeaderItem> {
        @BindView(R.id.icon)
        ImageView icon;
        @BindView(R.id.text)
        TextView text;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this,itemView);
        }

        @Override
        public void bindView(TextHeaderItem item, List<Object> payloads) {
            icon.setImageDrawable(item.icon);
            icon.setColorFilter(new ThemeHelper(itemView.getContext()).getTextColor(), PorterDuff.Mode.SRC_ATOP);
            text.setText(item.text);
        }

        @Override
        public void unbindView(TextHeaderItem item) {

        }
    }
}

package com.wslclds.castn.items;

import android.support.annotation.NonNull;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.items.AbstractItem;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import com.wslclds.castn.R;

public class FooterItem extends AbstractItem<FooterItem,FooterItem.ViewHolder> {
    private String text;

    public FooterItem(String text){
        this.text = text;
    }

    @NonNull
    @Override
    public ViewHolder getViewHolder(View v) {
        return new ViewHolder(v);
    }

    @Override
    public int getType() {
        return R.id.load_more_item_id;
    }

    @Override
    public int getLayoutRes() {
        return R.layout.item_load_more;
    }

    public class ViewHolder extends FastAdapter.ViewHolder<FooterItem> {
        @BindView(R.id.loadMoreLayout)
        public LinearLayout loadMoreLayout;
        @BindView(R.id.footerButton)
        Button footerButton;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this,itemView);
        }

        @Override
        public void bindView(FooterItem item, List<Object> payloads) {
            footerButton.setText(item.text);
        }

        @Override
        public void unbindView(FooterItem item) {

        }
    }
}

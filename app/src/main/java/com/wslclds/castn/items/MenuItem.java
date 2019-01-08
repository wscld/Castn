package com.wslclds.castn.items;

import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.items.AbstractItem;

import java.util.List;

import com.wslclds.castn.helpers.ThemeHelper;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.wslclds.castn.R;

public class MenuItem extends AbstractItem<MenuItem, MenuItem.ViewHolder> {

    private String text;
    private Drawable image;
    private boolean showSwitch;
    private boolean switchValue;

    public MenuItem(String text, Drawable image){
        this.text = text;
        this.image = image;
    }

    public MenuItem(String text, Drawable image, boolean showSwitch){
        this.text = text;
        this.image = image;
        this.showSwitch = showSwitch;
    }

    public void setSwitchValue(boolean value){
        switchValue = value;
    }

    @NonNull
    @Override
    public ViewHolder getViewHolder(View v) {
        return new ViewHolder(v);
    }

    @Override
    public int getType() {
        return R.id.menu_item_id;
    }

    @Override
    public int getLayoutRes() {
        return R.layout.item_menu;
    }

    public class ViewHolder extends FastAdapter.ViewHolder<MenuItem> {
        @BindView(R.id.image)
        ImageView imageView;
        @BindView(R.id.text)
        TextView textView;
        @BindView(R.id.aSwitch)
        Switch aSwitch;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this,itemView);
        }

        @Override
        public void bindView(MenuItem item, List<Object> payloads) {
            imageView.setImageDrawable(item.image);
            //imageView.setColorFilter(new ThemeHelper(itemView.getContext()).getTextColor(), PorterDuff.Mode.SRC_ATOP);
            textView.setText(item.text);
            if(item.showSwitch){
                aSwitch.setVisibility(View.VISIBLE);
                aSwitch.setChecked(item.switchValue);
            }else {
                aSwitch.setVisibility(View.GONE);
            }

        }

        @Override
        public void unbindView(MenuItem item) {

        }
    }
}

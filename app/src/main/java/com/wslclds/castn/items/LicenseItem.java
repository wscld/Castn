package com.wslclds.castn.items;

import android.support.annotation.NonNull;
import android.view.View;
import android.widget.TextView;

import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.items.AbstractItem;

import java.util.List;

import com.wslclds.castn.R;
import com.wslclds.castn.factory.objects.License;
import butterknife.BindView;
import butterknife.ButterKnife;

public class LicenseItem extends AbstractItem<LicenseItem, LicenseItem.ViewHolder> {
    private License license;

    public LicenseItem(License license){
        this.license = license;
    }

    public License getLicense() {
        return license;
    }

    @NonNull
    @Override
    public ViewHolder getViewHolder(View v) {
        return new ViewHolder(v);
    }

    @Override
    public int getType() {
        return R.id.license_item_id;
    }

    @Override
    public int getLayoutRes() {
        return R.layout.item_license;
    }

    public class ViewHolder extends FastAdapter.ViewHolder<LicenseItem> {
        @BindView(R.id.author)
        TextView author;
        @BindView(R.id.name)
        TextView name;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this,itemView);
        }

        @Override
        public void bindView(LicenseItem item, List<Object> payloads) {
            author.setText(item.license.getAuthor());
            name.setText(item.license.getRepo());
        }

        @Override
        public void unbindView(LicenseItem item) {

        }
    }
}

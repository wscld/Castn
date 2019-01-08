package com.wslclds.castn.builders;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.IAdapter;
import com.mikepenz.fastadapter.IItem;
import com.mikepenz.fastadapter.adapters.ItemAdapter;
import com.mikepenz.fastadapter.listeners.OnClickListener;
import com.mikepenz.iconics.Iconics;
import com.mikepenz.iconics.IconicsDrawable;
import com.wslclds.castn.items.MenuItem;

import java.util.ArrayList;

import javax.annotation.Nullable;

public class AlertWithListBuilder {
    AlertDialog alertDialog;
    OnAction onAction;
    private ItemAdapter itemAdapter;
    private FastAdapter fastAdapter;

    public AlertWithListBuilder(Context context, ArrayList<IItem> arrayList, String title, String description, boolean showCreatePlaylist, OnAction onAction){
        this.onAction = onAction;
        final RecyclerView recyclerView = new RecyclerView(context);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        itemAdapter = new ItemAdapter();
        fastAdapter = FastAdapter.with(itemAdapter);
        recyclerView.setAdapter(fastAdapter);

        if(showCreatePlaylist){
            MenuItem menuItem = new MenuItem("Create Playlist", new IconicsDrawable(context).icon(CommunityMaterial.Icon.cmd_playlist_plus).color(Color.WHITE).paddingDp(9));
            arrayList.add(0,menuItem);
        }

        itemAdapter.add(arrayList);

        alertDialog = new AlertDialog.Builder(context).create();
        alertDialog.setView(recyclerView);
        alertDialog.setTitle(title);
        alertDialog.setMessage(description);

        fastAdapter.withOnClickListener(new OnClickListener() {
            @Override
            public boolean onClick(@Nullable View v, IAdapter adapter, IItem item, int position) {
                onAction.onClick(item,position);
                alertDialog.dismiss();
                return true;
            }
        });
    }
    public void show(){
        alertDialog.show();
    }

    public interface OnAction{
        void onClick(IItem item, int position);
    }
}

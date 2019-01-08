package com.wslclds.castn.builders;

import android.content.Context;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;

import java.util.ArrayList;

public class PopUpMenuBuilder {
    WithOnClickListener withOnClickListener;
    ArrayList<String> items;
    Context context;
    View view;

    public PopUpMenuBuilder(Context context, View view, ArrayList<String> items, WithOnClickListener withOnClickListener){
        this.context = context;
        this.withOnClickListener = withOnClickListener;
        this.items = items;
        this.view = view;
    }

    public void showMenu(){
        PopupMenu popupMenu = new PopupMenu(context,view);
        ArrayList<MenuItem> menuItems = new ArrayList<>();
        for(String item : items){
            MenuItem menuItem = popupMenu.getMenu().add(item);
            menuItems.add(menuItem);
        }
        popupMenu.show();
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                withOnClickListener.onClickListener(menuItems.indexOf(menuItem));
                return true;
            }
        });
    }

    public interface WithOnClickListener{
        void onClickListener(int position);
    }
}

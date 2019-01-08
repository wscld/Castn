package com.wslclds.castn.helpers;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;

import com.wslclds.castn.R;

public class ThemeHelper {
    Context context;
    boolean dark;

    public ThemeHelper(Context context){
        SharedPreferences preferences = context.getSharedPreferences("app.castn", Context.MODE_PRIVATE);
        dark = preferences.getBoolean("dark",false);
        this.context = context;
    }

    public void setDark(boolean dark){
        this.dark = dark;
        SharedPreferences preferences = context.getSharedPreferences("app.castn", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("dark", dark);
        editor.commit();
    }

    public boolean isDark() {
        return dark;
    }

    public void apply(boolean full){
        if(dark) {
            if(full){
                context.setTheme(R.style.AppThemeDark);
            }else {
                context.setTheme(R.style.PopupDark);
            }
        }else {
            if(full){
                context.setTheme(R.style.AppTheme);
            }else {
                context.setTheme(R.style.Popup);
            }
        }
    }

    public int getTextColor(){
        if(dark){
            return Color.WHITE;
        }else {
            return Color.BLACK;
        }
    }

    public int getThemeColor(int id) {
        if(dark){
            if(id == R.color.background){
                return R.color.darkThemeColorPrimaryDark;
            }else if(id == R.color.colorPrimary){
                return R.color.darkThemeColorPrimary;
            }else if(id == R.color.colorPrimaryDark){
                return R.color.darkThemeColorPrimaryDark;
            }else if(id == R.color.colorPrimaryDarkDarker){
                return R.color.darkThemeColorPrimaryDarkDarker;
            }else if(id == R.color.colorAccent){
                return R.color.darkThemeColorAccent;
            }else if(id == R.color.bottomNavDefault){
                return R.color.darkBottomNavDefault;
            }else if(id == R.color.bottomNavSelected){
                return R.color.darkBottomNavSelected;
            }
        }
        return id;
    }
}

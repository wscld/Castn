package com.wslclds.castn.helpers;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;

import com.wslclds.castn.R;

public class ThemeHelper {
    public static int THEME_LIGHT = 0;
    public static int THEME_DARK = 1;
    public static int THEME_AMOLED = 2;

    private Context context;
    private int theme;

    public ThemeHelper(Context context){
        SharedPreferences preferences = context.getSharedPreferences("app.castn", Context.MODE_PRIVATE);
        theme = preferences.getInt("theme",0);
        this.context = context;
    }

    public void setTheme(int theme){
        this.theme = theme;
        SharedPreferences preferences = context.getSharedPreferences("app.castn", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("theme", theme);
        editor.commit();
    }

    public int getTheme() {
        return theme;
    }

    public void apply(boolean full){
        if(theme == THEME_DARK) {
            if(full){
                context.setTheme(R.style.AppThemeDark);
            }else {
                context.setTheme(R.style.PopupDark);
            }
        }else if(theme == THEME_AMOLED){
            if(full){
                context.setTheme(R.style.AppThemeAMOLED);
            }else {
                context.setTheme(R.style.PopupAMOLED);
            }
        }else if(theme == THEME_LIGHT){
            if(full){
                context.setTheme(R.style.AppTheme);
            }else {
                context.setTheme(R.style.Popup);
            }
        }
    }

    public int getTextColor(){
        if(theme == THEME_AMOLED || theme == THEME_DARK){
            return Color.WHITE;
        }else {
            return Color.BLACK;
        }
    }

    public int getThemeColor(int id) {
        if(theme == THEME_DARK){
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
        }else if(theme == THEME_AMOLED){
            if(id == R.color.background){
                return R.color.AMOLEDThemeColorPrimaryDark;
            }else if(id == R.color.colorPrimary){
                return R.color.AMOLEDThemeColorPrimary;
            }else if(id == R.color.colorPrimaryDark){
                return R.color.AMOLEDThemeColorPrimaryDark;
            }else if(id == R.color.colorPrimaryDarkDarker){
                return R.color.AMOLEDThemeColorPrimaryDarkDarker;
            }else if(id == R.color.colorAccent){
                return R.color.AMOLEDThemeColorAccent;
            }else if(id == R.color.bottomNavDefault){
                return R.color.AMOLEDBottomNavDefault;
            }else if(id == R.color.bottomNavSelected){
                return R.color.AMOLEDBottomNavSelected;
            }
        }
        return id;
    }
}

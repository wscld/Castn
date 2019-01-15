package com.wslclds.castn.helpers;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

import com.wslclds.castn.builders.AlertBuilder;
import com.wslclds.castn.factory.DatabaseManager;
import com.wslclds.castn.factory.objects.Download;
import com.wslclds.castn.factory.objects.Episode;
import com.wslclds.castn.factory.objects.Playlist;
import com.wslclds.castn.services.DownloadService;
import com.wslclds.castn.services.OPMLImportService;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.Permission;

public class Helper {
    public static String DEFAULT_PLAYLIST_ID = "1001";
    public static int STATE_STREAM = 0;
    public static int STATE_QUEUED = 1;
    public static int STATE_DOWNLOADED = 2;
    private Context context;

    public Helper(Context context){
        this.context = context;
    }

    public static Playlist createPlaylistObject(String playlistName, boolean publicAvailable){
        Playlist playlist = new Playlist();
        playlist.setId(UUID.randomUUID().toString());
        playlist.setDate(new Date().getTime());
        playlist.setPublicAvailable(publicAvailable);
        playlist.setName(playlistName);
        return playlist;
    }

    public void makeDownload(Episode episode){
        if (android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
            AndPermission.with(context)
                    .runtime()
                    .permission(Permission.Group.STORAGE)
                    .onGranted(permissions -> {
                        startDownload(episode);
                    })
                    .onDenied(permissions -> {
                        new AlertBuilder(context, "Permission denied", "Storage permission is needed").show();
                    })
                    .start();
        }else{
            startDownload(episode);
        }
    }


    private void startDownload(Episode episode){
        DatabaseManager databaseManager = new DatabaseManager(context);
        if(databaseManager.addDownloadToQueue(episode)) {
            Intent intent = new Intent(context, DownloadService.class);
            context.startService(intent);
        }else {
            AlertBuilder alertBuilder = new AlertBuilder(context,"Already downloaded","This episode is already available offline");
            alertBuilder.show();
        }
    }

    public void startOPMLImport(String file){
        Intent intent = new Intent(context, OPMLImportService.class);
        intent.putExtra("file",file);
        context.startService(intent);
    }

    public void makeNetworkSafeDownload(ArrayList<Episode> episodes){
        DatabaseManager databaseManager = new DatabaseManager(context);
        for(Episode episode : episodes) {
            databaseManager.addDownloadToQueue(episode);
        }
        if(isWifiConnected()){
            Intent intent = new Intent(context, DownloadService.class);
            context.startService(intent);
        }
    }

    public boolean isAutomaticDownload(){
        SharedPreferences preferences = context.getSharedPreferences("app.castn", Context.MODE_PRIVATE);
        return preferences.getBoolean("autoDownload",false);
    }

    public void setAutomaticDownload(boolean value){
        SharedPreferences preferences = context.getSharedPreferences("app.castn", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("autoDownload", value);
        editor.commit();
    }

    public boolean isWifiConnected() {
        boolean haveConnectedWifi = false;

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] netInfo = cm.getAllNetworkInfo();
        for (NetworkInfo ni : netInfo) {
            if (ni.getTypeName().equalsIgnoreCase("WIFI"))
                if (ni.isConnected())
                    haveConnectedWifi = true;
        }
        return haveConnectedWifi;
    }

    public static String formatDuration(String duration){
        if(duration != null){
            if(duration.contains(":") && duration.length() == 8){
                return duration;
            }else {
                duration = duration.replace(":","");
                if(duration.length() == 3){
                    String firstPart = duration.substring(0,1);
                    String secondPart = duration.substring(1,3);
                    return "00:0"+firstPart+":"+secondPart;
                }else if(duration.length() == 4){
                    String firstPart = duration.substring(0,2);
                    String secondPart = duration.substring(2,4);
                    return "00:"+firstPart+":"+secondPart;
                }else if(duration.length() == 5){
                    String firstPart = duration.substring(0,2);
                    String secondPart = duration.substring(3,5);
                    return "00:"+firstPart+":"+secondPart;
                }else if(duration.length() == 6){
                    String firstPart = duration.substring(0,2);
                    String secondPart = duration.substring(2,4);
                    String thirdPart = duration.substring(4,6);
                    return firstPart+":"+secondPart+":"+thirdPart;
                }else {
                    return "00:00:00";
                }
            }
        }else {
            return "00:00:00";
        }
    }

    public static String urlToId(String url){
        try{
            if(url.substring(url.length() - 1).equals("/")){
                url = url.substring(0, url.length() - 1);
            }
        }catch (Exception e){ }

        return url.replace("http://","")
                .replace("https://","")
                .replace("www.","")
                .replace("=",".")
                .replace("&",".")
                .replace("?",".")
                .replace("/",".");
    }

    public static int darker (int color, float factor) {
        int a = Color.alpha( color );
        int r = Color.red( color );
        int g = Color.green( color );
        int b = Color.blue( color );

        return Color.argb( a,
                Math.max( (int)(r * factor), 0 ),
                Math.max( (int)(g * factor), 0 ),
                Math.max( (int)(b * factor), 0 ) );
    }

    public static float dpFromPx(final Context context, final float px) {
        return px / context.getResources().getDisplayMetrics().density;
    }

    public static float pxFromDp(final Context context, final float dp) {
        return dp * context.getResources().getDisplayMetrics().density;
    }

    public static final String formatTime(long millis) {
        long secs = millis / 1000;
        return String.format("%02d:%02d:%02d", (secs % 86400) / 3600, (secs % 3600) / 60, secs % 60);
    }

    public boolean deleteDownload(Download download){
        if(download != null && download.getLocalPath() != null) {
            File file = new File(download.getLocalPath());
            return file.delete();
        }
        return false;
    }

    public static String bytesIntoHumanReadable(long bytes) {
        float kilobyte = 1024;
        float megabyte = kilobyte * 1024;
        float gigabyte = megabyte * 1024;
        float terabyte = gigabyte * 1024;
        DecimalFormat df = new DecimalFormat();
        df.setMaximumFractionDigits(1);

        if ((bytes >= 0) && (bytes < kilobyte)) {
            return df.format(bytes) + " B";

        } else if ((bytes >= kilobyte) && (bytes < megabyte)) {
            return df.format((bytes / kilobyte)) + " KB";

        } else if ((bytes >= megabyte) && (bytes < gigabyte)) {
            return df.format((bytes / megabyte)) + " MB";

        } else if ((bytes >= gigabyte) && (bytes < terabyte)) {
            return df.format((bytes / gigabyte)) + " GB";

        } else if (bytes >= terabyte) {
            return df.format((bytes / terabyte)) + " TB";

        } else {
            return df.format(bytes) + " Bytes";
        }
    }
}

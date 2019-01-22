package com.wslclds.castn.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationManagerCompat;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.DownloadProgressListener;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;

import com.wslclds.castn.activities.MainActivity;
import com.wslclds.castn.factory.DatabaseManager;
import com.wslclds.castn.factory.objects.Download;
import com.wslclds.castn.GlideApp;
import com.wslclds.castn.factory.objects.Episode;
import com.wslclds.castn.helpers.Helper;
import com.wslclds.castn.R;

import io.realm.Realm;
import okhttp3.OkHttpClient;

import static com.androidnetworking.common.ANConstants.REQUEST_CANCELLED_ERROR;

public class DownloadService extends Service {
    private final IBinder downloadServiceBinder = new DownloadServiceBinder();

    private DownloadListener downloadListener;
    private DatabaseManager databaseManager;
    private ArrayList<Download> downloadQueue;
    private Notification.Builder notificationBuilder;
    private NotificationManager notificationManager;
    int NOTIFICATION_ID = 8;
    String CHANNEL_ID = "download_channel";
    String DOWNLOAD_TAG = "app.castn.download";
    private boolean downloadRunning;

    public static final String ACTION_CANCEL = "cancel";
    int prevPercentage;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return downloadServiceBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
    }

    public class DownloadServiceBinder extends Binder {
        public DownloadService getService() {
            return DownloadService.this;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Realm.init(this);

        String action = null;
        if(intent != null){
            action = intent.getAction();
        }
        if(action != null && action.equals(ACTION_CANCEL)){
            stopDownload();
        }else {
            if(!downloadRunning) {
                init();
            }else {
                updateQueue();
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    public void init(){
        OkHttpClient okHttpClient = new OkHttpClient() .newBuilder()
                .build();
        AndroidNetworking.initialize(getApplicationContext(),okHttpClient);

        databaseManager = new DatabaseManager(DownloadService.this);
        downloadQueue = databaseManager.getDownloadQueue();
        if(downloadListener != null) {
            downloadListener.onDownloadQueueUpdate();
        }
        removeNotification();
        startDownload();
    }

    public void updateQueue(){
        downloadQueue = databaseManager.getDownloadQueue();
        if(downloadListener != null) {
            downloadListener.onDownloadQueueUpdate();
        }
    }

    public Download getCurrentDownload(){
        if(downloadQueue.size() > 0){
            return downloadQueue.get(0);
        }
        return null;
    }

    public File getStorageFolder(){
        File mainDir = Environment.getExternalStorageDirectory();
        File castnFolder = new File(mainDir,"Castn");
        if(!castnFolder.isDirectory()){
            castnFolder.mkdir();
        }

        File podcastsFolder = new File(castnFolder,"Podcasts");
        if(!podcastsFolder.isDirectory()){
            podcastsFolder.mkdir();
        }

        return podcastsFolder;
    }

    public void startDownload(){
        if(downloadQueue != null && downloadQueue.size() > 0 && !downloadRunning) {

            downloadRunning = true;
            String title = downloadQueue.get(0).getTitle();
            String image = downloadQueue.get(0).getImage();
            String enclosureUrl = downloadQueue.get(0).getEnclosureUrl();
            Episode episode = databaseManager.getEpisode(enclosureUrl);
            String directory = getStorageFolder().toString();
            //String fileName = checkFilename(directory,episode.getPodcastTitle()+" - "+episode.getTitle());
            String fileName = getFileName(enclosureUrl);
            String fileDirectory = directory+"/"+fileName;

            databaseManager.setDownloadLocalPath(enclosureUrl,fileDirectory);

            AndroidNetworking.download(enclosureUrl,directory,fileName)
                    .setTag(DOWNLOAD_TAG)
                    .setPriority(Priority.HIGH)
                    .build()
                    .setDownloadProgressListener(new DownloadProgressListener() {
                        @Override
                        public void onProgress(long bytesDownloaded, long totalBytes) {
                            updateNotificationProgress((int)totalBytes,(int)bytesDownloaded);
                            if(downloadListener != null){
                                downloadListener.onProgress(enclosureUrl, totalBytes, bytesDownloaded);
                            }
                        }
                    }).startDownload(new com.androidnetworking.interfaces.DownloadListener() {
                @Override
                public void onDownloadComplete() {
                    downloadRunning = false;
                    if (downloadListener != null) {
                        downloadListener.onDownloadComplete(enclosureUrl);
                    }
                    databaseManager.setDownloadCompleted(enclosureUrl,fileDirectory);
                    init();
                }

                @Override
                public void onError(ANError anError) {
                    if(!anError.getErrorDetail().equals(REQUEST_CANCELLED_ERROR)) {
                        downloadRunning = false;
                        if (downloadListener != null) {
                            downloadListener.onDownloadFailed(enclosureUrl, anError.getErrorCode(), anError.getErrorDetail());
                        }
                        databaseManager.removeDownload(enclosureUrl);
                        init();
                    }
                }
            });

            startNotification(title,image);
        }
    }

    public void stopDownload(){
        AndroidNetworking.forceCancel(DOWNLOAD_TAG);
        new Helper(this).deleteDownload(downloadQueue.get(0));
        databaseManager.removeDownload(downloadQueue.get(0).getEnclosureUrl());
        downloadRunning = false;
        init();
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private void createChannel() {
        NotificationManager mNotificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        String id = CHANNEL_ID;
        CharSequence name = "Downloads";
        String description = "Notification when downloading episode";
        int importance = NotificationManager.IMPORTANCE_LOW;
        NotificationChannel mChannel = new NotificationChannel(id, name, importance);
        mChannel.setDescription(description);
        mChannel.setShowBadge(false);
        mChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
        mNotificationManager.createNotificationChannel(mChannel);
    }

    private void startNotification(String title, String image){
        GlideApp.with(this).asBitmap().load(image).override(100,100).into(new SimpleTarget<Bitmap>() {
            @Override
            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    createChannel();
                    notificationBuilder = new Notification.Builder(DownloadService.this,CHANNEL_ID);
                }else{
                    notificationBuilder = new Notification.Builder(DownloadService.this);
                }

                notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                notificationBuilder.setSmallIcon(R.drawable.ic_stat_icon)
                        .setColor(getResources().getColor(R.color.colorPrimary))
                        .setContentTitle(title)
                        .setContentText("Downloading")
                        .setProgress(0,0,true)
                        .setLargeIcon(resource)
                        .setContentIntent(makeContentIntent())
                        .addAction(new Notification.Action(0,"cancel",makePendingIntent(ACTION_CANCEL)))
                        .setAutoCancel(true);

                startForeground(NOTIFICATION_ID,notificationBuilder.build());
            }
        });
    }

    private void updateNotificationProgress(int total, int downloaded){
        int percentage = Math.round(((float)downloaded/total)*100);
        int max = 100;
        if(notificationBuilder != null && percentage != prevPercentage){
            notificationBuilder.setProgress(max,percentage,false);
            notificationManager.notify(NOTIFICATION_ID,notificationBuilder.build());
            prevPercentage = percentage;
            //startForeground(NOTIFICATION_ID,notificationBuilder.build());
        }
    }

    private void removeNotification(){
        stopForeground(true);
    }

    private String getFileName(String url) {
        String result = null;
        if (result == null) {
            result = Uri.parse(url).getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    private String checkFilename(String directory, String filename) {
        filename = filename.replace("/","");
        filename = filename.replace(" ","_");
        File f = new File(directory+"/"+filename);
        if(f.exists()){
            return filename+"_"+new Date().getTime();
        }
        return filename;
    }

    private PendingIntent makeContentIntent(){
        Intent intent = new Intent(this, MainActivity.class);
        return PendingIntent.getActivity(this, 5, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private PendingIntent makePendingIntent(String action){
        Intent intent = new Intent(this, DownloadService.class);
        intent.setAction(action);
        return PendingIntent.getService(this, 1, intent, 0);
    }

    public ArrayList<Download> getDownloadQueue() {
        return downloadQueue;
    }

    public void getDownloadListener(DownloadListener downloadListener) {
        this.downloadListener = downloadListener;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        removeNotification();
        AndroidNetworking.forceCancel(DOWNLOAD_TAG);
    }

    //interfaces
    public interface DownloadListener {
        void onProgress(String enclosureUrl, long totalBytes, long downloadedBytes);
        void onDownloadFailed(String enclosureUrl, int errorCode, String errorMessage);
        void onDownloadComplete(String enclosureUrl);
        void onDownloadQueueUpdate();
    }
}

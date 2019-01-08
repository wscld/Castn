package com.wslclds.castn.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;

import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.wslclds.castn.GlideApp;
import com.wslclds.castn.R;
import com.wslclds.castn.activities.MainActivity;
import com.wslclds.castn.factory.DatabaseManager;
import com.wslclds.castn.factory.Parser;
import com.wslclds.castn.factory.objects.OPMLElement;
import com.wslclds.castn.factory.objects.Podcast;
import com.wslclds.castn.factory.opmlFactory.OPMLReader;

import org.xmlpull.v1.XmlPullParserException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;

import io.realm.Realm;

public class OPMLImportService extends Service {
    private final IBinder opmlImportServiceBinder = new OPMLImportServiceBinder();
    private String opmlFile;
    private Notification.Builder notificationBuilder;
    private NotificationManager notificationManager;
    int NOTIFICATION_ID = 13;
    String CHANNEL_ID = "import_channel";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return opmlImportServiceBinder;
    }
    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }
    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
    }
    public class OPMLImportServiceBinder extends Binder {
        public OPMLImportService getService() {
            return OPMLImportService.this;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        opmlFile = intent.getStringExtra("file");
        System.out.println("file::"+opmlFile);
        Realm.init(this);
        loadOPML();
        return super.onStartCommand(intent, flags, startId);
    }

    private void loadOPML(){
        OPMLReader opmlReader = new OPMLReader();
        DatabaseManager databaseManager = new DatabaseManager(this);
        Parser parser = new Parser(this);
        try {
            Reader reader = new BufferedReader(new FileReader(new File(opmlFile)));
            ArrayList<OPMLElement> opmlElements = opmlReader.readDocument(reader);
            for(OPMLElement opmlElement : opmlElements){
                System.out.println("parsing:"+opmlElement.getXmlUrl());
                Podcast podcast = parser.parsePodcast(opmlElement.getXmlUrl());
                databaseManager.storePodcast(podcast);
            }
        } catch (Exception e) {
            e.printStackTrace();
            onDestroy();
        }finally {
            onDestroy();
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private void createChannel() {
        NotificationManager mNotificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        String id = CHANNEL_ID;
        CharSequence name = "OPML Import";
        String description = "Notification when importing OPML subscriptions";
        int importance = NotificationManager.IMPORTANCE_LOW;
        NotificationChannel mChannel = new NotificationChannel(id, name, importance);
        mChannel.setDescription(description);
        mChannel.setShowBadge(false);
        mChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
        mNotificationManager.createNotificationChannel(mChannel);
    }

    private void startNotification(){
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            createChannel();
            notificationBuilder = new Notification.Builder(OPMLImportService.this,CHANNEL_ID);
        }else{
            notificationBuilder = new Notification.Builder(OPMLImportService.this);
        }

        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationBuilder.setSmallIcon(R.drawable.ic_stat_icon)
                .setColor(getResources().getColor(R.color.colorPrimary))
                .setContentTitle("Importing OPML Subscriptions")
                .setProgress(0,0,true)
                .setContentIntent(makeContentIntent())
                .setAutoCancel(true);

        startForeground(NOTIFICATION_ID,notificationBuilder.build());
    }


    private PendingIntent makeContentIntent(){
        Intent intent = new Intent(this, MainActivity.class);
        return PendingIntent.getActivity(this, 5, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //notificationManager.cancel(NOTIFICATION_ID);
    }
}

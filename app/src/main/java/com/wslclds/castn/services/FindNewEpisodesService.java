package com.wslclds.castn.services;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.text.format.DateUtils;

import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;

import java.util.ArrayList;
import java.util.Date;

import com.wslclds.castn.GlideApp;
import com.wslclds.castn.R;
import com.wslclds.castn.activities.MainActivity;
import com.wslclds.castn.factory.DatabaseManager;
import com.wslclds.castn.factory.objects.Episode;
import com.wslclds.castn.factory.objects.Podcast;
import com.wslclds.castn.factory.Parser;
import com.wslclds.castn.helpers.Helper;

import io.realm.Realm;

public class FindNewEpisodesService extends JobService {
    boolean isWorking;
    int NOTIFICATION_ID = 4;
    String CHANNEL_ID = "new_episode_channel";

    @Override
    public boolean onStartJob(JobParameters job) {
        isWorking = true;
        Realm.init(this);

        @SuppressLint("StaticFieldLeak") AsyncTask asyncTask = new AsyncTask() {
            int newEpisodes = 0;
            Podcast lastPodcast;

            @Override
            protected Object doInBackground(Object[] objects) {
                DatabaseManager databaseManager = new DatabaseManager(FindNewEpisodesService.this);
                Helper helper = new Helper(FindNewEpisodesService.this);
                Parser parser = new Parser(FindNewEpisodesService.this);

                ArrayList<Podcast> podcasts = databaseManager.getPodcasts();
                for(Podcast podcast : podcasts){
                    if(podcast != null){
                        long latestPubDate = databaseManager.getLatestPubDate(podcast.getUrl());
                            if(latestPubDate != 0){ // if has episodes cached find only new episodes


                                if(new Date().getTime()-latestPubDate < DateUtils.YEAR_IN_MILLIS) { //skip inactive podcasts
                                    if (new Date().getTime() - latestPubDate > (6*DateUtils.HOUR_IN_MILLIS)) {
                                        ArrayList<Episode> episodes = parser.parseEpisodesUntil(podcast.getUrl(), latestPubDate);
                                        newEpisodes += episodes.size();
                                        if (episodes.size() > 0) {
                                            databaseManager.storeEpisodes(episodes);
                                            lastPodcast = podcast;
                                        }

                                        if (helper.isAutomaticDownload()) {
                                            helper.makeNetworkSafeDownload(episodes);
                                        }

                                        databaseManager.updatePodcastPubDate(podcast.getUrl());
                                    }
                                }

                            }else{ // no episodes cached, load all episodes
                                ArrayList<Episode> episodes = parser.parseEpisodes(podcast.getUrl(),true);
                                newEpisodes += episodes.size();
                                if(episodes.size() > 0){
                                    databaseManager.storeEpisodes(episodes);
                                    lastPodcast = podcast;
                                }

                                databaseManager.updatePodcastPubDate(podcast.getUrl());
                            }
                    }
                }
                return null;
            }

            @Override
            protected void onPostExecute(Object o) {
                if(lastPodcast != null){
                    buildNotification(newEpisodes, lastPodcast);
                }
                isWorking = false;
                jobFinished(job,false);
            }
        };
        asyncTask.execute();

        return isWorking; // Answers the question: "Is there still work going on?"
    }

    @Override
    public boolean onStopJob(JobParameters job) {
        if(isWorking){
            return false;
        }else {
            return true;
        }
        // Answers the question: "Should this job be retried?"
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private void createChannel() {
        NotificationManager mNotificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        String id = CHANNEL_ID;
        CharSequence name = "New episodes";
        String description = "Notification when new episode is available";
        int importance = NotificationManager.IMPORTANCE_HIGH;
        NotificationChannel mChannel = new NotificationChannel(id, name, importance);
        mChannel.setDescription(description);
        mChannel.setShowBadge(false);
        mChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
        mNotificationManager.createNotificationChannel(mChannel);
    }

    private void buildNotification(int newEpisodes, Podcast lastPodcast){
        if(lastPodcast != null){
            GlideApp.with(this).asBitmap().load(lastPodcast.getImage()).into(new SimpleTarget<Bitmap>() {
                @Override
                public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                    NotificationManager nManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                    Notification.Builder builder;
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                        createChannel();
                        builder = new Notification.Builder(FindNewEpisodesService.this,CHANNEL_ID);
                    }else{
                        builder = new Notification.Builder(FindNewEpisodesService.this);
                    }
                    builder.setContentTitle(newEpisodes +" New episodes available");
                    builder.setContentText("From \""+lastPodcast.getTitle()+"\" and more");
                    builder.setSmallIcon(R.drawable.ic_stat_icon);
                    builder.setLargeIcon(resource);
                    builder.setAutoCancel(true);
                    builder.setShowWhen(true);
                    builder.setContentIntent(makeContentIntent());
                    nManager.notify(NOTIFICATION_ID,builder.build());
                }
            });
        }
    }


    private PendingIntent makeContentIntent(){
        Intent intent = new Intent(this, MainActivity.class);
        return PendingIntent.getActivity(this, 5, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }
}

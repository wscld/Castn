package com.wslclds.castn.services;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.session.MediaSessionManager;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaButtonReceiver;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;

import java.io.IOException;
import java.util.ArrayList;

import com.wslclds.castn.activities.MainActivity;
import com.wslclds.castn.factory.DatabaseManager;
import com.wslclds.castn.factory.objects.Download;
import com.wslclds.castn.factory.objects.Episode;
import com.wslclds.castn.GlideApp;
import com.wslclds.castn.R;

import io.realm.Realm;


public class AudioPlayerService extends Service implements AudioManager.OnAudioFocusChangeListener {

    private final IBinder audioPlayerBinder = new AudioPlayerBinder();

    private MediaPlayer mediaPlayer;
    private MediaSessionManager mediaSessionManager;
    private MediaSessionCompat mediaSession;
    private AudioManager audioManager;

    Handler progressHandler;
    Runnable progressRunnable;
    int handlerCount = 0;
    private OnChange onChange;
    private ArrayList<Episode> episodes;
    private int index;
    private String playlistId;
    private boolean loading;
    DatabaseManager databaseManager;

    private static final String CHANNEL_ID = "media_playback_channel_1";
    private static final String MEDIA_SESSION_TAG = "AudioPlayerService";
    private static final int FOREGROUND_ID = 1;

    public static final String ACTION_PLAY_PAUSE = "playPause";
    public static final String ACTION_REPLAY = "replay";
    public static final String ACTION_FORWARD = "forward";
    public static final String ACTION_CLOSE = "close";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return audioPlayerBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
    }

    public class AudioPlayerBinder extends Binder {
        public AudioPlayerService getService() {
            return AudioPlayerService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Realm.init(this);

        //ACTION_AUDIO_BECOMING_NOISY -- change in audio outputs -- BroadcastReceiver
        registerBecomingNoisyReceiver();
        registerHeadsetPlugUnplugReceiver();
    }

    @Override
    public void onDestroy() {
        killMediaPlayer();
        unregisterBecomingNoisyReceiver();
        unregisterHeadsetPlugUnplugReceiver();
        episodes = null;
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        databaseManager = new DatabaseManager(this);

        if (mediaPlayer != null && intent != null && intent.getAction() != null) {
            switch (intent.getAction()) {
                case ACTION_PLAY_PAUSE:
                    playPause();
                    break;
                case ACTION_REPLAY:
                    backward(10000);
                    break;
                case ACTION_FORWARD:
                    forward(10000);
                    break;
                case ACTION_CLOSE:
                    onDestroy();
                    break;
            }
        }

        return START_NOT_STICKY;
    }


    //mediaplayer
    public void setAndPlay(String playlistId){
        this.playlistId = playlistId;
        this.episodes = databaseManager.getPlaylistEpisodes(playlistId);
        this.index = 0;
        if(episodes != null && episodes.size() > 0){
            initMediaPlayer();
        }
    }

    private void initMediaPlayer(){
        killMediaPlayer();
        loading = true;
        final Episode currentEpisode = getCurrentEpisode();
        final Download download = databaseManager.getDownload(currentEpisode.getEnclosureUrl());
        mediaPlayer = new MediaPlayer();

        try {
            if(download  == null){
                mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mediaPlayer.setDataSource(currentEpisode.getEnclosureUrl());
            }else {
                mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mediaPlayer.setDataSource(download.getLocalPath());
            }
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {
                    play();
                    mediaPlayer.seekTo((int) databaseManager.getElapsedTimeFor(currentEpisode.getEnclosureUrl()));
                    onChange.onPlayingStateChanged(isPlaying());
                    loading = false;
                }
            });
            mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
                    return true;
                }
            });
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    playNext();
                }
            });
            mediaPlayer.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }
        progressHandler = new Handler();
        progressRunnable = new Runnable() {
            @Override
            public void run() {
                if(isPlaying()){
                    onChange.onProgressChanged(getPosition(),getDuration());
                }
                if(handlerCount >= 10 && isPlaying()){
                    databaseManager.updateElapsedTime(getCurrentEpisode().getEnclosureUrl(),getPosition(),getDuration());
                    handlerCount = 0;
                }
                handlerCount++;
                progressHandler.postDelayed(this,1000);
            }
        };
        progressHandler.postDelayed(progressRunnable,1000);
        onChange.onPlayingStateChanged(isPlaying());
        onChange.onEpisodeChanged(currentEpisode);
        initMediaSession();
        buildNotification(true);
    }

    private void killMediaPlayer(){
        removeAudioFocus();
        if(mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.setOnPreparedListener(null);
            mediaPlayer.release();
            mediaPlayer = null;
            onChange.onPlayingStateChanged(isPlaying());
            onChange.onEpisodeChanged(null);
            progressHandler.removeCallbacks(progressRunnable);
            progressHandler = null;
            progressRunnable = null;
        }
    }

    //mediasession
    private void initMediaSession(){
        mediaSessionManager = (MediaSessionManager) getSystemService(Context.MEDIA_SESSION_SERVICE);
        mediaSession = new MediaSessionCompat(getApplicationContext(), MEDIA_SESSION_TAG);
        mediaSession.setActive(true);
        mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        updateMetaData(null);
        mediaSession.setCallback(new MediaSessionCompat.Callback() {
            @Override
            public void onPlay() {
                super.onPlay();
                playPause();
            }

            @Override
            public void onPause() {
                super.onPause();
                playPause();
            }
        });
    }

    //notification
    @RequiresApi(Build.VERSION_CODES.O)
    private void createChannel() {
        NotificationManager mNotificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        String id = CHANNEL_ID;
        CharSequence name = "Podcast playback";
        String description = "Media playback controls";
        int importance = NotificationManager.IMPORTANCE_LOW;
        NotificationChannel mChannel = new NotificationChannel(id, name, importance);
        mChannel.setDescription(description);
        mChannel.setShowBadge(false);
        mChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
        mNotificationManager.createNotificationChannel(mChannel);
    }

    private void buildNotification(final boolean isPlaying){
        GlideApp.with(this).asBitmap().load(getCurrentEpisode().getImage()).override(200,200).into(new SimpleTarget<Bitmap>() {
            @Override
            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                updateMetaData(resource);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    createChannel();
                }

                NotificationCompat.Action forwardAction  = new NotificationCompat.Action(R.drawable.ic_forward_10,"Forward",makePendingIntent(ACTION_FORWARD));
                NotificationCompat.Action backwardAction  = new NotificationCompat.Action(R.drawable.ic_replay_10,"Backward",makePendingIntent(ACTION_REPLAY));
                NotificationCompat.Action playPauseAction;
                if(isPlaying){
                    playPauseAction = new NotificationCompat.Action(R.drawable.ic_pause,"Pause",makePendingIntent(ACTION_PLAY_PAUSE));
                }else {
                    playPauseAction = new NotificationCompat.Action(R.drawable.ic_play_arrow,"Play",makePendingIntent(ACTION_PLAY_PAUSE));
                }


                NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(AudioPlayerService.this, CHANNEL_ID);
                notificationBuilder
                        .setStyle(new android.support.v4.media.app.NotificationCompat.MediaStyle()
                                .setShowCancelButton(true)
                                .setMediaSession(mediaSession.getSessionToken())
                                .setShowActionsInCompactView(0,1,2)
                                .setCancelButtonIntent(MediaButtonReceiver.buildMediaButtonPendingIntent(AudioPlayerService.this, PlaybackStateCompat.ACTION_STOP)))
                        .setSmallIcon(R.drawable.ic_stat_icon)
                        .setColor(ContextCompat.getColor(AudioPlayerService.this, R.color.colorPrimary))
                        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                        .setOnlyAlertOnce(true)
                        .addAction(backwardAction)
                        .addAction(playPauseAction)
                        .setColor(Color.BLACK)
                        .addAction(forwardAction)
                        .setDeleteIntent(makePendingIntent(ACTION_CLOSE))
                        .setContentIntent(makeContentIntent())
                        .setLargeIcon(resource)
                        .setContentTitle(getCurrentEpisode().getTitle())
                        .setContentText(getCurrentEpisode().getPodcastTitle());

                NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                mNotificationManager.notify(FOREGROUND_ID, notificationBuilder.build());

                if(!isPlaying){
                    stopForeground(false);
                }else {
                    startForeground(FOREGROUND_ID,notificationBuilder.build());
                }
            }
        });
    }

    private void updateMetaData(Bitmap bitmap) {
        if(mediaSession != null){
            mediaSession.setMetadata(new MediaMetadataCompat.Builder()
                    .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, bitmap)
                    .putBitmap(MediaMetadataCompat.METADATA_KEY_ART, bitmap)
                    .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, episodes.get(index).getPodcastTitle())
                    .putString(MediaMetadataCompat.METADATA_KEY_TITLE, episodes.get(index).getTitle())
                    .build());
        }
    }

    private PendingIntent makePendingIntent(String action){
        Intent intent = new Intent(this, AudioPlayerService.class);
        intent.setAction(action);
        return PendingIntent.getService(this, 1, intent, 0);
    }

    private PendingIntent makeContentIntent(){
        Intent intent = new Intent(this, MainActivity.class);
        return PendingIntent.getActivity(this, 5, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    //control methods
    public void getChanges(OnChange onChange) {
        this.onChange = onChange;
    }

    public boolean isPlaying(){
        if(mediaPlayer != null){
            return mediaPlayer.isPlaying();
        }
        return false;
    }

    public boolean isLoading() {
        return loading;
    }

    public void playPause(){
        if(mediaPlayer != null && mediaPlayer.isPlaying()){
            pause();
        }else if(mediaPlayer != null && !mediaPlayer.isPlaying()){
            play();
        }
    }

    public void pause(){
        if(mediaPlayer != null && mediaPlayer.isPlaying()){
            mediaPlayer.pause();
            onChange.onPlayingStateChanged(isPlaying());
            buildNotification(false);
            removeAudioFocus();
        }
    }

    public void play(){
        if(mediaPlayer != null && !mediaPlayer.isPlaying() && requestAudioFocus()){
            mediaPlayer.start();
            onChange.onPlayingStateChanged(isPlaying());
            buildNotification(true);
        }
    }

    public void stop(){
        stopForeground(true);
        onDestroy();
    }

    public void forward(int time){
        if(mediaPlayer != null){
            mediaPlayer.seekTo((int) (getPosition()+time));
        }
    }

    public void backward(int time){
        if(mediaPlayer != null){
            mediaPlayer.seekTo((int) (getPosition()-time));
        }
    }

    public long getPosition(){
        if(mediaPlayer != null){
            return mediaPlayer.getCurrentPosition();
        }
        return 0;
    }


    public long getDuration(){
        if(mediaPlayer != null){
            return mediaPlayer.getDuration();
        }
        return 0;
    }

    public void seekTo(int position){
        if(mediaPlayer != null){
            mediaPlayer.seekTo(position);
        }
    }

    public Episode getCurrentEpisode(){
        if(episodes != null){
            return episodes.get(index);
        }
        return null;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void setSpeed(float speed){
        if(mediaPlayer != null){
            mediaPlayer.setPlaybackParams(mediaPlayer.getPlaybackParams().setSpeed(speed));
        }
    }

    public String getPlaylistId() {
        return playlistId;
    }

    public void playNext(){
        databaseManager.removeFromPlaylist(getCurrentEpisode().getEnclosureUrl(),playlistId);
        this.episodes = databaseManager.getPlaylistEpisodes(playlistId);
        setAndPlay(playlistId);
    }

    public ArrayList<Episode> getEpisodes() {
        return episodes;
    }

    public int getIndex() {
        return index;
    }


    //broadcastreceiver
    private BroadcastReceiver becomingNoisyReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            pause();
        }
    };

    private BroadcastReceiver headsetPlugUnplugReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            pause();
        }
    };

    private void registerBecomingNoisyReceiver(){
        IntentFilter intentFilter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        registerReceiver(becomingNoisyReceiver, intentFilter);
    }

    private void registerHeadsetPlugUnplugReceiver(){
        IntentFilter intentFilter = new IntentFilter(AudioManager.ACTION_HEADSET_PLUG);
        registerReceiver(headsetPlugUnplugReceiver, intentFilter);
    }

    private void unregisterBecomingNoisyReceiver(){
        try{
            unregisterReceiver(becomingNoisyReceiver);
        }catch (Exception e){}
    }

    private void unregisterHeadsetPlugUnplugReceiver(){
        try{
            unregisterReceiver(headsetPlugUnplugReceiver);
        }catch (Exception e){}
    }

    //audio manager
    private boolean requestAudioFocus() {
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int result = audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            //Focus gained
            return true;
        }
        //Could not gain focus
        return false;
    }

    private void removeAudioFocus() {
        if(audioManager != null){
            audioManager.abandonAudioFocus(this);
        }
    }

    @Override
    public void onAudioFocusChange(int i) {
        //Invoked when the audio focus of the system is updated.
        switch (i) {
            case AudioManager.AUDIOFOCUS_GAIN:
                // resume playback
                play();
                break;
            case AudioManager.AUDIOFOCUS_LOSS:
                // Lost focus for an unbounded amount of time: stop playback and release media player
                pause();
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                // Lost focus for a short time, but we have to stop
                // playback. We don't release the media player because playback
                // is likely to resume
                pause();
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                // Lost focus for a short time, but it's ok to keep playing
                // at an attenuated level
                pause();
                break;
        }
    }

    //interfaces
    public interface OnChange{
        void onProgressChanged(long progress, long total);
        void onPlayingStateChanged(boolean isPlaying);
        void onEpisodeChanged(Episode episode);
    }
}

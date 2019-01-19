package com.wslclds.castn.activities;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.text.util.Linkify;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.RetryStrategy;
import com.firebase.jobdispatcher.Trigger;
import com.google.gson.Gson;
import com.miguelcatalan.materialsearchview.MaterialSearchView;
import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import com.wslclds.castn.builders.AlertBuilder;
import com.wslclds.castn.extensions.LinkTransformationMethod;
import com.wslclds.castn.fragments.EpisodeListFragment;
import com.wslclds.castn.fragments.PodcastFragment;
import com.wslclds.castn.helpers.ThemeHelper;
import at.blogc.android.views.ExpandableTextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.wslclds.castn.factory.DatabaseManager;
import com.wslclds.castn.factory.objects.Podcast;
import com.wslclds.castn.helpers.Helper;
import com.wslclds.castn.factory.objects.Episode;
import com.wslclds.castn.factory.objects.ToolbarEvent;
import com.wslclds.castn.fragments.MainFragment;
import com.wslclds.castn.GlideApp;
import com.wslclds.castn.builders.PopUpMenuBuilder;
import com.wslclds.castn.R;
import com.wslclds.castn.services.AudioPlayerService;
import com.wslclds.castn.services.FindNewEpisodesService;
import com.wslclds.castn.services.PlayerKillerService;

import io.realm.Realm;
import me.yokeyword.fragmentation.SupportActivity;

public class MainActivity extends SupportActivity{

    private static int SLEEP_TIMER_CODE = 3;

    private Uri appLinkData;
    public static String FIND_EPISODES_SERVICE_TAG = "episode_finder";
    private  AudioPlayerService audioPlayerService;
    private  DatabaseManager databaseManager;
    private ThemeHelper themeHelper;
    private  boolean isBound = false;
    private  boolean seeking = false;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.search_view)
    MaterialSearchView searchView;
    @BindView(R.id.sliding_layout)
    SlidingUpPanelLayout slidingUpPanelLayout;
    @BindView(R.id.miniTitle)
    TextView miniTitle;
    @BindView(R.id.miniAuthor)
    TextView miniAuthor;
    @BindView(R.id.miniImage)
    ImageView miniImage;
    @BindView(R.id.miniProgress)
    ProgressBar miniProgress;
    @BindView(R.id.seekBar)
    SeekBar seekBar;
    @BindView(R.id.miniPlayPause)
    ImageButton miniPlayPause;
    @BindView(R.id.fullTitle)
    TextView fullTitle;
    @BindView(R.id.fullDescription)
    ExpandableTextView fullDescription;
    @BindView(R.id.fullImage)
    ImageView fullImage;
    @BindView(R.id.fullBackTime)
    ImageButton fullBackTime;
    @BindView(R.id.fullPlaylist)
    ImageButton fullPlaylist;
    @BindView(R.id.fullSkipTime)
    ImageButton fullSkipTime;
    @BindView(R.id.fullPlayPause)
    ImageButton fullPlayPause;
    @BindView(R.id.fullSpeed)
    ImageButton fullSpeed;
    @BindView(R.id.sleepButton)
    ImageButton sleepButton;
    @BindView(R.id.time1)
    TextView time1;
    @BindView(R.id.time2)
    TextView time2;
    @BindView(R.id.miniBackground)
    CardView miniBackground;
    @BindView(R.id.fullBackground)
    LinearLayout fullBackground;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Realm.init(this);
        themeHelper = new ThemeHelper(this);
        themeHelper.apply(true);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        databaseManager = new DatabaseManager(this);
        databaseManager.registerDeviceId();

        setSupportActionBar(toolbar);

        checkFirstRun();

        searchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                MainFragment mainFragment = findFragment(MainFragment.class);
                if(mainFragment != null){
                    mainFragment.start(EpisodeListFragment.newInstance(EpisodeListFragment.TYPE_EPISODE_SEARCH,query));
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                //Do some magic
                return false;
            }
        });

        searchView.setOnSearchViewListener(new MaterialSearchView.SearchViewListener() {
            @Override
            public void onSearchViewShown() {
                //Do some magic
            }

            @Override
            public void onSearchViewClosed() {
                //Do some magic
            }
        });

        handleIntent(getIntent());
        loadView();
    }

    private void checkFirstRun(){
        SharedPreferences prefs = getSharedPreferences("com.wslclds.castn", MODE_PRIVATE);
        if (prefs.getBoolean("firstrun", true)) {
            Intent intent = new Intent(this,IntroActivity.class);
            startActivity(intent);
            prefs.edit().putBoolean("firstrun", false).commit();
        }
    }


    public void loadView(){
        if(appLinkData != null){
            loadRootFragment(R.id.mainLayout,MainFragment.newInstance(appLinkData.toString()));
        }else {
            loadRootFragment(R.id.mainLayout,MainFragment.newInstance(null));
        }


        FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(this));
        Job findEpisodesJob = dispatcher.newJobBuilder()
                .setService(FindNewEpisodesService.class)
                .setTag(FIND_EPISODES_SERVICE_TAG)
                .setRecurring(true)
                .setLifetime(Lifetime.FOREVER)
                .setTrigger(Trigger.executionWindow((int)TimeUnit.HOURS.toSeconds(3), (int)TimeUnit.HOURS.toSeconds(3)+(int)TimeUnit.MINUTES.toSeconds(10)))
                .setReplaceCurrent(false)
                .setRetryStrategy(RetryStrategy.DEFAULT_EXPONENTIAL)
                .build();
        dispatcher.mustSchedule(findEpisodesJob);

        //Intent updateListenerService = new Intent(this,UpdateListenerService.class);
        //startService(updateListenerService);
    }

    private void handleIntent(Intent intent) {
        Uri appLinkData = intent.getData();
        if (appLinkData != null){
            String query = appLinkData.getQueryParameter("feed");
            MainFragment mainFragment = findFragment(MainFragment.class);
            if(mainFragment != null){
                mainFragment.start(PodcastFragment.newInstance(query));
            }else{
                this.appLinkData = appLinkData;
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressedSupport();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressedSupport() {
        if(slidingUpPanelLayout.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED){
            slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        }else {
            super.onBackPressedSupport();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(ToolbarEvent toolbarEvent) {
        invalidateOptionsMenu();

        if(toolbarEvent.getTitle() != null){
            setTitle(toolbarEvent.getTitle());
        }
        if(toolbarEvent.getColor() != 0){
            toolbar.setBackgroundColor(toolbarEvent.getColor());
            toolbar.setTitleTextColor(Color.WHITE);
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Helper.darker(toolbarEvent.getColor(),0.6f));
            Drawable upArrow = new IconicsDrawable(this,CommunityMaterial.Icon.cmd_arrow_left).paddingDp(20);
            upArrow.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
            getSupportActionBar().setHomeAsUpIndicator(upArrow);
        }else {
            toolbar.setBackgroundColor(getResources().getColor(themeHelper.getThemeColor(R.color.colorPrimary)));
            toolbar.setTitleTextColor(themeHelper.getTextColor());
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(themeHelper.getThemeColor(R.color.colorPrimaryDark)));
            Drawable upArrow = new IconicsDrawable(this,CommunityMaterial.Icon.cmd_arrow_left).paddingDp(20);
            upArrow.setColorFilter(themeHelper.getTextColor(), PorterDuff.Mode.SRC_ATOP);
            getSupportActionBar().setHomeAsUpIndicator(upArrow);
        }

        if(toolbarEvent.isHome()){
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }else{
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        MenuItem item = menu.findItem(R.id.action_search);
        Drawable drawable = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_action_action_search, null);
        drawable = DrawableCompat.wrap(drawable);
        DrawableCompat.setTint(drawable, themeHelper.getTextColor());
        item.setIcon(drawable);
        searchView.setMenuItem(item);

        if(toolbar.getTitle().equals("Timeline")){
            item.setVisible(true);
        }else {
            item.setVisible(false);
        }

        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK && requestCode == SLEEP_TIMER_CODE){
            if(isBound && data != null){
                long time = data.getLongExtra("result",0);
                if(time != 0 && time != 1) {
                    audioPlayerService.setSleep(time);
                }else if (time == 1){
                    audioPlayerService.removeTimer();
                }
            }
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(!isBound){
            makeBind();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
        makeBind();
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
        removeBind();
    }

    //player binding
    private void makeBind(){
        Intent intent = new Intent(this , AudioPlayerService.class);
        try {
            startService(intent);
        }catch (Exception e){
            e.printStackTrace();
        }
        bindService(intent , boundServiceConnection,BIND_AUTO_CREATE);
    }
    private void removeBind(){
        if(isBound){
            unbindService(boundServiceConnection);
            isBound = false;
        }
    }
    private ServiceConnection boundServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {

            AudioPlayerService.AudioPlayerBinder binderBridge = (AudioPlayerService.AudioPlayerBinder) service ;
            audioPlayerService = binderBridge.getService();
            isBound = true;
            setupUI();

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBound = false;
            audioPlayerService = null;
        }
    };

    //player logic
    public void playAudio(String playlistId){
        if(audioPlayerService != null){
            audioPlayerService.setAndPlay(playlistId);
        }
    }


    private void setupUI(){

        fullSkipTime.setImageDrawable(new IconicsDrawable(this,GoogleMaterial.Icon.gmd_forward_30));
        fullBackTime.setImageDrawable(new IconicsDrawable(this,GoogleMaterial.Icon.gmd_replay_30));
        fullPlaylist.setImageDrawable(new IconicsDrawable(this,GoogleMaterial.Icon.gmd_playlist_play));
        fullSpeed.setImageDrawable(new IconicsDrawable(this,CommunityMaterial.Icon.cmd_clock_fast));
        sleepButton.setImageDrawable(new IconicsDrawable(this,CommunityMaterial.Icon.cmd_sleep));

        if(audioPlayerService != null){
            updateEpisodeDetails(audioPlayerService.getCurrentEpisode());
            updatePlayPauseButton(audioPlayerService.isPlaying());
        }

        miniPlayPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(audioPlayerService != null){
                    audioPlayerService.playPause();
                }
            }
        });

        miniBackground.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                PopUpMenuBuilder popUpMenuBuilder = new PopUpMenuBuilder(MainActivity.this,
                        view,
                        new ArrayList<>(Arrays.asList("Stop")),
                        new PopUpMenuBuilder.WithOnClickListener() {
                            @Override
                            public void onClickListener(int position) {
                                if (position == 0 && audioPlayerService != null){
                                    audioPlayerService.stop();
                                }
                            }
                        });
                popUpMenuBuilder.showMenu();
                return true;
            }
        });

        sleepButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,SleepTimerActivity.class);
                intent.putExtra("time",audioPlayerService.getSleepTime());
                startActivityForResult(intent,SLEEP_TIMER_CODE);
            }
        });

        fullPlayPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(audioPlayerService != null){
                    audioPlayerService.playPause();
                }
            }
        });

        fullDescription.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //fullDescription.toggle();
                Intent intent = new Intent(MainActivity.this,EpisodeDetailActivity.class);
                intent.putExtra("episode",new Gson().toJson(audioPlayerService.getCurrentEpisode()));
                intent.putExtra("justDescription", true);
                startActivity(intent);
            }
        });

        fullTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //fullDescription.toggle();
                Intent intent = new Intent(MainActivity.this,EpisodeDetailActivity.class);
                intent.putExtra("episode",new Gson().toJson(audioPlayerService.getCurrentEpisode()));
                intent.putExtra("justDescription", true);
                startActivity(intent);
            }
        });

        fullPlaylist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this,EpisodeQueueActivity.class);
                intent.putExtra("playlistId", audioPlayerService.getPlaylistId());
                startActivity(intent);
            }
        });

        fullSkipTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(audioPlayerService != null){
                    audioPlayerService.forward(30000);
                }
            }
        });

        fullBackTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(audioPlayerService != null){
                    audioPlayerService.backward(30000);
                }
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            fullSpeed.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ArrayList<String> items = new ArrayList<>();
                    items.add("0.5x");
                    items.add("0.75x");
                    items.add("Normal");
                    items.add("1.25x");
                    items.add("1.5x");

                    new PopUpMenuBuilder(MainActivity.this,
                            view,
                            items,
                            new PopUpMenuBuilder.WithOnClickListener() {
                                @Override
                                public void onClickListener(int position) {
                                    System.out.println(position);
                                    switch (position) {
                                        case 0:
                                            audioPlayerService.setSpeed(0.50f);
                                            break;
                                        case 1:
                                            audioPlayerService.setSpeed(0.75f);
                                            break;
                                        case 2:
                                            audioPlayerService.setSpeed(1.0f);
                                            break;
                                        case 3:
                                            audioPlayerService.setSpeed(1.25f);
                                            break;
                                        case 4:
                                            audioPlayerService.setSpeed(1.50f);
                                            break;
                                    }

                                }
                            }).showMenu();
                }
            });
        }else{
            fullSpeed.setEnabled(false);
            fullSpeed.setAlpha(0.5f);
        }

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if(seeking){
                    audioPlayerService.seekTo(i);
                    updateProgress(i,seekBar.getMax());
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                seeking = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                seeking = false;
            }
        });

        audioPlayerService.getChanges(new AudioPlayerService.OnChange() {
            @Override
            public void onProgressChanged(long progress, long total) {
                updateProgress(progress,total);
            }

            @Override
            public void onPlayingStateChanged(boolean isPlaying) {
                updatePlayPauseButton(isPlaying);
            }

            @Override
            public void onEpisodeChanged(Episode episode) {
                updateEpisodeDetails(episode);
            }
        });
    }

    private void updateEpisodeDetails(Episode episode){
        if(episode != null){
            showMiniPlayer(true);
            slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
            miniTitle.setText(episode.getTitle());
            miniAuthor.setText(episode.getPodcastTitle());
            fullTitle.setText(episode.getTitle());
            fullDescription.setText(episode.getPlainDescription());
            fullDescription.setTransformationMethod(new LinkTransformationMethod(Linkify.WEB_URLS | Linkify.EMAIL_ADDRESSES | Linkify.PHONE_NUMBERS));
            GlideApp.with(MainActivity.this).load(episode.getImage()).centerCrop().into(fullImage);

            //colors setup
            Podcast podcast = databaseManager.getPodcast(episode.getUrl());
            if(podcast != null && podcast.getColor() != 0){
                int color = podcast.getColor();
                int darker = Helper.darker(color,0.7f);
                GlideApp.with(MainActivity.this).load(episode.getImage()).centerCrop().into(miniImage);
                miniBackground.setCardBackgroundColor(darker);
                fullBackground.setBackgroundColor(darker);
            }else {
                miniImage.setImageBitmap(null);
                miniBackground.setCardBackgroundColor(getResources().getColor(R.color.colorPrimary));
                fullBackground.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                GlideApp.with(MainActivity.this).asBitmap().override(100,100).load(episode.getImage()).centerCrop().into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        miniImage.setImageBitmap(resource);
                        Palette.from(resource).generate(new Palette.PaletteAsyncListener() {
                            @Override
                            public void onGenerated(@NonNull Palette palette) {
                                int color = palette.getDominantColor(getResources().getColor(R.color.colorPrimary));
                                int darker = Helper.darker(color,0.7f);
                                miniBackground.setCardBackgroundColor(darker);
                                fullBackground.setBackgroundColor(darker);
                            }
                        });
                    }
                });
            }
        }else {
            showMiniPlayer(false);
        }
    }

    private void updatePlayPauseButton(boolean isPlaying){
        if(isPlaying){
            miniPlayPause.setImageDrawable(new IconicsDrawable(MainActivity.this,GoogleMaterial.Icon.gmd_pause));
            fullPlayPause.setImageDrawable(new IconicsDrawable(MainActivity.this,GoogleMaterial.Icon.gmd_pause));
        }else {
            miniPlayPause.setImageDrawable(new IconicsDrawable(MainActivity.this,GoogleMaterial.Icon.gmd_play_arrow));
            fullPlayPause.setImageDrawable(new IconicsDrawable(MainActivity.this,GoogleMaterial.Icon.gmd_play_arrow));
        }
    }
    private void updateProgress(long progress, long total){
        miniProgress.setMax((int)total);
        seekBar.setMax((int)total);

        time1.setText(Helper.formatTime(progress));
        time2.setText(Helper.formatTime(total));

        if(!seeking){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                miniProgress.setProgress((int)progress,true);
                seekBar.setProgress((int)progress,true);
            }else {
                miniProgress.setProgress((int)progress);
                seekBar.setProgress((int)progress);
            }
        }
    }

    private void showMiniPlayer(boolean show){
        if(show){
            slidingUpPanelLayout.setPanelHeight((int)Helper.pxFromDp(this,65));
        }else {
            slidingUpPanelLayout.setPanelHeight(0);
        }
    }
}

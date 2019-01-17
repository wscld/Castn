package com.wslclds.castn.activities;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.CardView;
import android.text.Html;
import android.text.format.DateUtils;
import android.text.util.Linkify;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.gson.Gson;
import com.liuguangqiang.swipeback.SwipeBackLayout;
import com.mikepenz.fastadapter.IItem;

import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

import com.wslclds.castn.builders.AlertWithInputBuilder;
import com.wslclds.castn.extensions.LinkTransformationMethod;
import com.wslclds.castn.factory.objects.PlaylistEpisode;
import com.wslclds.castn.helpers.ThemeHelper;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.wslclds.castn.extensions.SquareImageView;
import com.wslclds.castn.factory.DatabaseManager;
import com.wslclds.castn.factory.objects.Episode;
import com.wslclds.castn.factory.objects.Playlist;
import com.wslclds.castn.GlideApp;
import com.wslclds.castn.helpers.Helper;
import com.wslclds.castn.builders.AlertWithListBuilder;
import com.wslclds.castn.helpers.URLImageParser;
import com.wslclds.castn.items.PlaylistItem;
import com.wslclds.castn.R;

public class EpisodeDetailActivity extends AppCompatActivity implements SwipeBackLayout.SwipeBackListener {

    boolean justDescription;
    Episode episode;
    String playlistId;
    DatabaseManager databaseManager;
    ThemeHelper themeHelper;

    @BindView(R.id.title)
    TextView title;
    @BindView(R.id.fullDescription)
    TextView fullDescription;
    @BindView(R.id.fullImage)
    SquareImageView fullImage;
    @BindView(R.id.fullTitle)
    TextView fullTitle;
    @BindView(R.id.status)
    TextView statusBadge;
    @BindView(R.id.date)
    TextView date;
    @BindView(R.id.duration)
    TextView duration;
    @BindView(R.id.image)
    ImageView image;
    @BindView(R.id.play)
    ImageButton play;
    @BindView(R.id.episodeCard)
    CardView episodeCard;
    @BindView(R.id.mainCard)
    CardView mainCard;
    @BindView(R.id.swipeBackLayout)
    SwipeBackLayout swipeBackLayout;
    @BindView(R.id.addToPlaylist)
    Button addToPlaylist;
    @BindView(R.id.download)
    Button download;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        themeHelper = new ThemeHelper(this);
        themeHelper.apply(false);
        setContentView(R.layout.activity_episode_detail);
        ButterKnife.bind(this);
        setDragEdge(SwipeBackLayout.DragEdge.TOP);
        playlistId = getIntent().getStringExtra("playlistId");
        justDescription = getIntent().getBooleanExtra("justDescription",false);

        databaseManager = new DatabaseManager(this);

        if(playlistId != null){
            play.setVisibility(View.GONE);
            addToPlaylist.setText("remove from playlist");
        }

        @SuppressLint("StaticFieldLeak") AsyncTask asyncTask = new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] objects) {
                episode = new Gson().fromJson(getIntent().getStringExtra("episode"),Episode.class);
                Episode tempEp = databaseManager.getEpisode(episode.getEnclosureUrl());
                if(tempEp != null){
                    episode = tempEp;
                }
                return null;
            }

            @Override
            protected void onPostExecute(Object o) {
                int color = databaseManager.getColorForPodcast(episode.getUrl());
                int status = databaseManager.getDownloadStatus(episode.getEnclosureUrl());

                title.setText(episode.getTitle());
                fullTitle.setText(episode.getTitle());

                if(status == Helper.STATE_DOWNLOADED){
                    statusBadge.setText("DOWNLOADED");
                    setDownloadEnabled(false);
                }else if(status == Helper.STATE_QUEUED){
                    setDownloadEnabled(false);
                }else {
                    setDownloadEnabled(true);
                }

                //date.setText(DateUtils.getRelativeTimeSpanString(episode.getPubDate(),new Date().getTime(),DateUtils.MINUTE_IN_MILLIS).toString());
                duration.setText(episode.getDuration());

                if(color != 0){
                    GlideApp.with(EpisodeDetailActivity.this).load(episode.getImage()).override(200,200).centerCrop().into(image);
                    episodeCard.setCardBackgroundColor(Helper.darker(color,0.7f));
                    if(justDescription){
                        mainCard.setBackgroundColor(Helper.darker(color,0.6f));
                    }
                }else {
                    GlideApp.with(EpisodeDetailActivity.this).asBitmap().load(episode.getImage()).override(200,200).into(new SimpleTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                            GlideApp.with(EpisodeDetailActivity.this).load(resource).override(200,200).centerCrop().into(image);
                            Palette.from(resource).generate(new Palette.PaletteAsyncListener() {
                                @Override
                                public void onGenerated(@NonNull Palette palette) {
                                    int color = palette.getDominantColor(getResources().getColor(R.color.colorPrimary));
                                    episodeCard.setCardBackgroundColor(Helper.darker(color,0.7f));
                                    if(justDescription){
                                        mainCard.setBackgroundColor(Helper.darker(color,0.6f));
                                    }
                                }
                            });
                        }
                    });
                }
                GlideApp.with(EpisodeDetailActivity.this).load(episode.getImage()).override(400,400).into(fullImage);

                play.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        databaseManager.addEpisodeToPlaylist(episode,Helper.DEFAULT_PLAYLIST_ID);
                        setResult(RESULT_OK);
                        finish();
                    }
                });

                download.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        new Helper(EpisodeDetailActivity.this).makeDownload(episode);
                        setDownloadEnabled(false);
                    }
                });

                addToPlaylist.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(playlistId == null){
                            ArrayList<IItem> items = new ArrayList<>();
                            ArrayList<Playlist> playlists = databaseManager.getPlaylists();
                            for(Playlist playlist : playlists){
                                PlaylistItem playlistItem = new PlaylistItem(playlist);
                                items.add(playlistItem);
                            }
                            AlertWithListBuilder alertWithListBuilder = new AlertWithListBuilder(EpisodeDetailActivity.this, items, null, null,true, new AlertWithListBuilder.OnAction() {
                                @Override
                                public void onClick(IItem item, int position) {
                                    if(position == 0){
                                        AlertWithInputBuilder alertWithInputBuilder = new AlertWithInputBuilder(EpisodeDetailActivity.this, "Create new playlist", null,"Playlist name",null, new AlertWithInputBuilder.OnAction() {
                                            @Override
                                            public void OnSubmit(String string) {
                                                Playlist playlist = Helper.createPlaylistObject(string,true);
                                                databaseManager.createPlaylist(playlist);

                                                PlaylistEpisode playlistEpisode = createPlaylistEpisodeObject(playlist.getId());
                                                databaseManager.addEpisodeToPlaylist(playlistEpisode);
                                                
                                            }
                                        });
                                        alertWithInputBuilder.show();
                                    }else {
                                        PlaylistItem playlistItem = (PlaylistItem)item;
                                        PlaylistEpisode playlistEpisode = createPlaylistEpisodeObject(playlistItem.getPlaylist().getId());
                                        databaseManager.addEpisodeToPlaylist(playlistEpisode);
                                    }
                                }
                            });
                            alertWithListBuilder.show();
                        }else {
                            Toast.makeText(EpisodeDetailActivity.this,"Episode removed",Toast.LENGTH_SHORT).show();
                            databaseManager.removeFromPlaylist(episode.getEnclosureUrl(),playlistId);
                            setResult(RESULT_OK);
                            finish();
                        }
                    }
                });
                loadDescription();
            }
        };
        asyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        if(justDescription){
            addToPlaylist.setVisibility(View.GONE);
            download.setVisibility(View.GONE);
            episodeCard.setVisibility(View.GONE);
            fullDescription.setTextColor(Color.WHITE);
            fullTitle.setTextColor(Color.WHITE);
        }
    }

    private void loadDescription(){
        URLImageParser URLImageParser = new URLImageParser(EpisodeDetailActivity.this,fullDescription);
        fullDescription.setTransformationMethod(new LinkTransformationMethod(Linkify.WEB_URLS | Linkify.EMAIL_ADDRESSES | Linkify.PHONE_NUMBERS));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            fullDescription.setText(Html.fromHtml(episode.getDescription(),Html.FROM_HTML_MODE_COMPACT, URLImageParser,null));
        }else {
            fullDescription.setText(Html.fromHtml(episode.getDescription()));
        }
    }

    private PlaylistEpisode createPlaylistEpisodeObject(String playlistId){
        PlaylistEpisode playlistEpisode = new PlaylistEpisode();
        playlistEpisode.setId(UUID.randomUUID().toString());
        playlistEpisode.setEpisodeJson(new Gson().toJson(episode));
        playlistEpisode.setEnclosureUrl(episode.getEnclosureUrl());
        playlistEpisode.setUrl(episode.getUrl());
        playlistEpisode.setPosition(0);
        playlistEpisode.setPlaylistId(playlistId);

        return playlistEpisode;
    }

    private void setDownloadEnabled(boolean enabled){
        if(enabled){
            download.setEnabled(true);
            download.setAlpha(1.0f);
        }else {
            download.setEnabled(false);
            download.setAlpha(0.4f);
        }
    }

    private void addToPlaylist(){
        ArrayList<Playlist> playlists = databaseManager.getPlaylists();
        ArrayList<IItem> playlistItems = new ArrayList<>();
        for(Playlist playlist : playlists){
            PlaylistItem playlistItem = new PlaylistItem(playlist);
            playlistItems.add(playlistItem);
        }

        AlertWithListBuilder alertWithListBuilder = new AlertWithListBuilder(this, playlistItems, "Select playlist", null,true, new AlertWithListBuilder.OnAction() {
            @Override
            public void onClick(IItem item, int position) {
                PlaylistItem playlistItem = (PlaylistItem)item;
                databaseManager.addEpisodeToPlaylist(episode,playlistItem.getPlaylist().getId());
            }
        });
        alertWithListBuilder.show();
    }

    public void setDragEdge(SwipeBackLayout.DragEdge dragEdge) {
        swipeBackLayout.setDragEdge(dragEdge);
    }

    @Override
    public void onViewPositionChanged(float fractionAnchor, float fractionScreen) {
    }
}

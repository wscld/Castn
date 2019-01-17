package com.wslclds.castn.activities;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.CardView;
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
import com.wslclds.castn.GlideApp;
import com.wslclds.castn.R;
import com.wslclds.castn.builders.AlertWithInputBuilder;
import com.wslclds.castn.builders.AlertWithListBuilder;
import com.wslclds.castn.extensions.SquareImageView;
import com.wslclds.castn.factory.DatabaseManager;
import com.wslclds.castn.factory.objects.Episode;
import com.wslclds.castn.factory.objects.Playlist;
import com.wslclds.castn.factory.objects.PlaylistEpisode;
import com.wslclds.castn.factory.objects.Podcast;
import com.wslclds.castn.helpers.Helper;
import com.wslclds.castn.helpers.ThemeHelper;
import com.wslclds.castn.items.PlaylistItem;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PodcastDetailActivity extends AppCompatActivity implements SwipeBackLayout.SwipeBackListener{

    Podcast podcast;
    DatabaseManager databaseManager;
    ThemeHelper themeHelper;

    @BindView(R.id.fullDescription)
    TextView fullDescription;
    @BindView(R.id.fullTitle)
    TextView fullTitle;
    @BindView(R.id.mainCard)
    CardView mainCard;
    @BindView(R.id.swipeBackLayout)
    SwipeBackLayout swipeBackLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        themeHelper = new ThemeHelper(this);
        themeHelper.apply(false);
        setContentView(R.layout.activity_podcast_detail);
        ButterKnife.bind(this);
        databaseManager = new DatabaseManager(this);


        fullDescription.setTextColor(Color.WHITE);
        fullTitle.setTextColor(Color.WHITE);


        @SuppressLint("StaticFieldLeak") AsyncTask asyncTask = new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] objects) {
                podcast = new Gson().fromJson(getIntent().getStringExtra("podcast"),Podcast.class);
                return null;
            }

            @Override
            protected void onPostExecute(Object o) {
                int color = databaseManager.getColorForPodcast(podcast.getUrl());

                fullTitle.setText(podcast.getTitle());
                fullDescription.setText(podcast.getDescription());

                if(color != 0){
                    mainCard.setBackgroundColor(Helper.darker(color,0.6f));
                }else {
                    GlideApp.with(PodcastDetailActivity.this).asBitmap().load(podcast.getImage()).override(200,200).into(new SimpleTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                            Palette.from(resource).generate(new Palette.PaletteAsyncListener() {
                                @Override
                                public void onGenerated(@NonNull Palette palette) {
                                    int color = palette.getDominantColor(getResources().getColor(R.color.colorPrimary));
                                    mainCard.setBackgroundColor(Helper.darker(color,0.6f));
                                }
                            });
                        }
                    });
                }
            }
        };
        asyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public void setDragEdge(SwipeBackLayout.DragEdge dragEdge) {
        swipeBackLayout.setDragEdge(dragEdge);
    }

    @Override
    public void onViewPositionChanged(float fractionAnchor, float fractionScreen) {
    }
}

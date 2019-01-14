package com.wslclds.castn.activities;

import android.graphics.Color;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.paolorotolo.appintro.AppIntro;
import com.github.paolorotolo.appintro.AppIntroFragment;
import com.github.paolorotolo.appintro.model.SliderPage;
import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.wslclds.castn.R;
import com.wslclds.castn.fragments.SlideFragment;
import com.wslclds.castn.helpers.ThemeHelper;

public class IntroActivity extends AppIntro {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ThemeHelper themeHelper = new ThemeHelper(this);
        themeHelper.apply(true);
        setBarColor(getResources().getColor(R.color.colorPrimaryDark));


        SlideFragment slideFragment1 = SlideFragment.newInstance(
                "Timeline",
                "Easily catch up with the latest episodes from your favorite podcasts",
                "cmd_format_float_left",
                getResources().getColor(R.color.colorPrimary),
                Color.DKGRAY
        );
        addSlide(slideFragment1);

        SlideFragment slideFragment2 = SlideFragment.newInstance(
                "Subscriptions",
                "All your subscriptions easily grouped in one page",
                "cmd_view_module",
                getResources().getColor(R.color.colorPrimary),
                Color.DKGRAY
        );
        addSlide(slideFragment2);

        SlideFragment slideFragment3 = SlideFragment.newInstance(
                "Downloads",
                "Download your favorite episodes to listen offline",
                "cmd_download",
                getResources().getColor(R.color.colorPrimary),
                Color.DKGRAY
        );
        addSlide(slideFragment3);

        SlideFragment slideFragment4 = SlideFragment.newInstance(
                "Find new podcasts",
                "Easily search and discover new podcasts",
                "cmd_earth",
                getResources().getColor(R.color.colorPrimary),
                Color.DKGRAY
        );
        addSlide(slideFragment4);

    }

    @Override
    public void onDonePressed(Fragment currentFragment) {
        super.onDonePressed(currentFragment);
        finish();
    }

    @Override
    public void onSkipPressed(Fragment currentFragment) {
        super.onSkipPressed(currentFragment);
        finish();
    }
}

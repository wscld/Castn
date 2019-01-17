package com.wslclds.castn.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.liuguangqiang.swipeback.SwipeBackLayout;
import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.wslclds.castn.R;
import com.wslclds.castn.builders.AlertBuilder;
import com.wslclds.castn.helpers.ThemeHelper;

import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SleepTimerActivity extends AppCompatActivity implements SwipeBackLayout.SwipeBackListener {

    ThemeHelper themeHelper;

    @BindView(R.id.swipeBackLayout)
    SwipeBackLayout swipeBackLayout;
    @BindView(R.id.currentTime)
    TextView currentTime;
    @BindView(R.id.setTime)
    Button setTimeButton;
    @BindView(R.id.cancel)
    Button cancelButton;
    @BindView(R.id.icon)
    ImageView icon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        themeHelper = new ThemeHelper(this);
        themeHelper.apply(false);
        setContentView(R.layout.activity_sleep_timer);
        ButterKnife.bind(this);
        long time = getIntent().getLongExtra("time",0);

        icon.setImageDrawable(new IconicsDrawable(this,CommunityMaterial.Icon.cmd_sleep));
        icon.setColorFilter(themeHelper.getTextColor());
        if(time == 0){
            currentTime.setText("No sleep time set");
        }else {
            currentTime.setText((DateUtils.getRelativeTimeSpanString(time,new Date().getTime(),DateUtils.MINUTE_IN_MILLIS)).toString());
        }

        setTimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertBuilder(SleepTimerActivity.this, "Set minutes", null, new AlertBuilder.onButtonClick3() {
                    @Override
                    public void onConfirm(long l) {
                        long time = l*60000;

                        Intent data = new Intent();
                        data.putExtra("result",(long)time);
                        setResult(RESULT_OK,data);
                        finish();
                    }
                }).show();
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent data = new Intent();
                data.putExtra("result",(long)1);
                setResult(RESULT_OK,data);
                finish();
            }
        });

    }

    public void setDragEdge(SwipeBackLayout.DragEdge dragEdge) {
        swipeBackLayout.setDragEdge(dragEdge);
    }

    @Override
    public void onViewPositionChanged(float fractionAnchor, float fractionScreen) {
    }
}

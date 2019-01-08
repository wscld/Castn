package com.wslclds.castn.helpers;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Html;
import android.widget.TextView;

import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;

import java.util.ArrayList;

import com.wslclds.castn.GlideApp;
import com.wslclds.castn.GlideRequest;

public class URLImageParser implements Html.ImageGetter {

    ArrayList<Target> targets;
    final TextView textView;
    Context context;

    public URLImageParser(Context context , TextView textView){
        this.textView = textView;
        this.context = context;
        this.targets = new ArrayList<>();
    }

    @Override
    public Drawable getDrawable(String url) {
        final UrlDrawable urlDrawable = new UrlDrawable();
        final GlideRequest load = GlideApp.with(context).asBitmap().load(url);
        final Target target = new BitmapTarget(urlDrawable);
        targets.add(target);
        load.into(target);
        return urlDrawable;
    }

    private class BitmapTarget extends SimpleTarget<Bitmap> {

        Drawable drawable;
        private final UrlDrawable urlDrawable;
        public BitmapTarget(UrlDrawable urlDrawable) {
            this.urlDrawable = urlDrawable;
        }

        @Override
        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
            drawable = new BitmapDrawable(context.getResources(), resource);

            textView.post(new Runnable() {
                @Override
                public void run() {
                    int w = textView.getWidth();
                    int hh=drawable.getIntrinsicHeight();
                    int ww=drawable.getIntrinsicWidth() ;
                    int newHeight = hh * ( w  )/ww;
                    Rect rect = new Rect( 0 , 0 , w  ,newHeight);
                    drawable.setBounds(rect);
                    urlDrawable.setBounds(rect);
                    urlDrawable.setDrawable(drawable);
                    textView.setText(textView.getText());
                    textView.invalidate();
                }
            });
        }
    }

    class UrlDrawable extends BitmapDrawable{
        private Drawable drawable;

        @SuppressWarnings("deprecation")
        public UrlDrawable() {
        }
        @Override
        public void draw(Canvas canvas) {
            if (drawable != null)
                drawable.draw(canvas);
        }
        public Drawable getDrawable() {
            return drawable;
        }
        public void setDrawable(Drawable drawable) {
            this.drawable = drawable;
        }
    }

}
package com.wslclds.castn.builders;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;

import java.util.ArrayList;
import java.util.List;

import com.wslclds.castn.GlideApp;
import com.wslclds.castn.R;

public class CoverBuilder {
    OnLoad onLoad;

    public CoverBuilder(Context context, List<String> images, OnLoad onLoad){
        this.onLoad = onLoad;
        ArrayList<String> tempImages = new ArrayList<>();
        tempImages.add("");
        tempImages.add("");
        tempImages.add("");
        tempImages.add("");

        if(images.size() >=4){
           images = images.subList(0,4);
        }

        for(int i = 0; i < images.size(); i++){
            tempImages.set(i,images.get(i));
        }

        images.clear();
        images.addAll(tempImages);

        ArrayList<Bitmap> bitmaps = new ArrayList<>();
        for(String image : images){
            List<String> finalImages = images;
            GlideApp.with(context).asBitmap().override(500,500).load(image).centerCrop().into(new SimpleTarget<Bitmap>() {
                @Override
                public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                    @SuppressLint("StaticFieldLeak") AsyncTask asyncTask = new AsyncTask() {
                        @Override
                        protected Object doInBackground(Object[] objects) {
                            bitmaps.add(resource);
                            return null;
                        }

                        @Override
                        protected void onPostExecute(Object o) {
                            checkBitmaps(bitmaps, finalImages);
                        }
                    };
                    asyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }

                @Override
                public void onLoadFailed(@Nullable Drawable errorDrawable) {
                    super.onLoadFailed(errorDrawable);
                    @SuppressLint("StaticFieldLeak") AsyncTask asyncTask = new AsyncTask() {
                        @Override
                        protected Object doInBackground(Object[] objects) {
                            bitmaps.add(BitmapFactory.decodeResource(context.getResources(), R.drawable.castn_icon));
                            return null;
                        }

                        @Override
                        protected void onPostExecute(Object o) {
                            checkBitmaps(bitmaps, finalImages);
                        }
                    };
                    asyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }
            });
        }
    }

    private void checkBitmaps(ArrayList<Bitmap> bitmaps, List<String> images){
        if(bitmaps.size() == images.size()){
            @SuppressLint("StaticFieldLeak") AsyncTask asyncTask = new AsyncTask() {
                Bitmap bitmap;
                @Override
                protected Object doInBackground(Object[] objects) {
                    bitmap = combineImages(bitmaps.get(0),bitmaps.get(1),bitmaps.get(2),bitmaps.get(3));
                    return null;
                }

                @Override
                protected void onPostExecute(Object o) {
                    onLoad.onImageLoaded(bitmap);
                }
            };
            asyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    public Bitmap combineImages(Bitmap c, Bitmap s, Bitmap c2, Bitmap s2) {
        Bitmap cs = null;

        if(c != null && s != null) {
            int width, height = 0;

            width = s.getWidth() + s.getWidth();
            height = c.getHeight() + c.getHeight();

            cs = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

            Canvas comboImage = new Canvas(cs);

            comboImage.drawBitmap(c, 0f, 0f, null);
            comboImage.drawBitmap(s, c.getWidth(), 0f, null);
            if (c2 != null)
                comboImage.drawBitmap(c2, 0, c.getHeight(), null);
            if (s2 != null)
                comboImage.drawBitmap(s2, c.getWidth(), c.getHeight(), null);
        }
        return cs;
    }

    public interface OnLoad{
        void onImageLoaded(Bitmap bitmap);
    }
}

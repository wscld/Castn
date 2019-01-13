package com.wslclds.castn.factory;

import android.content.Context;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.Date;

import com.wslclds.castn.factory.objects.Feed;

public class DataGetter {
    private Context context;
    private static int NOW = 0;
    private static int THREE_SECONDS = 1000*60*10;
    private static int TEN_MINUTES = 1000*60*10;
    private static int TWELVE_MINUTES = 1000*60*20;
    private static int ONE_HOUR = 1000*60*60;
    private static int TWO_HOURS = 1000*60*60*2;

    public DataGetter(Context context){
        this.context = context;
    }

    public String getFeed(String url){
        String feed = "";
        Document doc;
        DatabaseManager databaseManager = new DatabaseManager(context);

        Feed feedObject = databaseManager.getCachedFeed(url);

        if(feedObject == null){
            try {
                doc = Jsoup.connect(url).userAgent("Mozilla/5.0 AppleWebKit/537.36 (KHTML, like Gecko) Chrome/45.0.2454.4 Safari/537.36").ignoreContentType(true).timeout(THREE_SECONDS).get();
                feed = doc.toString();
                databaseManager.cacheFeed(url,feed);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else  if(feedObject != null && feedObject.getDate() > 0 && new Date().getTime() - feedObject.getDate() > TEN_MINUTES){
            try {
                doc = Jsoup.connect(url).userAgent("Mozilla/5.0 AppleWebKit/537.36 (KHTML, like Gecko) Chrome/45.0.2454.4 Safari/537.36").ignoreContentType(true).timeout(THREE_SECONDS).get();
                feed = doc.toString();
                databaseManager.cacheFeed(url,feed);
            } catch (IOException e) {
                e.printStackTrace();
                feed = feedObject.getFeed();
            }
        }else {
            feed = feedObject.getFeed();
        }
        return feed;
    }

    public String getFeed(String url, long cacheTime){
        String feed = "";
        Document doc;
        DatabaseManager databaseManager = new DatabaseManager(context);

        Feed feedObject = databaseManager.getCachedFeed(url);

        if(feedObject == null){
            try {
                doc = Jsoup.connect(url).userAgent("Mozilla/5.0 AppleWebKit/537.36 (KHTML, like Gecko) Chrome/45.0.2454.4 Safari/537.36").ignoreContentType(true).timeout(THREE_SECONDS).get();
                feed = doc.toString();
                databaseManager.cacheFeed(url,feed);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else  if(feedObject != null && feedObject.getDate() > 0 && new Date().getTime() - feedObject.getDate() > cacheTime){
            try {
                doc = Jsoup.connect(url).userAgent("Mozilla/5.0 AppleWebKit/537.36 (KHTML, like Gecko) Chrome/45.0.2454.4 Safari/537.36").ignoreContentType(true).timeout(THREE_SECONDS).get();
                feed = doc.toString();
                databaseManager.cacheFeed(url,feed);
            } catch (IOException e) {
                e.printStackTrace();
                feed = feedObject.getFeed();
            }
        }else {
            feed = feedObject.getFeed();
        }
        return feed;
    }

    public String getJson(String url){
        String feed = "";
        DatabaseManager databaseManager = new DatabaseManager(context);

        Feed feedObject = databaseManager.getCachedFeed(url);

        if(feedObject == null){
            try {
                feed = Jsoup.connect(url).userAgent("Mozilla/5.0 AppleWebKit/537.36 (KHTML, like Gecko) Chrome/45.0.2454.4 Safari/537.36").ignoreContentType(true).timeout(THREE_SECONDS).execute().body();
                databaseManager.cacheFeed(url,feed);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else  if(feedObject != null && feedObject.getDate() > 0 && new Date().getTime() - feedObject.getDate() > TEN_MINUTES){
            try {
                feed = Jsoup.connect(url).userAgent("Mozilla/5.0 AppleWebKit/537.36 (KHTML, like Gecko) Chrome/45.0.2454.4 Safari/537.36").ignoreContentType(true).timeout(THREE_SECONDS).execute().body();
                databaseManager.cacheFeed(url,feed);
            } catch (IOException e) {
                e.printStackTrace();
                feed = feedObject.getFeed();
            }
        }else {
            feed = feedObject.getFeed();
        }
        return feed;
    }
}

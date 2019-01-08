package com.wslclds.castn.factory.dataRetriever;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;

import com.wslclds.castn.factory.DataGetter;
import com.wslclds.castn.factory.objects.Podcast;

public class SearchApi {
    Context context;
    public SearchApi(Context context){
        this.context = context;
    }

    public ArrayList<Podcast> findPodcastWithTerm(String term){
        DataGetter dataGetter = new DataGetter(context);
        ArrayList<Podcast> podcasts = new ArrayList<>();
        try {
            String apiUrl = "https://itunes.apple.com/search?media=podcast&entity=podcast&term="+URLEncoder.encode(term,"utf-8");
            String result = Jsoup.parse(dataGetter.getFeed(apiUrl)).body().text();

            JSONObject jsonObject = new JSONObject(result);
            JSONArray resultsArray = jsonObject.getJSONArray("results");
            for(int i = 0; i < resultsArray.length(); i++){
                JSONObject object = resultsArray.getJSONObject(i);
                Podcast podcast = new Podcast();
                podcast.setAuthor(object.getString("artistName"));
                podcast.setTitle(object.getString("collectionName"));
                podcast.setImage(object.getString("artworkUrl100"));
                podcast.setUrl(object.getString("feedUrl"));
                podcasts.add(podcast);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return podcasts;
    }

    public ArrayList<Podcast> findPodcastWithAuthor(String author){
        DataGetter dataGetter = new DataGetter(context);
        ArrayList<Podcast> podcasts = new ArrayList<>();
        try {
            String apiUrl = "https://itunes.apple.com/search?media=podcast&entity=podcastAuthor&term="+URLEncoder.encode(author,"utf-8");
            String result = dataGetter.getFeed(apiUrl);

            JSONObject jsonObject = new JSONObject(result);
            JSONArray resultsArray = jsonObject.getJSONArray("results");
            for(int i = 0; i < resultsArray.length(); i++){
                JSONObject object = resultsArray.getJSONObject(i);
                Podcast podcast = new Podcast();
                podcast.setAuthor(object.getString("artistName"));
                podcast.setTitle(object.getString("collectionName"));
                podcast.setImage(object.getString("artworkUrl100"));
                podcast.setUrl(object.getString("feedUrl"));
                podcasts.add(podcast);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return podcasts;
    }
}

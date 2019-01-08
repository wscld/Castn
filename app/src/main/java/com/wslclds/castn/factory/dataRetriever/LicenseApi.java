package com.wslclds.castn.factory.dataRetriever;

import android.content.Context;
import android.net.Uri;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;

import com.wslclds.castn.factory.DataGetter;

public class LicenseApi {
    Context context;

    public LicenseApi(Context context){
        this.context = context;
    }



    public String getLicenseFromGithub(String url){
        Uri uri = Uri.parse(url);
        String author = uri.getPathSegments().get(0);
        String repo = uri.getPathSegments().get(1);
        String license = url;

        DataGetter dataGetter = new DataGetter(context);
        try {
            String apiUrl = "https://api.github.com/repos/"+author+"/"+repo+"/license";
            String result = Jsoup.parse(dataGetter.getFeed(apiUrl)).body().text();

            JSONObject jsonObject = new JSONObject(result);
            license = Jsoup.parse(dataGetter.getFeed(jsonObject.getString("download_url"))).body().text();

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return license;
    }
}

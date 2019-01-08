package com.wslclds.castn.factory.objects;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class PodcastUpload extends RealmObject {
    @PrimaryKey
    String id;
    String url;
    long date;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }
}

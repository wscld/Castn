package com.wslclds.castn.factory.objects;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Download extends RealmObject {

    @PrimaryKey
    private String id;
    private String enclosureUrl;
    private String localPath;
    private String image;
    private String title;
    private String episodeJson;
    private boolean completed;
    private long date;

    public String getEpisodeJson() {
        return episodeJson;
    }

    public void setEpisodeJson(String episodeJson) {
        this.episodeJson = episodeJson;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public String getEnclosureUrl() {
        return enclosureUrl;
    }

    public void setEnclosureUrl(String enclosureUrl) {
        this.enclosureUrl = enclosureUrl;
    }

    public String getLocalPath() {
        return localPath;
    }

    public void setLocalPath(String localPath) {
        this.localPath = localPath;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}

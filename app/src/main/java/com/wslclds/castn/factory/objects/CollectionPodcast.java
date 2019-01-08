package com.wslclds.castn.factory.objects;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class CollectionPodcast extends RealmObject {
    @PrimaryKey
    private String id;
    private String collectionId;
    private String podcastJson;
    private String podcastUrl;
    private int position;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCollectionId() {
        return collectionId;
    }

    public void setCollectionId(String collectionId) {
        this.collectionId = collectionId;
    }

    public String getPodcastJson() {
        return podcastJson;
    }

    public void setPodcastJson(String podcastJson) {
        this.podcastJson = podcastJson;
    }

    public String getPodcastUrl() {
        return podcastUrl;
    }

    public void setPodcastUrl(String podcastUrl) {
        this.podcastUrl = podcastUrl;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }
}

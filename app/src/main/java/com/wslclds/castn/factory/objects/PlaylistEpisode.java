package com.wslclds.castn.factory.objects;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class PlaylistEpisode extends RealmObject {
    @PrimaryKey
    private String id;
    private String playlistId;
    private String episodeJson;
    private String enclosureUrl;
    private String url;
    private int position;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPlaylistId() {
        return playlistId;
    }

    public void setPlaylistId(String playlistId) {
        this.playlistId = playlistId;
    }

    public String getEpisodeJson() {
        return episodeJson;
    }

    public void setEpisodeJson(String episodeJson) {
        this.episodeJson = episodeJson;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public String getEnclosureUrl() {
        return enclosureUrl;
    }

    public void setEnclosureUrl(String enclosureUrl) {
        this.enclosureUrl = enclosureUrl;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public boolean equals(Object obj) {
        if(((PlaylistEpisode)obj).getId().equals(getId())){
            return true;
        }else {
            return false;
        }
    }
}

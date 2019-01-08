package com.wslclds.castn.factory.objects;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Playlist extends RealmObject {
    @PrimaryKey
    private String id;
    private String name;
    private long date;
    private boolean publicAvailable;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isPublicAvailable() {
        return publicAvailable;
    }

    public void setPublicAvailable(boolean publicAvailable) {
        this.publicAvailable = publicAvailable;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    @Override
    public boolean equals(Object obj) {
        if(((Playlist)obj).getId().equals(getId())){
            return true;
        }else {
            return false;
        }
    }
}

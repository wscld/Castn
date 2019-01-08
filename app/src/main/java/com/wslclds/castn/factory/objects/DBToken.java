package com.wslclds.castn.factory.objects;

import io.realm.RealmObject;

public class DBToken extends RealmObject {
    private String token;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}

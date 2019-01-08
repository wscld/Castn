package com.wslclds.castn.factory.objects;

import io.realm.RealmObject;

public class DeviceId extends RealmObject {
    private String deviceId;

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }
}

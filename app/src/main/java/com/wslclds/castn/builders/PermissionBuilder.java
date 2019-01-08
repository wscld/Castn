package com.wslclds.castn.builders;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

public class PermissionBuilder {
    public PermissionBuilder(Activity activity, String permissionString){
        int permission = ContextCompat.checkSelfPermission(activity, permissionString);
        if(permission != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(activity, new String[]{permissionString},0);
        }
    }
}

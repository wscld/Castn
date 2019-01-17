package com.wslclds.castn.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;


public class PlayerKillerService extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        System.out.println("killerr!!!!");
        Intent serviceIntent = new Intent(context,AudioPlayerService.class);
        serviceIntent.setAction(AudioPlayerService.ACTION_CLOSE);
        context.startService(serviceIntent);
    }
}

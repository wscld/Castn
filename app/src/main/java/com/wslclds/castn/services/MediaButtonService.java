package com.wslclds.castn.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.media.session.MediaButtonReceiver;
import android.view.KeyEvent;

public class MediaButtonService extends BroadcastReceiver {
    public MediaButtonService(){
        super();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_MEDIA_BUTTON.equals(intent.getAction())) {
            KeyEvent event = (KeyEvent) intent .getParcelableExtra(Intent.EXTRA_KEY_EVENT);

            if (event == null) {
                return;
            }

            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                Intent serviceIntent = new Intent(context,AudioPlayerService.class);
                serviceIntent.setAction(AudioPlayerService.ACTION_PLAY_PAUSE);
                context.startService(serviceIntent);
            }
        }
    }
}

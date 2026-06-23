package com.nass.ek.w3kiosk;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.view.KeyEvent;

public class MediaButtonReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_MEDIA_BUTTON.equals(intent.getAction())) {
            KeyEvent event = intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
            if (event != null && event.getAction() == KeyEvent.ACTION_DOWN) {
                Intent forward = new Intent("com.nass.ek.w3kiosk.MEDIA_BUTTON");
                forward.putExtra("keyCode", event.getKeyCode());
                forward.putExtra("scanCode", event.getScanCode());
                context.sendBroadcast(forward);
            }
            // Broadcast abortieren – verhindert, dass das System ihn weiterverarbeitet
            abortBroadcast();
        }
    }
}

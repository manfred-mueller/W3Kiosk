package com.nass.ek.w3kiosk;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

public class StatusReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String connType = ChecksAndConfigs.connectionType(context);
        String clientUrl = sharedPreferences.getString("clientUrl1", "w3c");
        String deviceId = sharedPreferences.getString("devId", "");
        if (deviceId.isEmpty()) {
            deviceId = SettingsActivity.readConfigFileContents();
        }
        StatusSender.sendData(deviceId, clientUrl, connType);
    }
}

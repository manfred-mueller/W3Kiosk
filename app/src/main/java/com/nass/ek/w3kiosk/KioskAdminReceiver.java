package com.nass.ek.w3kiosk;

import android.app.admin.DeviceAdminReceiver;
import android.content.Context;
import android.content.Intent;

public class KioskAdminReceiver extends DeviceAdminReceiver {

    @Override
    public void onEnabled(Context context, Intent intent) { }

    @Override
    public void onDisabled(Context context, Intent intent) { }

    @Override
    public void onLockTaskModeEntering(Context context, Intent intent, String pkg) { }

    @Override
    public void onLockTaskModeExiting(Context context, Intent intent) { }
}

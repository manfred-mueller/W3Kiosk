package com.nass.ek.w3kiosk;

import android.accessibilityservice.AccessibilityService;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.os.Build;
import android.view.accessibility.AccessibilityEvent;

public class ShutdownService extends AccessibilityService {

    public static ShutdownService instance;

    @Override
    public void onServiceConnected() {
        instance = this;
    }

    // Zentraler Neustart - wird von MainActivity und ScannerActivity aufgerufen
    public static void rebootDevice(Context context) {
        // Bevorzugt: DevicePolicyManager als Device Owner (Android 7+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            DevicePolicyManager dpm = (DevicePolicyManager)
                context.getSystemService(Context.DEVICE_POLICY_SERVICE);
            ComponentName admin = new ComponentName(context, KioskAdminReceiver.class);
            if (dpm != null && dpm.isDeviceOwnerApp(context.getPackageName())) {
                dpm.reboot(admin);
                return;
            }
        }
        // Fallback: Accessibility Service Power Menu
        if (instance != null) {
            instance.performGlobalAction(GLOBAL_ACTION_POWER_DIALOG);
            return;
        }
        // Letzter Fallback: Root
        try {
            Runtime.getRuntime().exec(new String[]{"su", "-c", "reboot"});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) { }

    @Override
    public void onInterrupt() { }

    @Override
    public void onDestroy() {
        instance = null;
        super.onDestroy();
    }
}

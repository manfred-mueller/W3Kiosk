package com.nass.ek.w3kiosk;

import android.app.UiModeManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;

import androidx.appcompat.app.AppCompatActivity;

public class ChecksAndConfigs extends AppCompatActivity {
    public static String PW1;
    public static String PW2;
    public static String PW3;
    public static String PW4;


    static {
        try {
            PW1 = AESUtils.decrypt("0220972AE0731AD40F36FCA15AEEAF7B");
            PW2 = AESUtils.decrypt("B4EEA51496774A498A2A1F7D10EF2AAE");
            PW3 = AESUtils.decrypt("CB2BF4EF649D42ACEAC301C660C31262");
            PW4 = AESUtils.decrypt("FF7676CF6F7845B2E429718C5C0AE9BB");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    // detect if network is connected
    public static Boolean isNetworkConnected(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Network nw = connectivityManager.getActiveNetwork();
            if (nw == null) return false;
            NetworkCapabilities actNw = connectivityManager.getNetworkCapabilities(nw);
            return actNw != null && (actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) || actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) || actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) || actNw.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH));
        } else {
            NetworkInfo nwInfo = connectivityManager.getActiveNetworkInfo();
            return nwInfo != null && nwInfo.isConnected();
        }
    }
    public static boolean isScanner() {
        return android.os.Build.MODEL.toUpperCase().startsWith("C4050") || android.os.Build.MODEL.toUpperCase().startsWith("C66") || android.os.Build.MODEL.toUpperCase().startsWith("C72") || android.os.Build.MODEL.toUpperCase().startsWith("C61") || Build.PRODUCT.startsWith("cedric");
    }

    public static boolean isTablet() {
        return android.os.Build.MODEL.toUpperCase().startsWith("RK");
    }

    public static boolean isTv(Context context) {
        UiModeManager uiModeManager =
                (UiModeManager) context.getApplicationContext().getSystemService(UI_MODE_SERVICE);
        return uiModeManager != null
                && uiModeManager.getCurrentModeType() == Configuration.UI_MODE_TYPE_TELEVISION;
    }
    public static boolean checkApps(Context context, String uri) {
        PackageInfo pkgInfo;
        try {
            pkgInfo = context.getPackageManager().getPackageInfo(uri, 0);
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
        return pkgInfo != null;
    }

    public static boolean isRooted() {
        // try executing commands
        return canExecuteCommand("/system/xbin/which su")
                || canExecuteCommand("/system/bin/which su") || canExecuteCommand("which su");
    }
    // executes a command on the system
    static boolean canExecuteCommand(String command) {
        boolean executedSuccesfully;
        try {
            Runtime.getRuntime().exec(command);
            executedSuccesfully = true;
        } catch (Exception e) {
            executedSuccesfully = false;
        }

        return executedSuccesfully;
    }
}

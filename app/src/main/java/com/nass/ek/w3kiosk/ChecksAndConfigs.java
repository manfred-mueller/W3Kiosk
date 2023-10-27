package com.nass.ek.w3kiosk;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Random;

public class ChecksAndConfigs extends AppCompatActivity {
    public static String PW1;
    public static String PW2;
    public static String PW3;
    public static String PW4;
    Context context;

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

    public static String randomId()
    {
        if (isTv()){
            return "TV-" + generateRandomNumber();
        }
        else if (isTablet()){
            return "TB-" + generateRandomNumber();
        }
        if (isScanner()){
            return Build.MODEL.toUpperCase() + generateRandomNumber();
        }
        return "DEV-" + generateRandomNumber();
    }

    public static boolean isScanner() {
        return android.os.Build.MODEL.toUpperCase().startsWith("C4050") || android.os.Build.MODEL.toUpperCase().startsWith("C66") || android.os.Build.MODEL.toUpperCase().startsWith("C72") || android.os.Build.MODEL.toUpperCase().startsWith("C61") || Build.PRODUCT.startsWith("cedric");
    }

    public static boolean isTablet() {
        return android.os.Build.MODEL.toUpperCase().startsWith("RK");
    }

    public static boolean isTv() {
        return android.os.Build.MODEL.toUpperCase().startsWith("TV") || android.os.Build.MODEL.toUpperCase().startsWith("X8");
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

    public static String connectionType(Context context) {
        String result = ""; // Returns connection type. 0: none; 1: mobile data; 2: wifi; 3: vpn
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (cm != null) {
                NetworkCapabilities capabilities = cm.getNetworkCapabilities(cm.getActiveNetwork());
                if (capabilities != null) {
                    if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                        result = "Wifi";
                    } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                        result = "LAN";
                    } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                        result = "Mobile";
                    } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN)) {
                        result = "VPN";
                    }
                }
            }
        } else {
            if (cm != null) {
                NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
                if (activeNetwork != null) {
                    // connected to the internet
                    if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
                        result = "Wifi";
                    } else if (activeNetwork.getType() == ConnectivityManager.TYPE_ETHERNET) {
                        result = "LAN";
                    } else if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
                        result = "Mobile";
                    } else if (activeNetwork.getType() == ConnectivityManager.TYPE_VPN) {
                        result = "VPN";
                    }
                }
            }
        }
        return result;
    }
    private static String generateRandomNumber() {
        Random random = new Random();
        int randomNumber = random.nextInt(1000000); // Change this range as needed
        return String.valueOf(randomNumber);
    }
}

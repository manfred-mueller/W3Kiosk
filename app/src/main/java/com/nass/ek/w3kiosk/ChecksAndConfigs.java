package com.nass.ek.w3kiosk;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.text.format.Formatter;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;
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
        return android.os.Build.MODEL.toUpperCase().startsWith("C4050") || android.os.Build.MODEL.toUpperCase().startsWith("C66") || android.os.Build.MODEL.toUpperCase().startsWith("C72") || android.os.Build.MODEL.toUpperCase().startsWith("C61")|| android.os.Build.MODEL.toUpperCase().startsWith("MC33")|| android.os.Build.MODEL.toUpperCase().startsWith("DT")|| android.os.Build.MODEL.toUpperCase().startsWith("RD");
    }

    public static boolean isTablet() {
        return android.os.Build.MODEL.toUpperCase().startsWith("RK") || android.os.Build.MODEL.toUpperCase().startsWith("PRIME");
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

    public static boolean isRooted() {
        return checkRootMethod1() || checkRootMethod2() || checkRootMethod3();
    }

    private static boolean checkRootMethod1() {
        String buildTags = android.os.Build.TAGS;
        return buildTags != null && buildTags.contains("test-keys");
    }

    private static boolean checkRootMethod2() {
        try {
            File file = new File("/system/app/Superuser.apk");
            return file.exists();
        } catch (Exception e) {
            return false;
        }
    }

    private static boolean checkRootMethod3() {
        Process process = null;
        try {
            process = Runtime.getRuntime().exec(new String[]{"/system/bin/which", "su"});
            BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
            return in.readLine() != null;
        } catch (IOException e) {
            return false;
        } finally {
            if (process != null) {
                try {
                    process.destroy();
                } catch (Exception e) {
                    // Ignore
                }
            }
        }
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
    public static String getIPAddress(Context context) {
        // Check if connected to Wi-Fi
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnected()) {
            if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                // Get IP address when connected to Wi-Fi
                WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                int ipAddress = wifiManager.getConnectionInfo().getIpAddress();
                return Formatter.formatIpAddress(ipAddress);
            } else {
                // Get IP address when connected to cellular or other network types
                return getIPAddressFromNetworkInterface();
            }
        }
        return "";
    }

    // This method retrieves the device's IP address from network interfaces (for cellular networks or other connections)
    private static String getIPAddressFromNetworkInterface() {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface networkInterface : interfaces) {
                List<InetAddress> addresses = Collections.list(networkInterface.getInetAddresses());
                for (InetAddress address : addresses) {
                    if (!address.isLoopbackAddress() && address instanceof InetAddress) {
                        String ipAddress = address.getHostAddress();
                        if (isIPv4Address(ipAddress)) {
                            return ipAddress;
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    // Helper method to check if the IP address is IPv4 (ignore IPv6 for most common cases)
    private static boolean isIPv4Address(String ipAddress) {
        return ipAddress.indexOf(':') < 0;  // IPv6 addresses contain ':'
    }
}

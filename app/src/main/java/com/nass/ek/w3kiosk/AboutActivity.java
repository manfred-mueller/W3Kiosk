package com.nass.ek.w3kiosk;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.nass.ek.appupdate.UpdateWrapper;

import java.text.DateFormat;
import java.util.Date;

public class AboutActivity extends AppCompatActivity {

    public void closeClick(View view) {
        finish();
    }

    WindowManager windowManager;
    DisplayMetrics displayMetrics;
    public String Rooted;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new CountDownTimer(15000, 1000) {
            public void onTick(long millisUntilFinished) {
            }
            public void onFinish() {
                finish();
            }
        }.start();
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        displayMetrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(displayMetrics);
        setContentView(R.layout.activity_about);
        String localVersion = BuildConfig.VERSION_NAME;
        if (ChecksAndConfigs.isRooted()) {
            Rooted = getString(R.string.Yes);
        } else
        {
            Rooted = getString(R.string.No);
        }
        Date buildDate = new Date(Long.parseLong(BuildConfig.BUILD_TIME));
        @SuppressLint("StringFormatMatches") String appinfo=(String.format(getString(R.string.appInfo) , getString(R.string.app_name), localVersion, DateFormat.getDateInstance(DateFormat.MEDIUM).format(buildDate), connectionType(this), Rooted));
        TextView appinfoText = findViewById(R.id.textView3);
        appinfoText.setText(appinfo);
        findViewById(R.id.logo_id).setOnClickListener(view -> checkUpdate());
        checkUpdate();
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

    private void checkUpdate() {

        String updateFound=(String.format(getString(R.string.UpdateAvailable), getString(R.string.app_name)));
        UpdateWrapper updateWrapper = new UpdateWrapper.Builder(AboutActivity.this)
                .setTime(3000)
                .setNotificationIcon(R.mipmap.ic_launcher)
                .setUpdateTitle(updateFound)
                .setUpdateContentText(getString(R.string.UpdateDescription))
                .setUrl("https://raw.githubusercontent.com/manfred-mueller/W3Kiosk/master/w3kiosk.json")
                .setIsShowToast(true)

                .setCallback((model, hasNewVersion) -> {
                    Log.d("Latest Version", hasNewVersion + "");
                    Log.d("Version Name", model.getVersionName());
                    Log.d("Release", model.getVersionCode() + "");
                    Log.d("Version Description", model.getContentText());
                    Log.d("Min Support", model.getMinSupport() + "");
                    Log.d("Download URL", model.getUrl() + "");
                })
                .build();

        updateWrapper.start();
    }
}

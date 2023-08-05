package com.nass.ek.w3kiosk;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.KeyEvent;
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
        String localVersion = BuildConfig.VERSION_NAME + "." + BuildConfig.VERSION_CODE;
        Date buildDate = new Date(Long.parseLong(BuildConfig.BUILD_TIME));
        @SuppressLint("StringFormatMatches") String appinfo=(String.format(getString(R.string.appInfo) , getString(R.string.app_name), localVersion, DateFormat.getDateInstance(DateFormat.MEDIUM).format(buildDate)));
        TextView appinfoText = findViewById(R.id.textView3);
        appinfoText.setText(appinfo);
        findViewById(R.id.logo_id).setOnClickListener(view -> adjustFontSize());
        checkUpdate();
    }

    public void adjustFontScale() {
        Configuration configuration = getResources().getConfiguration();
        if (configuration.fontScale > 0.70) {
            configuration.fontScale = 0.75f;
        } else {
            configuration.fontScale = 1.50f;
        }
            DisplayMetrics metrics = getResources().getDisplayMetrics();
            WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
            wm.getDefaultDisplay().getMetrics(metrics);
            metrics.scaledDensity = configuration.fontScale * metrics.density;
            getBaseContext().getResources().updateConfiguration(configuration, metrics);
    }


    public void adjustFontSize() {
        Configuration configuration = getResources().getConfiguration();
        configuration.fontScale=(float) 0.75; //0.85 small size, 1 normal size, 1,15 big etc

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        metrics.scaledDensity = configuration.fontScale * metrics.density;
        getBaseContext().getResources().updateConfiguration(configuration, metrics);
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
                    Log.d("Version Code", model.getVersionCode() + "");
                    Log.d("Version Description", model.getContentText());
                    Log.d("Min Support", model.getMinSupport() + "");
                    Log.d("Download URL", model.getUrl() + "");
                })
                .build();

        updateWrapper.start();

    }
}

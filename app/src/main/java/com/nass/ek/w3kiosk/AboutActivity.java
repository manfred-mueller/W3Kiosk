package com.nass.ek.w3kiosk;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.nass.ek.appupdate.UpdateWrapper;

import java.text.DateFormat;
import java.util.Date;

public class AboutActivity extends AppCompatActivity {

    public void closeClick(View view) {
        finish();
    }

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
        setContentView(R.layout.activity_about);
        String localVersion = BuildConfig.VERSION_NAME + "." + BuildConfig.VERSION_CODE;
        Date buildDate = new Date(Long.parseLong(BuildConfig.BUILD_TIME));
        @SuppressLint("StringFormatMatches") String appinfo=(String.format(getString(R.string.appInfo) , getString(R.string.app_name), localVersion, DateFormat.getDateInstance(DateFormat.MEDIUM).format(buildDate)));
        TextView appinfoText = findViewById(R.id.textView3);
        appinfoText.setText(appinfo);
        findViewById(R.id.logo_id).setOnClickListener(view -> checkUpdate());
        checkUpdate();
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

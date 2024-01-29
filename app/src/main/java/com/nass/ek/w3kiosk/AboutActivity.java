package com.nass.ek.w3kiosk;

import static com.nass.ek.w3kiosk.ChecksAndConfigs.connectionType;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Html;
import android.text.Spanned;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

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
    public String clientUrl;
    public String passWord;
    public String deviceId;
    public String deviceName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new CountDownTimer(150000, 1000) {
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
        } else {
            Rooted = getString(R.string.No);
        }
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        passWord = sharedPreferences.getString("apiKey", BuildConfig.API_KEY);
        clientUrl = sharedPreferences.getString("clientUrl1", "");
        deviceName = SettingsActivity.readConfigFileContents();
        Date buildDate = new Date(Long.parseLong(BuildConfig.BUILD_TIME));
        @SuppressLint("StringFormatMatches") String appinfo = (String.format(getString(R.string.appInfo), getString(R.string.app_name), localVersion, DateFormat.getDateInstance(DateFormat.MEDIUM).format(buildDate), connectionType(this), Rooted));
        TextView appinfoText = findViewById(R.id.textView3);
        appinfoText.setText(appinfo);
        findViewById(R.id.logo_id).setOnClickListener(view -> checkUpdate());
        deviceId = sharedPreferences.getString("devId", "");
        if (deviceId.isEmpty()) {
            deviceId = deviceName;
        }
        //StatusSender.sendData(deviceId, clientUrl, connectionType(this));
        checkUpdate();
    }

    private void checkUpdate() {
        String updateFound = (String.format(getString(R.string.UpdateAvailable), getString(R.string.app_name)));
        UpdateWrapper updateWrapper = new UpdateWrapper.Builder(AboutActivity.this)
                .setTime(3000)
                .setNotificationIcon(R.mipmap.ic_launcher)
                .setUpdateTitle(updateFound)
                .setUpdateContentText(getString(R.string.UpdateDescription))
                .setUrl(BuildConfig.UPDATE_URL)
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

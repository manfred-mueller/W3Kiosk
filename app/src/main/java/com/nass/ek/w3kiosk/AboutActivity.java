package com.nass.ek.w3kiosk;

import static com.nass.ek.w3kiosk.ChecksAndConfigs.checkApps;
import static com.nass.ek.w3kiosk.ChecksAndConfigs.connectionType;
import static com.nass.ek.w3kiosk.ChecksAndConfigs.getIPAddress;

import android.annotation.SuppressLint;
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

import android.view.KeyEvent;

public class AboutActivity extends AppCompatActivity {

    public void closeClick(View view) {
        finish();
    }

    WindowManager windowManager;
    DisplayMetrics displayMetrics;
    public String KeyCode = "";
    public String Rooted;

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
        String localVersion = BuildConfig.VERSION_NAME + " (" + BuildConfig.VERSION_CODE + ")";
        if (ChecksAndConfigs.isRooted()) {
            Rooted = getString(R.string.Yes);
        } else {
            Rooted = getString(R.string.No);
        }
        Date buildDate = new Date(Long.parseLong(BuildConfig.BUILD_TIME));
        @SuppressLint("StringFormatMatches") String appinfo = (String.format(getString(R.string.appInfo), getString(R.string.app_name), localVersion, DateFormat.getDateInstance(DateFormat.MEDIUM).format(buildDate), String.format(connectionType(this) + " " + getIPAddress(getApplicationContext())), Rooted));
        TextView appinfoText = findViewById(R.id.textView3);
        appinfoText.setText(appinfo);
        TextView keycodeText = findViewById(R.id.textView4);
        keycodeText.setText(KeyCode);
        findViewById(R.id.logo_id).setOnClickListener(view -> checkUpdate());
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
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        TextView keycodeText = findViewById(R.id.textView4);
        keycodeText.setText("Keycode: " + keyCode);
        return super.onKeyDown(keyCode, event);
    }
}

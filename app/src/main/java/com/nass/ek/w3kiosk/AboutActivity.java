package com.nass.ek.w3kiosk;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.Date;

public class AboutActivity extends Activity {
    Handler handler;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        Date buildDate = new Date(Long.parseLong(BuildConfig.BUILD_TIME));
        @SuppressLint("StringFormatMatches") String appinfo=(String.format(getString(R.string.appInfo) , getString(R.string.app_name), DateFormat.getDateInstance(DateFormat.MEDIUM).format(buildDate)));
        TextView appinfoText = findViewById(R.id.textView3);
        appinfoText.setText(appinfo);
    }
    public void closeClick(View view) {
        finish();
    }
}

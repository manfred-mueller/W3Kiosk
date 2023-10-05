package com.nass.ek.w3kiosk;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

import org.json.JSONArray;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MarqueeActivity extends AppCompatActivity {

    private WebView webView;
    private String marqueeText;
    private int marqueeSpeed;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_marquee);

        webView = findViewById(R.id.webView);
        webView.setBackgroundColor(getResources().getColor(R.color.colorPrimary));

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        marqueeText = sharedPreferences.getString("marqueeText", getString(R.string.W3Lager));
        marqueeSpeed = sharedPreferences.getInt("marqueeSpeed", 25);

        if (marqueeText.isEmpty()) {
            File marqueeFile = new File("/storage/emulated/0/Pictures/marquee.png");
            if (marqueeFile.exists()) {
                marqueeText="<img src=\"file:///storage/emulated/0/Pictures/marquee.png\"/>";
            } else {
                marqueeText="<img src=\"file:///android_res/drawable/logo_splash.png\"/>";
            }
        }
        String htmlContent = generateMarqueeHtml(marqueeText, marqueeSpeed);
        loadHtmlContent(htmlContent);

        webView.setWebViewClient(new WebViewClient());
    }

    private String generateMarqueeHtml(String text, int speed) {
        return "<html><head><style>" +
                "marquee {position: absolute; font-size: 20vh; white-space: nowrap; " +
                "color: #f0f0f0; top: 50%; transform: translateY(-50%);}" +
                "</style></head><body>" +
                "<marquee id='marqueeText' behavior=\"scroll\" direction=\"left\" scrollamount=\"" + speed + "\">" + text + "</marquee>" +
                "</body></html>";
    }

    private void loadHtmlContent(String htmlContent) {
        webView.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        finish();
        return true;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        finish();
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        finish();
        return super.dispatchKeyEvent(event);
    }
}

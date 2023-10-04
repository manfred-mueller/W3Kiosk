package com.nass.ek.w3kiosk;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

public class MarqueeActivity extends AppCompatActivity {

    private WebView webView;
    private String marquee;
    private int marqueeSpeed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_marquee);

        webView = findViewById(R.id.webView);
        webView.setBackgroundColor(getResources().getColor(R.color.colorPrimary));

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        marquee = sharedPreferences.getString("marqueeText", getString(R.string.W3Lager));
        marqueeSpeed = sharedPreferences.getInt("marqueeSpeed", 25);

        if (marquee.isEmpty()) {
            marquee = getString(R.string.W3Lager);
        }

        String htmlContent = generateMarqueeHtml(marquee, marqueeSpeed);
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

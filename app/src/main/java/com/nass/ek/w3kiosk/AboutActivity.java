package com.nass.ek.w3kiosk;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;

public class AboutActivity extends Activity {
    Handler handler;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
    }
    public void closeClick(View view) {
        finish();
    }
}

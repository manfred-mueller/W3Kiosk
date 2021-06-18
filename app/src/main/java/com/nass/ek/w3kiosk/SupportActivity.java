package com.nass.ek.w3kiosk;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import static android.provider.Settings.*;
import static android.provider.Settings.Secure;

import android.service.autofill.AutofillService;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.System;

public class SupportActivity extends AppCompatActivity {

    String tvUri = "com.teamviewer.quicksupport.market";
    String adUri = "com.anydesk.anydeskandroid";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_support);
        boolean tvCheck = checkApps(tvUri);
        boolean adCheck = checkApps(adUri);

        if (isAccessibilitySettingsOn()) {
            findViewById(R.id.copyright_text).setOnClickListener(this::sdClick);
        } else {
            Toast.makeText(this, getString(R.string.additional_perms), Toast.LENGTH_LONG).show();
            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
            startActivity(intent);
        }

        if (tvCheck) {
            {
                findViewById(R.id.tv_Button).setVisibility(View.VISIBLE);
            }
        }
        if (adCheck) {
            {
                findViewById(R.id.ad_Button).setVisibility(View.VISIBLE);
            }
        }
    }

    public void tvClick(View view) {
        appClick(tvUri);
    }
    public void adClick(View view) {
        appClick(adUri);
    }
    public void sdClick(View view) {
        startService(new Intent(this, ShutdownService.class));
    }

    public void closeClick(View view) {
        finish();
    }
    public void autofillClick(View view)
    {
        try {
            Process process = Runtime.getRuntime().exec("settings put secure autofill_service null");
            BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void appClick(String uri) {

        Intent t;
        PackageManager manager = getPackageManager();
        try {
            t = manager.getLaunchIntentForPackage(uri);
            if (t == null)
                throw new PackageManager.NameNotFoundException();
            t.addCategory(Intent.CATEGORY_LAUNCHER);
            startActivity(t);
        } catch (PackageManager.NameNotFoundException ignored) {
        }
    }

    private boolean checkApps(String uri) {
        PackageInfo pkgInfo;
        try {
            pkgInfo = getPackageManager().getPackageInfo(uri, 0);
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
        return pkgInfo != null;
    }

    public void hideKeyboard(View v) {
        InputMethodManager imm = (InputMethodManager) this.getSystemService(Activity.INPUT_METHOD_SERVICE);

        View view = this.getCurrentFocus();
        if (view == null) {
            view = new View(this);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public boolean isAccessibilitySettingsOn() {
        int accessibilityEnabled = 0;
        ContentResolver cr = getApplicationContext().getContentResolver();
        try {
            accessibilityEnabled = Settings.Secure.getInt(cr, Settings.Secure.ACCESSIBILITY_ENABLED);
        } catch (Settings.SettingNotFoundException ignored) {
        }
        if (accessibilityEnabled == 1) {
            String settingValue = Settings.Secure.getString(cr, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            return settingValue != null && settingValue.contains("com.nass.ek.w3kiosk/com.nass.ek.w3kiosk.ShutdownService");
        }
        return false;
    }
}
package com.nass.ek.w3kiosk;

import static android.os.Build.VERSION_CODES.O;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.view.accessibility.AccessibilityManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

public class SettingsActivity extends AppCompatActivity {

    Context context = this;
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    Switch s;
    EditText e;
    CheckBox c;
    ImageButton b;
    RadioButton r;

    String[] allowedApps = new String[]{"0", "1", "2", "3", "4"};
    String[] marqueeTimeout = new String[]{"1", "5", "10", "15", "20", "25", "30"};
    String[] urlTimeout = new String[]{"---", "30", "60", "90", "120", "150", "180"};
    String[] zoomFactor = new String[]{"75%", "80%", "85%", "90%", "95%", "100%", "105%", "110%", "115%", "120%", "125%"};
    private Spinner appsDropdown;
    private Spinner marqueeDropdown;
    private Spinner timeoutDropdown;
    private Spinner zoomDropdown;


    @SuppressLint({"ApplySharedPref", "StringFormatMatches"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        setContentView(R.layout.activity_settings);
        toggleLogin(findViewById(R.id.autologinLayout));
        toggleMarquee(findViewById(R.id.marqueeLayout));
        TextView client1Text = findViewById(R.id.client1Text);
        TextView client2Text = findViewById(R.id.client2Text);
        TextView client3Text = findViewById(R.id.client3Text);
        client1Text.setText(String.format(getString(R.string.website1), getString(R.string.url_preset)));
        client2Text.setText(getString(R.string.website2));
        client3Text.setText(getString(R.string.website3));
        appsDropdown = findViewById(R.id.appsSpinner);
        ArrayAdapter<String> appAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, allowedApps);
        appAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        appsDropdown.setAdapter(appAdapter);
        appsDropdown.setOnLongClickListener(v -> {
            startActivity(new Intent(this, AppsActivity.class));
            return true;
        });
        marqueeDropdown = findViewById(R.id.marqueeSpinner);
        ArrayAdapter<String> mqtimeoutAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, marqueeTimeout);
        mqtimeoutAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        marqueeDropdown.setAdapter(mqtimeoutAdapter);
        timeoutDropdown = findViewById(R.id.timeoutSpinner);
        ArrayAdapter<String> timeoutAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, urlTimeout);
        timeoutAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        timeoutDropdown.setAdapter(timeoutAdapter);
        zoomDropdown = findViewById(R.id.zoomSpinner);
        ArrayAdapter<String> zoomAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, zoomFactor);
        zoomAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        zoomDropdown.setAdapter(zoomAdapter);
        findViewById(R.id.setLauncherButton).setVisibility(View.VISIBLE);
        if (ChecksAndConfigs.isTablet()){
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
                findViewById(R.id.marqueeLayout).setVisibility(View.VISIBLE);
                findViewById(R.id.autologinLayout).setVisibility(View.VISIBLE);
            }
        }

        if (ChecksAndConfigs.isScanner()){
            if (checkApp()){
                findViewById(R.id.scannerButton).setVisibility(View.VISIBLE);
            }
            findViewById(R.id.mobileToggle).setVisibility(View.VISIBLE);
            findViewById(R.id.mobileText).setVisibility(View.VISIBLE);
            findViewById(R.id.timeoutText).setVisibility(View.GONE);
            findViewById(R.id.timeoutSpinner).setVisibility(View.GONE);
            findViewById(R.id.zoomText).setVisibility(View.GONE);
            findViewById(R.id.zoomSpinner).setVisibility(View.GONE);
            findViewById(R.id.client3Text).setVisibility(View.GONE);
            findViewById(R.id.client3EditText).setVisibility(View.GONE);
        }

        b = findViewById(R.id.updateCloseButton);
        b.setOnClickListener(view -> {
            SharedPreferences.Editor editor = sharedPreferences.edit();

            s = findViewById(R.id.mobileToggle);
            editor.putBoolean("mobileMode", s.isChecked());

            s = findViewById(R.id.updateToggle);
            editor.putBoolean("autoUpdate", s.isChecked());

            r = findViewById(R.id.radioSlow);
            if (r.isChecked())
            editor.putInt("marqueeSpeed", 10);

            r = findViewById(R.id.radioNormal);
            if (r.isChecked())
                editor.putInt("marqueeSpeed", 25);

            r = findViewById(R.id.radioFast);
            if (r.isChecked())
                editor.putInt("marqueeSpeed", 40);

            editor.putInt("appsCount", Integer.parseInt(appsDropdown.getSelectedItem().toString()));

            editor.putInt("urlTimeout", timeoutDropdown.getSelectedItemPosition());

            editor.putInt("marqueeTimeout", marqueeDropdown.getSelectedItemPosition());

            editor.putInt("zoomFactor", zoomDropdown.getSelectedItemPosition());

            s = findViewById(R.id.marquee);
            editor.putBoolean("marquee", s.isChecked());

            s = findViewById(R.id.autoLogin);
            editor.putBoolean("autoLogin", s.isChecked());

            e = findViewById(R.id.marqueeEditText);
            editor.putString("marqueeText", e.getText().toString());

            e = findViewById(R.id.loginEditText);
            editor.putString("loginName", e.getText().toString());

            e = findViewById(R.id.pwEditText);
            editor.putString("loginPassword", e.getText().toString());

            e = findViewById(R.id.client1EditText);
            editor.putString("clientUrl1", e.getText().toString());

            e = findViewById(R.id.client2EditText);
            editor.putString("clientUrl2", e.getText().toString());

            e = findViewById(R.id.client3EditText);
            editor.putString("clientUrl3", e.getText().toString());

            editor.commit();
            finish();
        });

        s = findViewById(R.id.mobileToggle);
        s.setChecked(sharedPreferences.getBoolean("mobileMode", false));

        s = findViewById(R.id.updateToggle);
        s.setChecked(sharedPreferences.getBoolean("autoUpdate", false));

        int i = sharedPreferences.getInt("marqueeSpeed", 25);
        if (i < 25) {
            r = findViewById(R.id.radioSlow);
        }
        else if (i == 25) {
            r = findViewById(R.id.radioNormal);
        }
        else {
            r = findViewById(R.id.radioFast);
        }
        r.setChecked(true);

        appsDropdown.setSelection(sharedPreferences.getInt("appsCount", 0));
        int urlSpinnerValue = sharedPreferences.getInt("urlTimeout",-1);
        if(urlSpinnerValue != -1)
            timeoutDropdown.setSelection(urlSpinnerValue);
        int marqueeSpinnerValue = sharedPreferences.getInt("marqueeTimeout",-1);
        if(marqueeSpinnerValue != -1)
            marqueeDropdown.setSelection(marqueeSpinnerValue);
        int zoomValue = sharedPreferences.getInt("zoomFactor",5);
        zoomDropdown.setSelection(zoomValue);

        s = findViewById(R.id.marquee);
        s.setChecked(sharedPreferences.getBoolean("marquee", false));
        s.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                findViewById(R.id.marqueeEditText).setVisibility(View.VISIBLE);
                findViewById(R.id.marqueeTimeoutText).setVisibility(View.VISIBLE);
                findViewById(R.id.marqueeSpinner).setVisibility(View.VISIBLE);
                findViewById(R.id.marqueeSpeedText).setVisibility(View.VISIBLE);
                findViewById(R.id.marqueeSpeedGroup).setVisibility(View.VISIBLE);
            } else {
                findViewById(R.id.marqueeEditText).setVisibility(View.GONE);
                findViewById(R.id.marqueeTimeoutText).setVisibility(View.GONE);
                findViewById(R.id.marqueeSpinner).setVisibility(View.GONE);
                findViewById(R.id.marqueeSpeedText).setVisibility(View.GONE);
                findViewById(R.id.marqueeSpeedGroup).setVisibility(View.GONE);
            }
        });

        s = findViewById(R.id.autoLogin);
        s.setChecked(sharedPreferences.getBoolean("autoLogin", false));
        s.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                findViewById(R.id.loginEditText).setVisibility(View.VISIBLE);
                findViewById(R.id.loginName).setVisibility(View.VISIBLE);
                findViewById(R.id.pwEditText).setVisibility(View.VISIBLE);
                findViewById(R.id.loginPw).setVisibility(View.VISIBLE);
            } else {
                findViewById(R.id.loginEditText).setVisibility(View.GONE);
                findViewById(R.id.loginName).setVisibility(View.GONE);
                findViewById(R.id.pwEditText).setVisibility(View.GONE);
                findViewById(R.id.loginPw).setVisibility(View.GONE);
            }
        });
        e = findViewById(R.id.client1EditText);
        e.setText(sharedPreferences.getString("clientUrl1", ""));

        e = findViewById(R.id.client2EditText);
        e.setText(sharedPreferences.getString("clientUrl2", ""));

        e = findViewById(R.id.client3EditText);
        e.setText(sharedPreferences.getString("clientUrl3", ""));

        e = findViewById(R.id.loginEditText);
        e.setText(sharedPreferences.getString("loginName", ""));

        e = findViewById(R.id.marqueeEditText);
        e.setText(sharedPreferences.getString("marqueeText", ""));

        e = findViewById(R.id.pwEditText);
        e.setText(sharedPreferences.getString("loginPassword", ""));

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            b = findViewById(R.id.displayButton);
            if (!ChecksAndConfigs.isScanner()) {
                b.setVisibility(View.VISIBLE);
            }

            b = findViewById(R.id.keyboardButton);
            if (ChecksAndConfigs.isTv(this)) {
                b.setVisibility(View.VISIBLE);
            }

            c = findViewById(R.id.writeStorage);
            c.setChecked(context.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
            c.setEnabled(context.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED);

            c = findViewById(R.id.camAccess);
            if (ChecksAndConfigs.isTv(this)) {
                c.setVisibility(View.GONE);
            } else {
                c.setChecked(context.checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED);
                c.setEnabled(context.checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED);
            }

            c = findViewById(R.id.overlayPerm);
            if (ChecksAndConfigs.isTv(this)) {
                c.setVisibility(View.GONE);
            } else {
                c.setChecked(Settings.canDrawOverlays(this));
                c.setEnabled(!Settings.canDrawOverlays(this));
            }

            c = findViewById(R.id.powerMenu);
            c.setChecked(isAccessibilitySettingsOn());
            c.setEnabled(!isAccessibilitySettingsOn());

            c = findViewById(R.id.writeSystem);
            if (ChecksAndConfigs.isTv(this)) {
                c.setVisibility(View.GONE);
            } else {
                c.setChecked(Settings.System.canWrite(this));
                c.setEnabled(!Settings.System.canWrite(this));
            }

            c = findViewById(R.id.installApps);
            PackageManager packageManager = context.getPackageManager();
            if (Build.VERSION.SDK_INT >= O) {
                c.setChecked(packageManager.canRequestPackageInstalls());
                c.setEnabled(!packageManager.canRequestPackageInstalls());
            } else {
                c.setVisibility(View.GONE);
            }
        }
    }


    public void toggleLogin(View v) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        boolean checkAutoLogin = sharedPreferences.getBoolean("autoLogin", false);
        if (checkAutoLogin) {
            findViewById(R.id.loginEditText).setVisibility(View.VISIBLE);
            findViewById(R.id.loginName).setVisibility(View.VISIBLE);
            findViewById(R.id.pwEditText).setVisibility(View.VISIBLE);
            findViewById(R.id.loginPw).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.loginEditText).setVisibility(View.GONE);
            findViewById(R.id.loginName).setVisibility(View.GONE);
            findViewById(R.id.pwEditText).setVisibility(View.GONE);
            findViewById(R.id.loginPw).setVisibility(View.GONE);
        }
    }

    public void toggleMarquee(View v) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        boolean checkMarquee = sharedPreferences.getBoolean("marquee", false);
        if (checkMarquee) {
                findViewById(R.id.marqueeEditText).setVisibility(View.VISIBLE);
                findViewById(R.id.marqueeTimeoutText).setVisibility(View.VISIBLE);
                findViewById(R.id.marqueeSpinner).setVisibility(View.VISIBLE);
                findViewById(R.id.marqueeSpeedText).setVisibility(View.VISIBLE);
                findViewById(R.id.marqueeSpeedGroup).setVisibility(View.VISIBLE);
            } else {
                findViewById(R.id.marqueeEditText).setVisibility(View.GONE);
                findViewById(R.id.marqueeTimeoutText).setVisibility(View.GONE);
                findViewById(R.id.marqueeSpinner).setVisibility(View.GONE);
                findViewById(R.id.marqueeSpeedText).setVisibility(View.GONE);
                findViewById(R.id.marqueeSpeedGroup).setVisibility(View.GONE);
            }
    }

    private boolean isMyAppLauncherDefault() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        ComponentName defaultLauncher = intent.resolveActivity(getPackageManager());
        ComponentName myAppLauncher = new ComponentName(this, MainActivity.class);
        return defaultLauncher != null && defaultLauncher.equals(myAppLauncher);
    }

    private void showSetDefaultLauncherDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.setAsStartApp);
        builder.setMessage(R.string.setAsStartAppText);
        builder.setPositiveButton(R.string.Yes, (dialog, which) -> {
            Intent intent = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                intent = new Intent(Settings.ACTION_HOME_SETTINGS);
            }
            assert intent != null;
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
        builder.setNegativeButton(R.string.No, (dialog, which) -> {
            // Handle user's choice not to set as default launcher
            showRationaleForNo();
        });
        builder.show();
    }

    private void showRationaleForNo() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.NoStartApp);
        builder.setMessage(R.string.NoStartAppRationale);
        builder.setPositiveButton(R.string.imSure, (dialog, which) -> {
        });
        builder.setNegativeButton(R.string.setAsStartApp, (dialog, which) -> {
            // Handle user's decision to set as default launcher
            Intent intent = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                intent = new Intent(Settings.ACTION_HOME_SETTINGS);
            }
            assert intent != null;
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
        builder.show();
    }

    public void setLauncher(View v) {
        if (isMyAppLauncherDefault()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.changeStartApp);
            builder.setMessage(R.string.alreadyStartApp);
            builder.setPositiveButton("Ok", (dialog, which) -> {
            });
            builder.show();
        } else {
            showSetDefaultLauncherDialog();
        }
    }

    public void startSystemSettings(View v){
        startActivity(new Intent(Settings.ACTION_SETTINGS));
    }

    public void openDisplaySettings(View v) {
        Intent intent = new Intent(Settings.ACTION_DISPLAY_SETTINGS);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        startActivity(intent);
    }

    public void openKeyboardSettings(View v) {
        Intent intent = new Intent(Settings.ACTION_INPUT_METHOD_SETTINGS);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        startActivity(intent);
    }

    public void sdClick(View v) {
        startService(new Intent(this, ShutdownService.class));
    }

    public void hideKeyboard(View v) {
        InputMethodManager imm = (InputMethodManager) this.getSystemService(Activity.INPUT_METHOD_SERVICE);

        View view = this.getCurrentFocus();
        if (view == null) {
            view = new View(this);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public void checkAccessibilityPermission(View v){
            if (!isAccessibilitySettingsOn()) {
            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
            startActivity(intent);
        }
    }

    public void checkOverlayPermission(View v){
        if (android.os.Build.VERSION.SDK_INT >= 23 && !Settings.canDrawOverlays(SettingsActivity.this)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
            intent.setData(Uri.parse("package:" + getPackageName()));
            context.startActivity(intent);
        }
    }

    public void checkStoragePermission(View v){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_DENIED) {

                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 4710);
            }
        }
    }

    public void checkWritePermission(View v){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (! Settings.System.canWrite(this))
            {
                Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                intent.setData(Uri.parse("package:" + this.getPackageName()));
                startActivity(intent);
            }
        }
    }

    public void checkInstallPermission(View v) {
        Intent intent;
        PackageManager packageManager = context.getPackageManager();
        if (android.os.Build.VERSION.SDK_INT >= O && !packageManager.canRequestPackageInstalls()) {
            intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES);
            intent.setData(Uri.parse("package:" + getPackageName()));
            startActivity(intent);
        }
    }

    public void checkCameraPermission(View v){
        if (ContextCompat.checkSelfPermission(SettingsActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(SettingsActivity.this,new String[]{
                    Manifest.permission.CAMERA}, 4711);
        }
    }

    public void configureScanner(View v){
        Intent keyboardEmulator;
        PackageManager manager = getPackageManager();
        try {
            keyboardEmulator = manager.getLaunchIntentForPackage("com.rscja.scanner");
            if (keyboardEmulator == null)
                throw new PackageManager.NameNotFoundException();
            keyboardEmulator.addCategory(Intent.CATEGORY_LAUNCHER);
            startActivity(keyboardEmulator);
        } catch (PackageManager.NameNotFoundException ignored) {
        }
    }

    public boolean isAccessibilitySettingsOn() {
        AccessibilityManager am = (AccessibilityManager) getSystemService(ACCESSIBILITY_SERVICE);
        ContentResolver cr = getApplicationContext().getContentResolver();
        if (am.isEnabled()) {
            String settingValue = Settings.Secure.getString(cr, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            return settingValue != null && settingValue.contains("com.nass.ek.w3kiosk/com.nass.ek.w3kiosk.ShutdownService");
        }
        return false;
    }

    private boolean checkApp() {
        PackageInfo pkgInfo;
        try {
            pkgInfo = getPackageManager().getPackageInfo("com.rscja.scanner", 0);
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
        return pkgInfo != null;
    }

    @RequiresApi(api = O)
    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            int zoomValue = sharedPreferences.getInt("zoomFactor",5);
            zoomDropdown.setSelection(zoomValue);

            c = findViewById(R.id.writeStorage);
            c.setChecked(context.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
            c.setEnabled(context.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED);

            c = findViewById(R.id.camAccess);
            if (ChecksAndConfigs.isTv(this)) {
                c.setVisibility(View.GONE);
            } else {
                c.setChecked(context.checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED);
                c.setEnabled(context.checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED);
            }

            c = findViewById(R.id.overlayPerm);
            if (ChecksAndConfigs.isTv(this)) {
                c.setVisibility(View.GONE);
            } else {
                c.setChecked(Settings.canDrawOverlays(this));
                c.setEnabled(!Settings.canDrawOverlays(this));
            }

            c = findViewById(R.id.powerMenu);
            c.setChecked(isAccessibilitySettingsOn());
            c.setEnabled(!isAccessibilitySettingsOn());

            c = findViewById(R.id.writeSystem);
            if (ChecksAndConfigs.isTv(this)) {
                c.setVisibility(View.GONE);
            } else {
                c.setChecked(Settings.System.canWrite(this));
                c.setEnabled(!Settings.System.canWrite(this));
            }

            c = findViewById(R.id.installApps);
            if (android.os.Build.VERSION.SDK_INT >= O) {
                PackageManager packageManager = context.getPackageManager();
                c.setChecked(packageManager.canRequestPackageInstalls());
                c.setEnabled(!packageManager.canRequestPackageInstalls());
            } else {
                c.setVisibility(View.GONE);
            }
        }
    }
}
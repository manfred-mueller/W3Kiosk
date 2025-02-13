package com.nass.ek.w3kiosk;

import static android.os.Build.VERSION_CODES.O;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.accessibility.AccessibilityManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class SettingsActivity extends AppCompatActivity {

    Context context = this;
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    Switch s;
    EditText e;
    CheckBox c;
    ImageButton b;
    RadioButton r;

    String devId;
    String[] allowedApps = new String[]{"0", "1", "2", "3", "4"};
    String[] marqueeTimeout = new String[]{"5", "10", "15", "20", "25", "30"};
    String[] urlTimeout = new String[]{"---", "30", "60", "90", "120", "150", "180"};
    String[] zoomFactor = new String[]{"75%", "80%", "85%", "90%", "95%", "100%", "105%", "110%", "115%", "120%", "125%"};
    private Spinner appsDropdown;
    private Spinner marqueeDropdown;
    private Spinner timeoutDropdown;
    private Spinner zoomDropdown;
    public EditText marqueeEditText;
    private GestureDetector gestureDetector;

    public String chwUri = "com.rscja.scanner";
    public String zebUri = "com.zebra.scanner";
    public boolean chwCheck = false;
    public boolean zebCheck = false;
    public Intent keyboardEmulator;
    public static File configDirectory;


    @SuppressLint({"ApplySharedPref", "StringFormatMatches", "ClickableViewAccessibility"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        zebCheck = ChecksAndConfigs.checkApps(this, zebUri);
        chwCheck = ChecksAndConfigs.checkApps(this, chwUri);
        setContentView(R.layout.activity_settings);
        toggleLogin(findViewById(R.id.autologinLayout));
        toggleMarquee(findViewById(R.id.marqueeLayout));
        marqueeEditText = findViewById(R.id.marqueeEditText);
        TextView client1Text = findViewById(R.id.client1Text);
        TextView client2Text = findViewById(R.id.client2Text);
        TextView client3Text = findViewById(R.id.client3Text);
        client1Text.setText(String.format(getString(R.string.website1), getString(R.string.url_preset)));
        client2Text.setText(getString(R.string.website2));
        client3Text.setText(getString(R.string.website3));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            findViewById(R.id.permissionLayout).setVisibility(View.VISIBLE);
            findViewById(R.id.setLauncherButton).setVisibility(View.VISIBLE);
            findViewById(R.id.appsText).setVisibility(View.VISIBLE);
            appsDropdown = findViewById(R.id.appsSpinner);
            appsDropdown.setVisibility(View.VISIBLE);
            ArrayAdapter<String> appAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, allowedApps);
            appAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            appsDropdown.setAdapter(appAdapter);
            appsDropdown.setSelection(sharedPreferences.getInt("appsCount", 0));
            appsDropdown.setOnLongClickListener(v -> {
                startActivity(new Intent(this, AppsActivity.class));
                return true;
            });
        }
        marqueeDropdown = findViewById(R.id.marqueeSpinner);
        ArrayAdapter<String> mqtimeoutAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, marqueeTimeout);
        mqtimeoutAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        marqueeDropdown.setAdapter(mqtimeoutAdapter);
        gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDoubleTap(MotionEvent e) {
                toggleMarqueeEditText();
                return true;
            }
        });

        findViewById(R.id.marqueeText).setOnTouchListener((view, event) -> {
            gestureDetector.onTouchEvent(event);
            return true;
        });
        timeoutDropdown = findViewById(R.id.timeoutSpinner);
        ArrayAdapter<String> timeoutAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, urlTimeout);
        timeoutAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        timeoutDropdown.setAdapter(timeoutAdapter);
        zoomDropdown = findViewById(R.id.zoomSpinner);
        ArrayAdapter<String> zoomAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, zoomFactor);
        zoomAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        zoomDropdown.setAdapter(zoomAdapter);
        if (ChecksAndConfigs.isTablet()) {
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
                findViewById(R.id.marqueeLayout).setVisibility(View.VISIBLE);
                findViewById(R.id.autologinLayout).setVisibility(View.VISIBLE);
            }
        }

        if (ChecksAndConfigs.isScanner()) {
            if (chwCheck || zebCheck) {
                findViewById(R.id.scannerButton).setVisibility(View.VISIBLE);
                findViewById(R.id.chromeText).setVisibility(View.VISIBLE);
                findViewById(R.id.chromeToggle).setVisibility(View.VISIBLE);
            }
            findViewById(R.id.timeoutText).setVisibility(View.GONE);
            findViewById(R.id.timeoutSpinner).setVisibility(View.GONE);
            findViewById(R.id.zoomText).setVisibility(View.GONE);
            findViewById(R.id.zoomSpinner).setVisibility(View.GONE);
            findViewById(R.id.client3Text).setVisibility(View.GONE);
            findViewById(R.id.client3EditText).setVisibility(View.GONE);
        }

        devId = sharedPreferences.getString("devId", ChecksAndConfigs.randomId());

        b = findViewById(R.id.updateCloseButton);
        b.setOnClickListener(view -> {
            SharedPreferences.Editor editor = sharedPreferences.edit();

            s = findViewById(R.id.chromeToggle);
            editor.putBoolean("useChrome", s.isChecked());

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

            e = findViewById(R.id.devIdEditText);
            editor.putString("devId", e.getText().toString());

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

        s = findViewById(R.id.chromeToggle);
        s.setChecked(sharedPreferences.getBoolean("useChrome", false));

        s = findViewById(R.id.updateToggle);
        s.setChecked(sharedPreferences.getBoolean("autoUpdate", false));

        int i = sharedPreferences.getInt("marqueeSpeed", 25);
        if (i < 25) {
            r = findViewById(R.id.radioSlow);
        } else if (i == 25) {
            r = findViewById(R.id.radioNormal);
        } else {
            r = findViewById(R.id.radioFast);
        }
        r.setChecked(true);

        int urlSpinnerValue = sharedPreferences.getInt("urlTimeout", -1);
        if (urlSpinnerValue != -1)
            timeoutDropdown.setSelection(urlSpinnerValue);
        int marqueeSpinnerValue = sharedPreferences.getInt("marqueeTimeout", -1);
        if (marqueeSpinnerValue != -1)
            marqueeDropdown.setSelection(marqueeSpinnerValue);
        int zoomValue = sharedPreferences.getInt("zoomFactor", 5);
        zoomDropdown.setSelection(zoomValue);

        s = findViewById(R.id.marquee);
        if (ChecksAndConfigs.isTablet()) {
            s.setChecked(sharedPreferences.getBoolean("marquee", true));
            findViewById(R.id.marqueeEditText).setVisibility(View.VISIBLE);
            findViewById(R.id.marqueeTimeoutText).setVisibility(View.VISIBLE);
            findViewById(R.id.marqueeSpinner).setVisibility(View.VISIBLE);
            findViewById(R.id.marqueeSpeedText).setVisibility(View.VISIBLE);
            findViewById(R.id.marqueeSpeedGroup).setVisibility(View.VISIBLE);
        } else {
            s.setChecked(sharedPreferences.getBoolean("marquee", false));
        }
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

        e = findViewById(R.id.devIdEditText);
        e.setText(sharedPreferences.getString("devId", ChecksAndConfigs.randomId()));

        e = findViewById(R.id.marqueeEditText);
        e.setText(sharedPreferences.getString("marqueeText", getString(R.string.W3Lager)));

        e = findViewById(R.id.pwEditText);
        e.setText(sharedPreferences.getString("loginPassword", ""));

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            b = findViewById(R.id.displayButton);
            if (!ChecksAndConfigs.isScanner()) {
                b.setVisibility(View.VISIBLE);
            }

            b = findViewById(R.id.keyboardButton);
            if (ChecksAndConfigs.isTv()) {
                b.setVisibility(View.VISIBLE);
            }

            c = findViewById(R.id.writeStorage);
            if (ChecksAndConfigs.isTablet()) {
                c.setVisibility(View.GONE);
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    if (Environment.isExternalStorageManager()) {
                        c.setChecked(true);
                        c.setEnabled(false);
                    } else {
                        c.setChecked(false);
                        c.setEnabled(true);
                    }
                } else {
                    c.setChecked(context.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
                    c.setEnabled(context.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED);
                }
            }
            String configFileContent = readConfigFileContents();

            if (configFileContent.isEmpty()) {
                devId = sharedPreferences.getString("devId", findViewById(R.id.devIdEditText).toString());
                writeConfigFileContents(devId);
            }

            c = findViewById(R.id.camAccess);
            if (ChecksAndConfigs.isTv() || ChecksAndConfigs.isTablet()) {
                c.setVisibility(View.GONE);
            } else {
                c.setChecked(context.checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED);
                c.setEnabled(context.checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED);
            }

            c = findViewById(R.id.overlayPerm);
            if (ChecksAndConfigs.isTv()) {
                c.setVisibility(View.GONE);
            } else {
                c.setChecked(Settings.canDrawOverlays(this));
                c.setEnabled(!Settings.canDrawOverlays(this));
            }

            c = findViewById(R.id.powerMenu);
            if (ChecksAndConfigs.isTablet()) {
                c.setVisibility(View.GONE);
            } else {
                c.setChecked(isAccessibilitySettingsOn());
                c.setEnabled(!isAccessibilitySettingsOn());
            }

            c = findViewById(R.id.writeSystem);
            if (ChecksAndConfigs.isTv()) {
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

    public void toggleMarqueeEditText() {
        marqueeEditText.setEnabled(!marqueeEditText.isEnabled());
    }

    public void toggleMarquee(View v) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        boolean checkMarquee = sharedPreferences.getBoolean("marquee", false);
        findViewById(R.id.marqueeEditText).setVisibility(checkMarquee ? View.VISIBLE : View.GONE);
        findViewById(R.id.marqueeTimeoutText).setVisibility(checkMarquee ? View.VISIBLE : View.GONE);
        findViewById(R.id.marqueeSpinner).setVisibility(checkMarquee ? View.VISIBLE : View.GONE);
        findViewById(R.id.marqueeSpeedText).setVisibility(checkMarquee ? View.VISIBLE : View.GONE);
        findViewById(R.id.marqueeSpeedGroup).setVisibility(checkMarquee ? View.VISIBLE : View.GONE);
    }

    public void toggleLogin(View v) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        boolean checkAutoLogin = sharedPreferences.getBoolean("autoLogin", false);
        findViewById(R.id.loginEditText).setVisibility(checkAutoLogin ? View.VISIBLE : View.GONE);
        findViewById(R.id.loginName).setVisibility(checkAutoLogin ? View.VISIBLE : View.GONE);
        findViewById(R.id.pwEditText).setVisibility(checkAutoLogin ? View.VISIBLE : View.GONE);
        findViewById(R.id.loginPw).setVisibility(checkAutoLogin ? View.VISIBLE : View.GONE);
    }
/*
    private boolean isMyAppLauncherDefault() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        ComponentName defaultLauncher = intent.resolveActivity(getPackageManager());
        ComponentName myAppLauncher = new ComponentName(this, MainActivity.class);
        return defaultLauncher != null && defaultLauncher.equals(myAppLauncher);
    }
*/
    private void showSetDefaultLauncherDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.setAsStartApp);
        builder.setMessage(R.string.setAsStartAppText);
        builder.setPositiveButton(R.string.Yes, (dialog, which) -> {
            Intent intent;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                intent = new Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS);
            } else {
                intent = new Intent(Settings.ACTION_HOME_SETTINGS);
            }
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
            // User confirmed they don't want to set as default launcher
        });
        builder.setNegativeButton(R.string.setAsStartApp, (dialog, which) -> {
            // Handle user's decision to set as default launcher
            Intent intent;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                intent = new Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS);
            } else {
                intent = new Intent(Settings.ACTION_HOME_SETTINGS);
            }
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
                // User acknowledged the message
            });
            builder.show();
        } else {
            showSetDefaultLauncherDialog();
        }
    }

    private boolean isMyAppLauncherDefault() {
        final Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        final ResolveInfo resolveInfo = getPackageManager().resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
        if (resolveInfo != null) {
            String currentHomePackage = resolveInfo.activityInfo.packageName;
            return currentHomePackage.equals(getPackageName());
        }
        return false;
    }


    public void overwriteConfigFile(View v) {
        e = findViewById(R.id.devIdEditText);
        writeConfigFileContents(e.getText().toString());
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
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (! Environment.isExternalStorageManager()) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                Uri uri = Uri.fromParts("package", getPackageName(), null);
                intent.setData(uri);
                context.startActivity(intent);
            }
        } else {
            ActivityCompat.requestPermissions(SettingsActivity.this,new String[]{
                    Manifest.permission.WRITE_EXTERNAL_STORAGE}, 4710);
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
        PackageManager manager = getPackageManager();
        if (chwCheck) {
            keyboardEmulator = manager.getLaunchIntentForPackage(chwUri);
            keyboardEmulator.addCategory(Intent.CATEGORY_LAUNCHER);
            startActivity(keyboardEmulator);
        } else if (zebCheck) {
            keyboardEmulator = manager.getLaunchIntentForPackage(zebUri);
            keyboardEmulator.addCategory(Intent.CATEGORY_LAUNCHER);
            startActivity(keyboardEmulator);
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
    @RequiresApi(api = O)
    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            int zoomValue = sharedPreferences.getInt("zoomFactor",5);
            zoomDropdown.setSelection(zoomValue);

                c = findViewById(R.id.writeStorage);
            if (!ChecksAndConfigs.isTablet()) {
                c.setVisibility(View.GONE);
            } else {
                c.setChecked(context.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
                c.setEnabled(context.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED);
            }

            c = findViewById(R.id.camAccess);
            if (ChecksAndConfigs.isTv()) {
                c.setVisibility(View.GONE);
            } else {
                c.setChecked(context.checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED);
                c.setEnabled(context.checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED);
            }

            c = findViewById(R.id.overlayPerm);
            if (ChecksAndConfigs.isTv()) {
                c.setVisibility(View.GONE);
            } else {
                c.setChecked(Settings.canDrawOverlays(this));
                c.setEnabled(!Settings.canDrawOverlays(this));
            }

            c = findViewById(R.id.powerMenu);
            if (!ChecksAndConfigs.isTablet()) {
                c.setVisibility(View.GONE);
            } else {
                c.setChecked(isAccessibilitySettingsOn());
                c.setEnabled(!isAccessibilitySettingsOn());
            }

            c = findViewById(R.id.writeSystem);
            if (ChecksAndConfigs.isTv()) {
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
    static String readConfigFileContents() {
        if (android.os.Build.VERSION.SDK_INT >= O) {
            configDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        } else
        {
            File dir = new File(Environment.getExternalStorageDirectory() + "/Download/");
            dir.mkdirs();
        }

        if (configDirectory != null) {
            File configFile = new File(configDirectory, "w3coach.cfg");

            if (configFile.exists()) {
                try {
                    FileInputStream inputStream = new FileInputStream(configFile);
                    int size = inputStream.available();
                    byte[] buffer = new byte[size];
                    inputStream.read(buffer);
                    inputStream.close();
                    return new String(buffer);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return "";
    }

    private void writeConfigFileContents(String content) {
        configDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);

        if (configDirectory != null) {
            File configFile = new File(configDirectory, "w3coach.cfg");

            try {
                if (!configFile.exists()) {
                    if (!configFile.createNewFile()) {
                        return; // Exit early if file creation fails
                    }
                }

                FileOutputStream outputStream = null;
                try {
                    outputStream = new FileOutputStream(configFile);
                    outputStream.write(content.getBytes());
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (outputStream != null) {
                        try {
                            outputStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
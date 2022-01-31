package com.nass.ek.w3kiosk;

import android.Manifest;
import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.UiModeManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.View;
import android.view.accessibility.AccessibilityManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

import java.util.List;
import java.util.regex.Pattern;

@RequiresApi(api = Build.VERSION_CODES.M)
public class SettingsActivity extends AppCompatActivity {

    Context context = this;
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    Switch s;
    EditText e;
    CheckBox c;
    @SuppressLint("ApplySharedPref")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        TextView clientText = findViewById(R.id.clientText);
        clientText.setText(String.format(getString(R.string.website), getString(R.string.url_preset)));
        if (!isTv()){
            findViewById(R.id.permissionLayout).setVisibility(View.VISIBLE);
            if (checkApp("com.rscja.scanner")){
                findViewById(R.id.scannerButton).setVisibility(View.VISIBLE);
            }
        }
        ImageButton b = findViewById(R.id.settingsSaveButton);
        b.setOnClickListener(view -> {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            SharedPreferences.Editor editor = sharedPreferences.edit();

            s = findViewById(R.id.autofillToggle);
            editor.putBoolean("checkAutofill", s.isChecked());

            s = findViewById(R.id.mobileToggle);
            editor.putBoolean("mobileMode", s.isChecked());

            e = findViewById(R.id.clientEditText);
                editor.putString("clientUrl", e.getText().toString());

            editor.commit();
            finish();
        });

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        s = findViewById(R.id.autofillToggle);
        s.setChecked(sharedPreferences.getBoolean("checkAutofill", true));

        s = findViewById(R.id.mobileToggle);
        s.setChecked(sharedPreferences.getBoolean("mobileMode", false));

        e = findViewById(R.id.clientEditText);
        e.setText(sharedPreferences.getString("clientUrl", ""));

        c = findViewById(R.id.phoneAccess);
        c.setChecked(context.checkSelfPermission(Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED);

        c = findViewById(R.id.micAccess);
        c.setChecked(context.checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED);

        c = findViewById(R.id.camAccess);
        c.setChecked(context.checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED);

        c = findViewById(R.id.overlayPerm);
        c.setChecked(Settings.canDrawOverlays(this));

        c = findViewById(R.id.powerMenu);
        c.setChecked(isAccessibilitySettingsOn());

        c = findViewById(R.id.writeSystem);
        c.setChecked(Settings.System.canWrite(this));
    }

    public boolean isTv() {
        UiModeManager uiModeManager =
                (UiModeManager) this.getApplicationContext().getSystemService(UI_MODE_SERVICE);
        return uiModeManager != null
                && uiModeManager.getCurrentModeType() == Configuration.UI_MODE_TYPE_TELEVISION;
    }

    public void setLauncher(View v) {
        PackageManager packageManager = context.getPackageManager();
        ComponentName componentName = new ComponentName(context, DummyActivity.class);
        packageManager.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);

        Intent selector = new Intent(Intent.ACTION_MAIN);
        selector.addCategory(Intent.CATEGORY_HOME);
        selector.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(selector);

        packageManager.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_DEFAULT, PackageManager.DONT_KILL_APP);
    }

    public void startSystemSettings(View v){
        startActivity(new Intent(Settings.ACTION_SETTINGS));
    }

    public void sdClick(View view) {
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
            startActivityForResult(intent, 4713);
        }
        c = findViewById(R.id.powerMenu);
        c.setChecked(isAccessibilitySettingsOn());
    }

    public void checkOverlayPermission(View v){
        if (android.os.Build.VERSION.SDK_INT >= 23 && !Settings.canDrawOverlays(SettingsActivity.this)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
            intent.setData(Uri.parse("package:" + getPackageName()));
            ((Activity) context).startActivityForResult(intent, 4712);
        }
        c = findViewById(R.id.overlayPerm);
        c.setChecked(Settings.canDrawOverlays(this));
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void checkWritePermission(View v){
        if (! Settings.System.canWrite(this))
        {
            Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
            intent.setData(Uri.parse("package:" + this.getPackageName()));
            startActivityForResult(intent, 4714);
        }
        c = findViewById(R.id.writeSystem);
        c.setChecked(Settings.System.canWrite(this));
    }

    public void checkMicPermission(View v){
        if (ContextCompat.checkSelfPermission(SettingsActivity.this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(SettingsActivity.this,new String[]{
                    Manifest.permission.RECORD_AUDIO}, 4715);
        }
        c = findViewById(R.id.micAccess);
        c.setChecked(context.checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED);
    }

    public void checkPhonePermission(View v){
        if (ContextCompat.checkSelfPermission(SettingsActivity.this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(SettingsActivity.this,new String[]{
                    Manifest.permission.READ_PHONE_STATE}, 4716);
        }
        c = findViewById(R.id.phoneAccess);
        c.setChecked(context.checkSelfPermission(Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED);
    }

    public void checkCameraPermission(View v){
        if (ContextCompat.checkSelfPermission(SettingsActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(SettingsActivity.this,new String[]{
                    Manifest.permission.CAMERA}, 4711);
        }
        c = findViewById(R.id.camAccess);
        c.setChecked(context.checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 4711)
        {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                c = findViewById(R.id.camAccess);
                c.setChecked(true);
            }
        }
        if (requestCode == 4715)
        {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                c = findViewById(R.id.micAccess);
                c.setChecked(true);
            }
        }
        if (requestCode == 4716)
        {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                c = findViewById(R.id.phoneAccess);
                c.setChecked(true);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 4711)
        {
            if (context.checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                c = findViewById(R.id.camAccess);
                c.setChecked(true);
            }
        }
        if (requestCode == 4712)
        {
            if (Settings.canDrawOverlays(this)) {
                c = findViewById(R.id.overlayPerm);
                c.setChecked(true);
            }
        }
        else if (requestCode == 4713)
        {
            if (isAccessibilitySettingsOn()) {
                c = findViewById(R.id.powerMenu);
                c.setChecked(true);
            }
        }
        else if (requestCode == 4714)
        {
            if (Settings.System.canWrite(this)) {
                c = findViewById(R.id.writeSystem);
                c.setChecked(true);
            }
        }
        else if (requestCode == 4715)
        {
            if (context.checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                c = findViewById(R.id.micAccess);
                c.setChecked(true);
            }
        }
        else if (requestCode == 4716)
        {
            if ((context.checkSelfPermission(Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED)) {
                c = findViewById(R.id.phoneAccess);
                c.setChecked(true);
            }
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

    private boolean checkApp(String uri) {
        PackageInfo pkgInfo;
        try {
            pkgInfo = getPackageManager().getPackageInfo(uri, 0);
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
        return pkgInfo != null;
    }
}
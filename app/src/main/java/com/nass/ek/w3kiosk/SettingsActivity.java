package com.nass.ek.w3kiosk;

import android.Manifest;
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
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

@RequiresApi(api = Build.VERSION_CODES.M)
public class SettingsActivity extends AppCompatActivity {

    Context context = this;
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    Switch s;
    EditText e;
    CheckBox c;
    String[] allowedApps = new String[]{"0", "1", "2", "3", "4", "5"};
    String[] urlTimeout = new String[]{"---", "30", "60", "90", "120", "150", "180"};
    private Spinner appsDropdown;
    private boolean appsDropdownInitialized;
    private Spinner timeoutDropdown;
    private boolean timeoutDropdownInitialized;


    @RequiresApi(api = Build.VERSION_CODES.O)
    @SuppressLint("ApplySharedPref")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        toggleLogin(findViewById(R.id.autologinLayout));
        TextView client1Text = findViewById(R.id.client1Text);
        TextView client2Text = findViewById(R.id.client2Text);
        client1Text.setText(String.format(getString(R.string.website1), getString(R.string.url_preset)));
        client2Text.setText(getString(R.string.website2));
        appsDropdown = findViewById(R.id.appsSpinner);
        ArrayAdapter<String> appAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, allowedApps);
        appAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        appsDropdown.setAdapter(appAdapter);
        appsDropdown.setOnLongClickListener(v -> {
            startActivity(new Intent(this, AppsActivity.class));
            return true;
        });
        timeoutDropdown = findViewById(R.id.timeoutSpinner);
        ArrayAdapter<String> timeoutAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, urlTimeout);
        timeoutAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        timeoutDropdown.setAdapter(timeoutAdapter);
        findViewById(R.id.setLauncherButton).setVisibility(View.VISIBLE);
        if (MainActivity.isTablet() || MainActivity.isScanner()){
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
                findViewById(R.id.autologinLayout).setVisibility(View.VISIBLE);
            }
        }

        if (isScanner()){
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
                    findViewById(R.id.permissionLayout).setVisibility(View.VISIBLE);
                }
                if (checkApp("com.rscja.scanner")){
                findViewById(R.id.scannerButton).setVisibility(View.VISIBLE);
            }
            findViewById(R.id.client2Text).setVisibility(View.GONE);
            findViewById(R.id.client2EditText).setVisibility(View.GONE);
            findViewById(R.id.timeoutText).setVisibility(View.GONE);
            findViewById(R.id.timeoutSpinner).setVisibility(View.GONE);
        }

        ImageButton b = findViewById(R.id.updateCloseButton);
        b.setOnClickListener(view -> {
            SharedPreferences.Editor editor = sharedPreferences.edit();

            s = findViewById(R.id.autofillToggle);
            editor.putBoolean("checkAutofill", s.isChecked());

            s = findViewById(R.id.mobileToggle);
            editor.putBoolean("mobileMode", s.isChecked());

            editor.putInt("appsCount", Integer.parseInt(appsDropdown.getSelectedItem().toString()));

            editor.putInt("urlTimeout", timeoutDropdown.getSelectedItemPosition());

            s = findViewById(R.id.autoLogin);
            editor.putBoolean("autoLogin", s.isChecked());

            e = findViewById(R.id.loginEditText);
            editor.putString("loginName", e.getText().toString());

            e = findViewById(R.id.pwEditText);
            editor.putString("loginPassword", e.getText().toString());

            e = findViewById(R.id.client1EditText);
            editor.putString("clientUrl1", e.getText().toString());

            e = findViewById(R.id.client2EditText);
            editor.putString("clientUrl2", e.getText().toString());

            editor.commit();
            finish();
        });

        s = findViewById(R.id.autofillToggle);
        s.setChecked(sharedPreferences.getBoolean("checkAutofill", false));

        s = findViewById(R.id.mobileToggle);
        s.setChecked(sharedPreferences.getBoolean("mobileMode", false));

        appsDropdown.setSelection(sharedPreferences.getInt("appsCount", 0));
        int spinnerValue = sharedPreferences.getInt("urlTimeout",-1);
        if(spinnerValue != -1)
        timeoutDropdown.setSelection(spinnerValue);

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

        e = findViewById(R.id.loginEditText);
        e.setText(sharedPreferences.getString("loginName", ""));

        e = findViewById(R.id.pwEditText);
        e.setText(sharedPreferences.getString("loginPassword", ""));

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            c = findViewById(R.id.phoneAccess);
            c.setChecked(context.checkSelfPermission(Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED);

            c = findViewById(R.id.camAccess);
            c.setChecked(context.checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED);

            c = findViewById(R.id.overlayPerm);
            c.setChecked(Settings.canDrawOverlays(this));

            c = findViewById(R.id.powerMenu);
            c.setChecked(isAccessibilitySettingsOn());

            c = findViewById(R.id.writeSystem);
            c.setChecked(Settings.System.canWrite(this));
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

    public boolean isTv() {
        UiModeManager uiModeManager =
                (UiModeManager) this.getApplicationContext().getSystemService(UI_MODE_SERVICE);
        return uiModeManager != null
                && uiModeManager.getCurrentModeType() == Configuration.UI_MODE_TYPE_TELEVISION;
    }

    public static boolean isScanner() {
        return android.os.Build.MODEL.toUpperCase().startsWith("C4050") || android.os.Build.MODEL.toUpperCase().startsWith("C72") || android.os.Build.MODEL.toUpperCase().startsWith("C61") || Build.PRODUCT.startsWith("cedric");
    }

    public void setLauncher(View v) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            PackageManager packageManager = context.getPackageManager();
        ComponentName componentName = new ComponentName(context, DummyActivity.class);
        packageManager.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);

        Intent selector = new Intent(Intent.ACTION_MAIN);
        selector.addCategory(Intent.CATEGORY_HOME);
        selector.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(selector);

        packageManager.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_DEFAULT, PackageManager.DONT_KILL_APP);
    } else {
            startActivity(new Intent(Settings.ACTION_HOME_SETTINGS));
        }
    }

    public void startSystemSettings(View v){
        startActivity(new Intent(Settings.ACTION_SETTINGS));
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
            startActivityForResult(intent, 4713);
        }
    }

    public void checkOverlayPermission(View v){
        if (android.os.Build.VERSION.SDK_INT >= 23 && !Settings.canDrawOverlays(SettingsActivity.this)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
            intent.setData(Uri.parse("package:" + getPackageName()));
            ((Activity) context).startActivityForResult(intent, 4712);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void checkWritePermission(View v){
        if (! Settings.System.canWrite(this))
        {
            Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
            intent.setData(Uri.parse("package:" + this.getPackageName()));
            startActivityForResult(intent, 4714);
        }
    }

    public void checkPhonePermission(View v){
        if (ContextCompat.checkSelfPermission(SettingsActivity.this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(SettingsActivity.this,new String[]{
                    Manifest.permission.READ_PHONE_STATE}, 4716);
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

    @Override
    protected void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            c = findViewById(R.id.phoneAccess);
            c.setChecked(context.checkSelfPermission(Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED);

            c = findViewById(R.id.camAccess);
            c.setChecked(context.checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED);

            c = findViewById(R.id.overlayPerm);
            c.setChecked(Settings.canDrawOverlays(this));

            c = findViewById(R.id.powerMenu);
            c.setChecked(isAccessibilitySettingsOn());

            c = findViewById(R.id.writeSystem);
            c.setChecked(Settings.System.canWrite(this));
        }
    }

    public void checkAutofill(View V) {
        if (android.os.Build.VERSION.SDK_INT >= 26 && !isTv()) {
            Intent dialogIntent = new Intent(Settings.ACTION_REQUEST_SET_AUTOFILL_SERVICE);
            dialogIntent.setData(Uri.parse("package:none"));
            if (getSystemService(android.view.autofill.AutofillManager.class).isEnabled()) {
                android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(context);
                builder.setTitle(getString(R.string.disable_autofill))
                        .setMessage(R.string.autofill_text)
                        .setCancelable(false)
                        .setPositiveButton("OK", (dialog, which) -> startActivity(dialogIntent));
                android.app.AlertDialog dialog = builder.create();
                dialog.show();
            }
        }
    }
}
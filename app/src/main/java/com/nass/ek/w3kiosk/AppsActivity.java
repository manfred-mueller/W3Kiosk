package com.nass.ek.w3kiosk;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.nass.ek.w3kiosk.ChecksAndConfigs.PW1;
import static com.nass.ek.w3kiosk.ChecksAndConfigs.PW2;
import static com.nass.ek.w3kiosk.ChecksAndConfigs.PW3;
import static com.nass.ek.w3kiosk.ChecksAndConfigs.PW4;

public class AppsActivity extends AppCompatActivity {

    private static final String[] PASSWORDS = {PW1, PW2, PW3, PW4};

    public Button appButton;
    public String appUrl;
    public String appShort;

    private Button[] appButtons = new Button[4];
    private String[] appUrls = new String[4];
    private String[] appShorts = new String[4];

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apps);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        int appsCount = sharedPreferences.getInt("appsCount", 0);

        for (int i = 0; i < 4; i++) {
            appButtons[i] = findViewById(getResources().getIdentifier("app" + (i + 1) + "_Button", "id", getPackageName()));
            appUrls[i] = sharedPreferences.getString("app" + (i + 1), "");
            appShorts[i] = sharedPreferences.getString("app" + (i + 1) + "Short", null);

            if (appsCount > i) {
                if (!appUrls[i].isEmpty()) {
                    setupAppButton(i, appUrls[i], appShorts[i]);
                } else {
                    setupChooseAppButton(i);
                }
            }
        }
    }

    private void setupAppButton(int index, String appUrl, String appShort) {
        Drawable icon = getAppIcon(appUrl);
        appButtons[index].setCompoundDrawablesWithIntrinsicBounds(icon, null, null, null);
        appButtons[index].setText(appShort);
        appButtons[index].setOnClickListener(v -> appClick(appUrl));
        appButtons[index].setOnLongClickListener(v -> {
            deleteApp(getString(R.string.enterPassword), String.valueOf(index + 1));
            return true;
        });
        appButtons[index].setVisibility(View.VISIBLE);
    }

    private void setupChooseAppButton(int index) {
        appButtons[index].setVisibility(View.VISIBLE);
        appButtons[index].setOnClickListener(v -> chooseApp(String.valueOf(index + 1)));
    }

    private Drawable getAppIcon(String packageName) {
        Drawable icon = null;
        try {
            icon = getPackageManager().getApplicationIcon(packageName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return icon;
    }

    private void deleteApp(String title, String appId) {
        LayoutInflater li = LayoutInflater.from(this);
        View prompt = li.inflate(R.layout.check_password_dialog, null);
        AlertDialog.Builder checkPasswordDialog = new AlertDialog.Builder(this);
        checkPasswordDialog.setView(prompt);
        final EditText password = prompt.findViewById(R.id.check_password);

        checkPasswordDialog.setTitle(title)
                .setCancelable(false)
                .setPositiveButton("Ok", (dialog, id) -> {
                    String pwInput = password.getText().toString();
                    if (Arrays.asList(PW1, PW2, PW3, PW4).contains(pwInput)) {
                        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
                        SharedPreferences.Editor editor = sharedPreferences.edit();

                        int buttonId = getResources().getIdentifier("app" + appId + "_Button", "id", getPackageName());
                        appButton = findViewById(buttonId);
                        appUrl = "app" + appId;
                        appShort = "app" + appId + "Short";

                        appButton.setText("");
                        appButton.setOnClickListener(null);
                        editor.putString(appUrl, "");
                        editor.putString(appShort, "");
                        editor.apply();
                        finish();
                        startActivity(getIntent());
                    } else {
                        dialog.cancel();
                    }
                })
                .setNegativeButton(R.string.cancel, (dialog, id) -> dialog.cancel());

        AlertDialog dialog = checkPasswordDialog.create();
        dialog.show();
        dialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE).setAllCaps(false);
        dialog.getButton(android.app.AlertDialog.BUTTON_NEGATIVE).setAllCaps(false);
        dialog.getButton(android.app.AlertDialog.BUTTON_NEUTRAL).setAllCaps(false);
    }

    public void closeClick(View view) {
        finish();
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

    public void hideKeyboard(View v) {
        InputMethodManager imm = (InputMethodManager) this.getSystemService(Activity.INPUT_METHOD_SERVICE);

        View view = this.getCurrentFocus();
        if (view == null) {
            view = new View(this);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private void chooseApp(String appId) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setIcon(R.drawable.logo)
                .setTitle(R.string.choose_app);

        List<ApplicationInfo> launchableInstalledApps = new ArrayList<>();
        for (ApplicationInfo applicationInfo : getPackageManager().getInstalledApplications(PackageManager.GET_META_DATA)) {
            if (getPackageManager().getLaunchIntentForPackage(applicationInfo.packageName) != null) {
                launchableInstalledApps.add(applicationInfo);
            }
        }

        String[] shortListItems = new String[launchableInstalledApps.size()];
        for (int i = 0; i < launchableInstalledApps.size(); i++) {
            shortListItems[i] = launchableInstalledApps.get(i).loadLabel(getPackageManager()).toString();
        }

        int[] checkedItem = {-1};
        alertDialogBuilder.setSingleChoiceItems(shortListItems, checkedItem[0], (dialog, which) -> {
            checkedItem[0] = which;
            ApplicationInfo selectedApp = launchableInstalledApps.get(which);
            String shortName = selectedApp.loadLabel(getPackageManager()).toString();
            String longName = selectedApp.packageName;

            try {
                Drawable icon = getPackageManager().getApplicationIcon(longName);
                int buttonId = getResources().getIdentifier("app" + appId + "_Button", "id", getPackageName());
                appButton = findViewById(buttonId);
                appUrl = "app" + appId;
                appShort = "app" + appId + "Short";

                appButton.setCompoundDrawablesWithIntrinsicBounds(icon, null, null, null);
                appButton.setText(shortName);
                appButton.setOnClickListener(v -> appClick(longName));
                appButton.setOnLongClickListener(v -> {
                    deleteApp("Password", appId);
                    return true;
                });

                editor.putString(appUrl, longName);
                editor.putString(appShort, shortName);
                editor.apply();
                finish();
                startActivity(getIntent());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        alertDialogBuilder.setNegativeButton(getString(R.string.cancel), null);
        AlertDialog customAlertDialog = alertDialogBuilder.create();
        customAlertDialog.show();
        customAlertDialog.getButton(android.app.AlertDialog.BUTTON_NEGATIVE).setAllCaps(false);
    }
}

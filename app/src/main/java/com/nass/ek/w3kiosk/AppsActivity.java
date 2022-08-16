package com.nass.ek.w3kiosk;

import static com.nass.ek.w3kiosk.MainActivity.PASSWORD;

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
import java.util.List;


public class AppsActivity extends AppCompatActivity {

    public Button appButton;
    public Button app1Button;
    public Button app2Button;
    public Button app3Button;
    public Button app4Button;
    public Button app5Button;
    public String appUrl;
    public String appShort;
    public int appsCount;
    public String app1;
    public String app2;
    public String app3;
    public String app4;
    public String app5;
    public String app1Short;
    public String app2Short;
    public String app3Short;
    public String app4Short;
    public String app5Short;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apps);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        appsCount = sharedPreferences.getInt("appsCount", 0);
        app1 = sharedPreferences.getString("app1", "");
        app2 = sharedPreferences.getString("app2", "");
        app3 = sharedPreferences.getString("app3", "");
        app4 = sharedPreferences.getString("app4", "");
        app5 = sharedPreferences.getString("app5", "");
        app1Short = sharedPreferences.getString("app1Short", null);
        app2Short = sharedPreferences.getString("app2Short", null);
        app3Short = sharedPreferences.getString("app3Short", null);
        app4Short = sharedPreferences.getString("app4Short", null);
        app5Short = sharedPreferences.getString("app5Short", null);
        app1Button = findViewById(R.id.app1_Button);
        app2Button = findViewById(R.id.app2_Button);
        app3Button = findViewById(R.id.app3_Button);
        app4Button = findViewById(R.id.app4_Button);
        app5Button = findViewById(R.id.app5_Button);

//        findViewById(R.id.copyright_text).setOnClickListener(arg0 -> MainActivity.AboutBox.Show(AppsActivity.this));

        if (appsCount > 0) {
            if (!app1.isEmpty()) {
            Drawable icon = null;
            try {
                icon = getPackageManager().getApplicationIcon(app1);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            app1Button.setCompoundDrawablesWithIntrinsicBounds(icon, null, null, null);
            app1Button.setText(app1Short);
            app1Button.setOnClickListener(v -> appClick(app1));
            app1Button.setOnLongClickListener(v -> {
                deleteApp(getString(R.string.enterPassword), "1");
                return true;
            });
            app1Button.setVisibility(View.VISIBLE);
            } else {
            app1Button.setVisibility(View.VISIBLE);
            app1Button.setOnClickListener(v -> chooseApp("1"));
            }
        }

        if (appsCount > 1) {
            if (!app2.isEmpty()) {
                Drawable icon = null;
                try {
                    icon = getPackageManager().getApplicationIcon(app2);
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
                app2Button.setCompoundDrawablesWithIntrinsicBounds(icon, null, null, null);
                app2Button.setText(app2Short);
                app2Button.setOnClickListener(v -> appClick(app2));
                app2Button.setOnLongClickListener(v -> {
                    deleteApp(getString(R.string.enterPassword), "2");
                    return true;
                });
                app2Button.setVisibility(View.VISIBLE);
            } else {
                app2Button.setVisibility(View.VISIBLE);
                app2Button.setOnClickListener(v -> chooseApp("2"));
            }
        }

        if (appsCount > 2) {
            if (!app3.isEmpty()) {
            Drawable icon = null;
            try {
                icon = getPackageManager().getApplicationIcon(app3);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            app3Button.setCompoundDrawablesWithIntrinsicBounds(icon, null, null, null);
            app3Button.setText(app3Short);
            app3Button.setOnClickListener(v -> appClick(app3));
            app3Button.setOnLongClickListener(v -> {
                deleteApp(getString(R.string.enterPassword), "3");
                return true;
            });
            app3Button.setVisibility(View.VISIBLE);
            } else {
                app3Button.setVisibility(View.VISIBLE);
                app3Button.setOnClickListener(v -> chooseApp("3"));
            }
        }

        if (appsCount > 3) {
            if (!app4.isEmpty()) {
            Drawable icon = null;
            try {
                icon = getPackageManager().getApplicationIcon(app4);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            app4Button.setCompoundDrawablesWithIntrinsicBounds(icon, null, null, null);
            app4Button.setText(app4Short);
            app4Button.setOnClickListener(v -> appClick(app4));
            app4Button.setOnLongClickListener(v -> {
                deleteApp(getString(R.string.enterPassword), "4");
                return true;
            });
            app4Button.setVisibility(View.VISIBLE);
            } else {
                app4Button.setVisibility(View.VISIBLE);
                app4Button.setOnClickListener(v -> chooseApp("4"));
            }
        }

        if (appsCount > 4) {
            if (!app5.isEmpty()) {
            Drawable icon = null;
            try {
                icon = getPackageManager().getApplicationIcon(app5);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            app5Button.setCompoundDrawablesWithIntrinsicBounds(icon, null, null, null);
            app5Button.setText(app5Short);
            app5Button.setOnClickListener(v -> appClick(app5));
            app5Button.setOnLongClickListener(v -> {
                deleteApp(getString(R.string.enterPassword), "5");
                return true;
            });
            app5Button.setVisibility(View.VISIBLE);
            } else {
                app5Button.setVisibility(View.VISIBLE);
                app5Button.setOnClickListener(v -> chooseApp("5"));
            }
        }
    }

    private void deleteApp(String title, String AppId) {
        LayoutInflater li = LayoutInflater.from(this);
        View prompt = li.inflate(R.layout.check_password_dialog, null);
        androidx.appcompat.app.AlertDialog.Builder checkPasswordDialog = new androidx.appcompat.app.AlertDialog.Builder(this);
        checkPasswordDialog.setView(prompt);
        final EditText password = prompt.findViewById(R.id.check_password);

        checkPasswordDialog.setTitle(title);
        checkPasswordDialog.setCancelable(false)
                .setPositiveButton("Ok", (dialog, id) -> {
                    String PwInput = password.getText().toString();
                    if (PwInput.equals(PASSWORD)) {
                        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
                        SharedPreferences.Editor editor = sharedPreferences.edit();

                        switch (AppId) {
                            case "1":
                                appButton = findViewById(R.id.app1_Button);
                                appUrl = "app1";
                                appShort = "app1Short";
                                break;
                            case "2":
                                appButton = findViewById(R.id.app2_Button);
                                appUrl = "app2";
                                appShort = "app2Short";
                                break;
                            case "3":
                                appButton = findViewById(R.id.app3_Button);
                                appUrl = "app3";
                                appShort = "app3Short";
                                break;
                            case "4":
                                appButton = findViewById(R.id.app4_Button);
                                appUrl = "app4";
                                appShort = "app4Short";
                                break;
                            case "5":
                                appButton = findViewById(R.id.app5_Button);
                                appUrl = "app5";
                                appShort = "app5Short";
                                break;
                        }
                        appButton.setText("");
                        appButton.setOnClickListener(null);
                        editor.putString(appUrl, "");
                        editor.putString(appShort, "");
                        editor.apply();
                        finish();
                        startActivity(getIntent());
                    }
                    else {
                        dialog.cancel();
                    }
                });
        checkPasswordDialog.setNegativeButton(R.string.cancel, (dialog, id) -> dialog.cancel());
        androidx.appcompat.app.AlertDialog dialog = checkPasswordDialog.create();
        dialog.show();
        dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE).setAllCaps(false);
        dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_NEGATIVE).setAllCaps(false);
        dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_NEUTRAL).setAllCaps(false);
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

    private void chooseApp(String AppId) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(AppsActivity.this);
        alertDialog.setIcon(R.drawable.logo);
        alertDialog.setTitle(R.string.choose_app);

        List<ApplicationInfo> installedApps = getPackageManager().getInstalledApplications(PackageManager.GET_META_DATA);
        List<ApplicationInfo> launchableInstalledApps = new ArrayList<ApplicationInfo>();
        for(int i =0; i<installedApps.size(); i++){
            if(getPackageManager().getLaunchIntentForPackage(installedApps.get(i).packageName) != null){
                launchableInstalledApps.add(installedApps.get(i));
            }
        }

        String[] longListItems = new String[launchableInstalledApps.size()];
        String[] shortListItems = new String[launchableInstalledApps.size()];

        int j = 0;
        int k = 0;
        for (ApplicationInfo applicationInfo : launchableInstalledApps) {
                longListItems[j] = applicationInfo.packageName;
                shortListItems[k] = applicationInfo.loadLabel(getPackageManager()).toString();
            j++;
            k++;
        }

        int[] checkedItem = {-1};
        alertDialog.setSingleChoiceItems(shortListItems, checkedItem[0], (dialog, which) -> {
            checkedItem[0] = which;
            String shortName = shortListItems[which];
            String longName = longListItems[which];

            try
            {
                Drawable icon = getPackageManager().getApplicationIcon(longName);
                switch (AppId) {
                    case "1":
                        appButton = findViewById(R.id.app1_Button);
                        appUrl = "app1";
                        appShort = "app1Short";
                        break;
                    case "2":
                        appButton = findViewById(R.id.app2_Button);
                        appUrl = "app2";
                        appShort = "app2Short";
                        break;
                    case "3":
                        appButton = findViewById(R.id.app3_Button);
                        appUrl = "app3";
                        appShort = "app3Short";
                        break;
                    case "4":
                        appButton = findViewById(R.id.app4_Button);
                        appUrl = "app4";
                        appShort = "app4Short";
                        break;
                    case "5":
                        appButton = findViewById(R.id.app5_Button);
                        appUrl = "app5";
                        appShort = "app5Short";
                        break;
                }
                    appButton.setCompoundDrawablesWithIntrinsicBounds( icon, null, null, null);
                    appButton.setText(shortName);
                    appButton.setOnClickListener(v -> appClick(longName));
                    appButton.setOnLongClickListener(v -> {
                        deleteApp("Password", AppId);
                    return true;
                    });
                    editor.putString(appUrl, longName);
                    editor.putString(appShort, shortName);
                    editor.apply();
                    finish();
                    startActivity(getIntent());
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        });
        alertDialog.setNegativeButton(getString(R.string.cancel), (dialog, which) -> {

        });
        AlertDialog customAlertDialog = alertDialog.create();
        customAlertDialog.show();
        customAlertDialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_NEGATIVE).setAllCaps(false);
    }
}

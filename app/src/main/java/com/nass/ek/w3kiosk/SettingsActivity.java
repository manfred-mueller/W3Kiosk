package com.nass.ek.w3kiosk;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Switch;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

@RequiresApi(api = Build.VERSION_CODES.M)
public class SettingsActivity extends AppCompatActivity {

    Context context = this;
    @SuppressLint("ApplySharedPref")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        ImageButton b = findViewById(R.id.settingsSaveButton);
        b.setOnClickListener(view -> {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            @SuppressLint("UseSwitchCompatOrMaterialCode") Switch s;

            s = findViewById(R.id.autofillToggle);
            editor.putBoolean("checkAutofill", s.isChecked());

            s = findViewById(R.id.mobileToggle);
            editor.putBoolean("mobileMode", s.isChecked());

            EditText e;

            e = findViewById(R.id.clientEditText);
            if (e.getText().toString().isEmpty()){
                editor.putString("clientUrl", getString(R.string.default_url));
            }
            else
            {
                editor.putString("clientUrl", e.getText().toString());
            }
            editor.commit();
            restartApp();
        });

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch s;

        s = findViewById(R.id.autofillToggle);
        s.setChecked(sharedPreferences.getBoolean("checkAutofill", true));

        s = findViewById(R.id.mobileToggle);
        s.setChecked(sharedPreferences.getBoolean("mobileMode", false));

        EditText e;
        e = findViewById(R.id.clientEditText);
        e.setText(sharedPreferences.getString("clientUrl", null));
    }

    private void restartApp() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        this.startActivity(intent);
        Runtime.getRuntime().exit(0);
    }

    public void setLauncher(View v) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            final Intent intent = new Intent(Settings.ACTION_HOME_SETTINGS);
            startActivity(intent);
        } else {
            final Intent intent = new Intent(Settings.ACTION_SETTINGS);
            startActivity(intent);
        }
    }

    public void startSystemSettings(View v){
        startActivity(new Intent(Settings.ACTION_SETTINGS));
    }

    public void hideKeyboard(View v) {
        InputMethodManager imm = (InputMethodManager) this.getSystemService(Activity.INPUT_METHOD_SERVICE);

        View view = this.getCurrentFocus();
        if (view == null) {
            view = new View(this);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}
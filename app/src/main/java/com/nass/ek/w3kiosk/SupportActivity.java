package com.nass.ek.w3kiosk;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class SupportActivity extends AppCompatActivity {

    String tvUri = "com.teamviewer.quicksupport.market";
    String adUri = "com.anydesk.anydeskandroid";

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_support);
        boolean tvCheck = ChecksAndConfigs.checkApps(this, tvUri);
        boolean adCheck = ChecksAndConfigs.checkApps(this, adUri);

        if (ChecksAndConfigs.isTv()) {
            TextView txtTv = findViewById(R.id.textView);
            txtTv.setText(getString(R.string.helpTextTv));
            ImageView imgView = findViewById(R.id.imageView);
            imgView.setImageResource(R.drawable.remote);
            imgView.getLayoutParams().height = 400;
            imgView.requestLayout();
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

    public String getIP()
    {
        String ethAddr = execCommand(getString(R.string.commands_to_get_ip_on_eth0));
        String wifiAddr = execCommand(getString(R.string.commands_to_get_ip_on_wlan0));
        if (!ethAddr.isEmpty())
        {
            return ethAddr;
        }
        if (!wifiAddr.isEmpty())
        {
            return wifiAddr;
        }
        return "000.000.000.000";
    }

    public void setAdbPort(View view) {
        String tcpPort = execCommand(getString(R.string.commands_to_get_tcp_port_of_prop));
        execCommandsAsSU(new String[]{getString(R.string.commands_to_set_tcp_port_of_prop, tcpPort)});
        execCommandsAsSU(getResources().getStringArray(R.array.commands_to_enable_tcp_forward));
        Toast.makeText(this, "ADB active at: " + getIP() + ":" + tcpPort, Toast.LENGTH_LONG).show();
    }

    public void appClick(String uri) {

        Intent t;
        PackageManager manager = getPackageManager();
        try {
            t = manager.getLaunchIntentForPackage(uri);
            if (t == null)
                throw new PackageManager.NameNotFoundException();
            t.addCategory(Intent.CATEGORY_LAUNCHER);
            t.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
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

    public static String execCommand(String command) {
        try {
            Process process = Runtime.getRuntime().exec(command);

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            int read;
            char[] buffer = new char[4096];
            StringBuilder output = new StringBuilder();
            while ((read = reader.read(buffer)) > 0) {
                output.append(buffer, 0, read);
            }
            reader.close();

            process.waitFor();

            return output.toString();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static void execCommandsAsSU(String[] commands) {
        DataOutputStream os = null;
        try {
            Process process = Runtime.getRuntime().exec("su");
            os = new DataOutputStream(process.getOutputStream());

            for (String command : commands) {
                os.writeBytes(command + "\n");
                os.flush();
            }

            os.writeBytes("exit\n");
            os.flush();

            process.waitFor();
        } catch (IOException | InterruptedException e) {
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
            } catch (IOException e) {
                // Handle the exception or log it as needed
            }
        }

    }
}

package com.nass.ek.w3kiosk;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.UiModeManager;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.util.Enumeration;

public class SupportActivity extends AppCompatActivity {

    private static final String TAG = "W3kiosk";
    String tvUri = "com.teamviewer.quicksupport.market";
    String adUri = "com.anydesk.anydeskandroid";
    public String updateUrl;
    public String versionString;
    public boolean updateAvailable = false;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_support);
        boolean tvCheck = checkApps(tvUri);
        boolean adCheck = checkApps(adUri);

        if (isTv()){
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
        try {
            checkUpdate();
        } catch (IOException e) {
            e.printStackTrace();
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

    public boolean isTv() {
        UiModeManager uiModeManager =
                (UiModeManager) this.getApplicationContext().getSystemService(UI_MODE_SERVICE);
        return uiModeManager != null
                && uiModeManager.getCurrentModeType() == Configuration.UI_MODE_TYPE_TELEVISION;
    }

    private String getIpAddress() {
        String ip = "";
        try {
            Enumeration<NetworkInterface> enumNetworkInterfaces = NetworkInterface
                    .getNetworkInterfaces();
            while (enumNetworkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = enumNetworkInterfaces
                        .nextElement();
                Enumeration<InetAddress> enumInetAddress = networkInterface
                        .getInetAddresses();
                while (enumInetAddress.hasMoreElements()) {
                    InetAddress inetAddress = enumInetAddress.nextElement();

                    if (inetAddress.isSiteLocalAddress()) {
                        ip = inetAddress.getHostAddress();
                    }

                }

            }
        } catch (SocketException e) {
            e.printStackTrace();
            ip = "0.0.0.0";
        }
        return ip;
    }

    public void checkUpdate() throws IOException {
        Thread thread = new Thread(() -> {
            try  {
                URL url = null;
                try {
                    url = new URL("https://raw.githubusercontent.com/manfred-mueller/W3Kiosk/master/latest.version");
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
                BufferedReader reader = null;
                try {
                    reader = new BufferedReader(new InputStreamReader(url.openStream()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                String line;
                while ((line = reader.readLine()) != null) {
                    versionString = line;
                    reader.close();
                    TextView updateText = findViewById(R.id.txtUpdate);
                    if (isUpdateAvailable(versionString))
                    {
                        updateText.setText(String.format(getString(R.string.updateAvailable) ,versionString));
                        updateUrl = String.format("https://github.com/manfred-mueller/W3Kiosk/raw/master/app/release/w3kiosk-%1s-release.apk", versionString);
                        updateText.setOnClickListener(v -> getUpdate(updateUrl));
                    } else {
                        updateText.setText(String.format(getString(R.string.versionUptodate) ,versionString));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        thread.start();
    }

    public boolean isUpdateAvailable(String onlineVersion) {

        String localVersion = BuildConfig.VERSION_NAME + "." + BuildConfig.VERSION_CODE;
            int local_int = Integer.parseInt(localVersion.replaceAll("[\\D]", ""));
            int online_int = Integer.parseInt(onlineVersion.replaceAll("[\\D]", ""));
            if(local_int < online_int){
                return updateAvailable = true;
            }
        return false;
    }

    public void getUpdate(String uUrl) {
        try {
            uUrl = updateUrl;
            Uri uri = Uri.parse("googlechrome://navigate?url=" + uUrl);
            Intent i = new Intent(Intent.ACTION_VIEW, uri);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
        } catch (ActivityNotFoundException e) {
            // Chrome is probably not installed
        }
    }

}

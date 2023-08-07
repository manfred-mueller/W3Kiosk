package com.nass.ek.w3kiosk;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.UiModeManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.net.ConnectivityManagerCompat;
import androidx.preference.PreferenceManager;

import com.nass.ek.appupdate.UpdateWrapper;
import com.nass.ek.appupdate.services.TrustAllCertificates;

import java.util.Objects;

public class MainActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    public static String PW1;
    public static String PW2;
    public static String PW3;
    public static String PW4;

    static {
        try {
            PW1 = AESUtils.decrypt("0220972AE0731AD40F36FCA15AEEAF7B");
            PW2 = AESUtils.decrypt("B4EEA51496774A498A2A1F7D10EF2AAE");
            PW3 = AESUtils.decrypt("CB2BF4EF649D42ACEAC301C660C31262");
            PW4 = AESUtils.decrypt("FF7676CF6F7845B2E429718C5C0AE9BB");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public WebView kioskWeb;
    public String JavaString = "";
    Context context = this;
    public String urlPreset;
    public String autoName;
    public String autoPassWord;
    public boolean checkmobileMode;
    public boolean checkAutoLogin;
    public boolean autoUpdate;
    public String clientUrl1;
    public String clientUrl2;
    public String clientUrl3;
    public int appsCount;
    public int toSetting;
    public int handlerTimeout;
    public int toggleKey;
    public int zoom;
    public String nextUrl;
    public Handler handler;
    public Runnable runnable;

    public static String tvUri = "com.teamviewer.quicksupport.market";
    public static String adUri = "com.anydesk.anydeskandroid";

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public static boolean isScanner() {
        return android.os.Build.MODEL.toUpperCase().startsWith("C4050") || android.os.Build.MODEL.toUpperCase().startsWith("C66") || android.os.Build.MODEL.toUpperCase().startsWith("C72") || android.os.Build.MODEL.toUpperCase().startsWith("C61") || Build.PRODUCT.startsWith("cedric");
    }

    public static boolean isTablet() {
        return android.os.Build.MODEL.toUpperCase().startsWith("RK");
    }

    BroadcastReceiver connectionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null && intent.getAction().equals(ConnectivityManager
                    .CONNECTIVITY_ACTION)) {

                boolean isConnected = Objects.requireNonNull(ConnectivityManagerCompat.getNetworkInfoFromBroadcast
                        ((ConnectivityManager) context.getSystemService(CONNECTIVITY_SERVICE),
                                intent)).isConnected();

                if (isConnected) {
                    commitURL(urlPreset + clientUrl1);
                } else {
                    String noNet = context.getString(R.string.noNetwork);
                    String rawHTML = "<HTML>"+ "<body><table width=\"100%\" height=\"100%\"><td height=\"30%\"></td><tr><td height=\"40%\" align=\"center\" valign=\"middle\"><h1>" + noNet +"</h1></td><tr><td height=\"30%\"></td></table></body>"+ "</HTML>";
                    kioskWeb.loadData(rawHTML, "text/HTML", "UTF-8");
                }
            }
        }
    };

    @RequiresApi(api = Build.VERSION_CODES.M)
    @SuppressLint({"ApplySharedPref", "HardwareIds"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        checkmobileMode = sharedPreferences.getBoolean("mobileMode", false);
        checkAutoLogin = sharedPreferences.getBoolean("autoLogin", false);
        autoUpdate = sharedPreferences.getBoolean("autoUpdate", false);
        appsCount = sharedPreferences.getInt("appsCount", 0);
        toSetting = sharedPreferences.getInt("urlTimeout", 0);
        if (toSetting > 0) {
            handlerTimeout = toSetting * 30000;
        } else {
            handlerTimeout = toSetting;
        }
        zoom = sharedPreferences.getInt("zoomFactor", 5);
        clientUrl1 = sharedPreferences.getString("clientUrl1", "");
        clientUrl2 = sharedPreferences.getString("clientUrl2", "");
        clientUrl3 = sharedPreferences.getString("clientUrl3", "");
        autoName = sharedPreferences.getString("loginName", "");
        toggleKey = sharedPreferences.getInt("toggleKey", 82);
        autoPassWord = sharedPreferences.getString("loginPassword", "");
        urlPreset = getString(R.string.url_preset);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        kioskWeb = findViewById(R.id.kioskView);
        handler = new Handler();
        runnable = () -> {
            commitURL(urlPreset + clientUrl1);
            if (!clientUrl2.equals("")) {
                nextUrl = clientUrl2;
            } else
            {
                nextUrl = clientUrl1;
            }
        };

        if (autoUpdate) {
            checkUpdate();
        }

        if (isTv()) {
            new CountDownTimer(60000, 1000) {
                public void onTick(long millisUntilFinished) {
                }
                public void onFinish() {
                    findViewById(R.id.settingsButton).setVisibility(View.GONE);
                }
            }.start();
        }

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

        final int flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
            getWindow().getDecorView().setSystemUiVisibility(flags);
            final View decorView = getWindow().getDecorView();
            decorView.setOnSystemUiVisibilityChangeListener(visibility -> {
                if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                    decorView.setSystemUiVisibility(flags);
                }
            });

        if (isScanner()) {
            Intent startScannerActivityIntent = new Intent(getApplicationContext(), ScannerActivity.class);
            startActivity(startScannerActivityIntent);
        } else {
            setupSettings();
            commitURL(urlPreset + clientUrl1);
        }
        nextUrl = clientUrl2;
        Intent mIntent = getIntent();
        String action = mIntent.getAction();
        if (action != null && action.equals(Intent.ACTION_VIEW)) {
            commitURL(mIntent.getData().toString());
        }
    }

    private void checkPassword(String title) {
        LayoutInflater li = LayoutInflater.from(this);
        View prompt = li.inflate(R.layout.check_password_dialog, null);
        AlertDialog.Builder checkPasswordDialog = new AlertDialog.Builder(this);
        checkPasswordDialog.setView(prompt);
        final EditText password = prompt.findViewById(R.id.check_password);

        checkPasswordDialog.setTitle(title);
        checkPasswordDialog.setCancelable(false)
                .setPositiveButton("Ok", (dialog, id) -> {
                    String PwInput = password.getText().toString();
                    if (PwInput.equals("exit")) {
                        finish();
                    }
                    else if (PwInput.equals("h")) {
                        Intent startSupportActivityIntent = new Intent(getApplicationContext(), SupportActivity.class);
                        startActivity(startSupportActivityIntent);
                    }
                    else if (PwInput.equals("i")) {
                        Intent startAboutActivityIntent = new Intent(getApplicationContext(), AboutActivity.class);
                        startActivity(startAboutActivityIntent);
                    }
                    else if (PwInput.equals("a")) {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + adUri));
                        startActivity(intent);
                    }
                    else if (PwInput.equals("b")) {
                        Intent intent = new Intent(Settings.ACTION_BLUETOOTH_SETTINGS);
                        startActivity(intent);
                    }
                    else if (PwInput.equals("r")) {
                        startService(new Intent(this, ShutdownService.class));
                    }
                    else if (PwInput.equals("t")) {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + tvUri));
                        startActivity(intent);
                    }
                    else if (PwInput.equals(PW1) || PwInput.equals(PW2) || PwInput.equals(PW3) || PwInput.equals(PW4)) {
                        @SuppressLint({"NewApi", "LocalSuppress"}) Intent startSettingsActivityIntent = new Intent(getApplicationContext(), SettingsActivity.class);
                        startActivity(startSettingsActivityIntent);
                    }
                    else if (PwInput.equals("w")) {
                        Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
                        startActivity(intent);
                    }
                    else {
                        dialog.cancel();
                    }
                });
        checkPasswordDialog.setNegativeButton(R.string.cancel, (dialog, id) -> dialog.cancel());
        if (checkAutoLogin && !autoName.isEmpty() && !autoPassWord.isEmpty() && clientUrl2.isEmpty())
        {
            checkPasswordDialog.setNeutralButton("Autologin", (dialog, id) -> commitURL(urlPreset + clientUrl1));
        }
        else if (!clientUrl2.isEmpty()) {
            checkPasswordDialog.setNeutralButton(R.string.toggleUrl, (dialog, id) -> toggleUrl());
        }
        else if (appsCount > 0)
        {
            checkPasswordDialog.setNeutralButton(R.string.apps, (dialog, id) -> startActivity(new Intent(this, AppsActivity.class)));
        } else
        {
            checkPasswordDialog.setNeutralButton(R.string.reboot, (dialog, id) -> startService(new Intent(this, ShutdownService.class)));
        }
        AlertDialog dialog = checkPasswordDialog.create();
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setAllCaps(false);
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setAllCaps(false);
        dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setAllCaps(false);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @SuppressLint("SetJavaScriptEnabled")
    private void setupSettings() {
        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);
        ImageButton settingsButton = findViewById(R.id.settingsButton);
        if (isTv()) {
            settingsButton.setOnLongClickListener(v -> {
                toggleUrl();
                return true;
            });
        } else {
            settingsButton.setOnLongClickListener(v -> {
                recreate();
                return true;
            });
        }
        settingsButton.setOnClickListener(view -> checkPassword(getString(R.string.code_or_help)));

        kioskWeb.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onPermissionRequest(PermissionRequest request) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    request.grant(request.getResources());
                }
            }
        });

        kioskWeb.setWebViewClient(new WebViewClient()
        {
            public void onReceivedError(WebView webView, int errorCode, String description, String failingUrl) {
            try {
                webView.stopLoading();
            } catch (Exception ignored) {
            }
                String noNet = context.getString(R.string.noNetwork);
                String rawHTML = "<HTML>"+ "<body><table width=\"100%\" height=\"100%\"><td height=\"30%\"></td><tr><td height=\"40%\" align=\"center\" valign=\"middle\"><h1>" + noNet +"</h1></td><tr><td height=\"30%\"></td></table></body>"+ "</HTML>";
                kioskWeb.loadData(rawHTML, "text/HTML", "UTF-8");
            AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
            alertDialog.setTitle(getString(R.string.error));
            alertDialog.setMessage(getString(R.string.check_internet));
            alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.try_again), (dialog, which) -> commitURL(urlPreset + clientUrl1));

            alertDialog.show();
            super.onReceivedError(webView, errorCode, description, failingUrl);
        }
        });

        kioskWeb.clearCache(true);
        kioskWeb.clearHistory();
        kioskWeb.getSettings().setJavaScriptEnabled(true);
        kioskWeb.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        kioskWeb.getSettings().setMediaPlaybackRequiresUserGesture(false);
        kioskWeb.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        kioskWeb.getSettings().setDomStorageEnabled(true);
        setMobileMode(checkmobileMode);
        registerForContextMenu(kioskWeb);
    }

    @SuppressLint("SourceLockedOrientationActivity")
    public void setMobileMode(final boolean enabled) {
        final WebSettings webSettings = this.kioskWeb.getSettings();
        final String newUserAgent;
        if ((enabled) || isScanner()) {
            newUserAgent = webSettings.getUserAgentString().replace("Safari", "Mobile Safari");
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        else {
            newUserAgent = webSettings.getUserAgentString().replace("Mobile Safari", "Safari");
        }
        webSettings.setUserAgentString(newUserAgent);
        webSettings.setUseWideViewPort(enabled);
        webSettings.setLoadWithOverviewMode(enabled);
        webSettings.setSupportZoom(enabled);
        webSettings.setBuiltInZoomControls(enabled);
    }

    private void commitURL(String url) {
        kioskWeb.getSettings().setTextZoom(75 + (zoom * 5));
        if (url.equals(urlPreset))
        {
            @SuppressLint({"NewApi", "LocalSuppress"}) Intent startSettingsActivityIntent = new Intent(getApplicationContext(), SettingsActivity.class);
            startActivity(startSettingsActivityIntent);
        }
        if (isNetworkAvailable()) {
            if (!autoName.isEmpty() && !autoPassWord.isEmpty())
            {
                JavaString = "javascript:window.frames[\"Mainpage\"].document.getElementsByName('login')[0].value='" +
                        autoName + "';" +
                        "javascript:window.frames[\"Mainpage\"].document.getElementsByName('pwd')[0].value='" +
                        autoPassWord + "';";
                if (checkAutoLogin) {
                    JavaString += "javascript:window.frames[\"Mainpage\"].document.getElementById('logon').click()";
                }
                kioskWeb.setWebViewClient(new WebViewClient() {
                    public void onPageFinished(WebView view, String url) {
                            view.evaluateJavascript(JavaString, s -> {
                            });
                    }
                });
            }
            kioskWeb.setWebContentsDebuggingEnabled(true);
            kioskWeb.loadUrl(url);
        }
        else
        {
            String noNet = context.getString(R.string.noNetwork);
            String rawHTML = "<HTML>"+ "<body><table width=\"100%\" height=\"100%\"><td height=\"30%\"></td><tr><td height=\"40%\" align=\"center\" valign=\"middle\"><h1>" + noNet +"</h1></td><tr><td height=\"30%\"></td></table></body>"+ "</HTML>";
            kioskWeb.loadData(rawHTML, "text/HTML", "UTF-8");
        }
        hideKeyboard(this);
        findViewById(R.id.settingsButton).bringToFront();
    }

    private void toggleUrl(){
        if (nextUrl.equals(clientUrl3)){
            if (clientUrl3.startsWith("http")) {
                if (isTablet()) {
                    TrustAllCertificates.install();
                    kioskWeb.getSettings().setUserAgentString("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/81.0.4044.66 Safari/537.36");
                }
                commitURL(clientUrl3);
                if (handlerTimeout > 0) {
                    startHandler();
                }
            } else {
                commitURL(urlPreset + clientUrl3);
            }
            nextUrl = clientUrl1;
        }
        else if (nextUrl.equals(clientUrl2)){
            if (clientUrl2.startsWith("http")) {
                if (isTablet()) {
                    TrustAllCertificates.install();
                    kioskWeb.getSettings().setUserAgentString("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/81.0.4044.66 Safari/537.36");
                }
                commitURL(clientUrl2);
                if (handlerTimeout > 0) {
                    startHandler();
                }
            } else {
                commitURL(urlPreset + clientUrl2);
            }
            if (!clientUrl3.equals("")) {
                nextUrl = clientUrl3;
            } else
            {
                nextUrl = clientUrl1;
            }
        }
        else if (nextUrl.equals(clientUrl1)){
            commitURL(urlPreset + clientUrl1);
            nextUrl = clientUrl2;
            stopHandler();
        }
    }

    private void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        View view = activity.getCurrentFocus();
         if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    @Override
    protected void onDestroy() {
        PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(this);
        Runtime.getRuntime().exit(0);
        super.onDestroy();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        recreate();
    }

    public boolean isTv() {
        UiModeManager uiModeManager =
                (UiModeManager) this.getApplicationContext().getSystemService(UI_MODE_SERVICE);
        return uiModeManager != null
                && uiModeManager.getCurrentModeType() == Configuration.UI_MODE_TYPE_TELEVISION;
    }

    @Override
    public void onBackPressed() {
        if (isTv()) {
            toggleUrl();
        } else
        kioskWeb.goBack();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                    View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(connectionReceiver, filter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            unregisterReceiver(connectionReceiver);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (isTv()) {
            if (event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_MENU) {
                kioskWeb.showContextMenu();
                return true;
            } else if (event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == 23){
                recreate();
                return true;
            }
        }
        return super.dispatchKeyEvent(event);
    }

    public void toggleSettingsButton() {
        View buttonView = findViewById(R.id.settingsButton);
            if(buttonView.getVisibility()==View.GONE)
                buttonView.setVisibility(View.VISIBLE);
            else if(buttonView.getVisibility()==View.VISIBLE)
                buttonView.setVisibility(View.GONE);
    }
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.setHeaderTitle(R.string.chooseAction);
        menu.add(0, 1, 0, R.string.toggleUrl);
        menu.add(0, 2, 0, R.string.deactivateMenubutton);
        menu.add(0, 3, 0, R.string.settings);
        menu.add(0, 4, 0, R.string.showHelp);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (item.getItemId() == 1) {
            toggleUrl();
        } else if (item.getItemId() == 2) {
            toggleSettingsButton();
        } else if (item.getItemId() == 3) {
            checkPassword(getString(R.string.code_or_help));
        } else if (item.getItemId() == 4) {
            Intent startSupportActivityIntent = new Intent(getApplicationContext(), SupportActivity.class);
            startActivity(startSupportActivityIntent);
        }
        return true;
    }
    @Override
    public void onUserInteraction() {
        super.onUserInteraction();
        if (nextUrl.equals(clientUrl1) && handlerTimeout > 0) {
            stopHandler();
            startHandler();
        }
    }
    public void stopHandler() {
        handler.removeCallbacks(runnable);
    }
    public void startHandler() {
        handler.postDelayed(runnable, handlerTimeout);
    }

    public void checkUpdate() {

        String updateFound=(String.format(getString(R.string.UpdateAvailable), getString(R.string.app_name)));
        UpdateWrapper updateWrapper = new UpdateWrapper.Builder(MainActivity.this)
                .setTime(3000)
                .setNotificationIcon(R.mipmap.ic_launcher)
                .setUpdateTitle(updateFound)
                .setUpdateContentText(getString(R.string.UpdateDescription))
                .setUrl("https://raw.githubusercontent.com/manfred-mueller/W3Kiosk/master/w3kiosk.json")
                .setIsShowToast(true)

                .setCallback((model, hasNewVersion) -> {
                    Log.d("Latest Version", hasNewVersion + "");
                    Log.d("Version Name", model.getVersionName());
                    Log.d("Version Code", model.getVersionCode() + "");
                    Log.d("Version Description", model.getContentText());
                    Log.d("Min Support", model.getMinSupport() + "");
                    Log.d("Download URL", model.getUrl() + "");
                })
                .build();

        updateWrapper.start();
    }
}

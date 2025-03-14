package com.nass.ek.w3kiosk;

import static android.webkit.WebView.setWebContentsDebuggingEnabled;
import static com.nass.ek.w3kiosk.ChecksAndConfigs.PW1;
import static com.nass.ek.w3kiosk.ChecksAndConfigs.PW2;
import static com.nass.ek.w3kiosk.ChecksAndConfigs.PW3;
import static com.nass.ek.w3kiosk.ChecksAndConfigs.PW4;
import static com.nass.ek.w3kiosk.ChecksAndConfigs.checkApps;
import static com.nass.ek.w3kiosk.ChecksAndConfigs.isTablet;
import static com.nass.ek.w3kiosk.ChecksAndConfigs.isTv;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.LinkAddress;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.provider.Settings;
import android.text.Layout;
import android.text.SpannableString;
import android.text.style.AlignmentSpan;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.webkit.CookieSyncManager;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.core.net.ConnectivityManagerCompat;
import androidx.preference.PreferenceManager;

import com.nass.ek.appupdate.UpdateWrapper;
import com.nass.ek.appupdate.services.TrustAllCertificates;

import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.URL;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    public WebView kioskWeb;
    public String JavaString = "";
    Context context = this;
    public String urlPreset;
    public String autoName;
    public String autoPassWord;
    public boolean checkmobileMode;
    public boolean checkAutoLogin;
    public boolean autoUpdate;
    public boolean marquee;
    private String marqueeText;
    private boolean marqueeVisible;
    public String localIp;
    public String clientUrl1;
    public String clientUrl2;
    public String clientUrl3;
    public int appsCount;
    public int toSetting;
    public int mqtoSetting;
    public int urlTimeout;
    public int marqueeTimeout;
    private int marqueeBgColor;
    private int marqueeTxColor;
    private int marqueeSpeed;
    public int toggleKey;
    public int zoom;
    private String previousUrl;
    public String nextUrl;
    public Handler urlHandler;
    public Runnable urlRunnable;
    public Handler marqueeHandler;
    public Runnable marqueeRunnable;

    public SharedPreferences sharedPreferences;
    public static String tvUri = "com.teamviewer.quicksupport.market";
    public static String adUri = "com.anydesk.anydeskandroid";
    public String adbUri = "com.cgutman.androidremotedebugger";

    private DevicePolicyManager mDevicePolicyManager;
    private ComponentName mComponentName;

    private int upPressCount = 0;
    private int rightPressCount = 0;
    private long lastKeyPressTime = 0;
    private static final long KEY_SEQUENCE_TIMEOUT = 2000; // 2 seconds timeout
    private static final String TAG = "KeyEventDebug";



    BroadcastReceiver connectionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null && Objects.equals(intent.getAction(), ConnectivityManager
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
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        enableImmersiveMode();
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        checkmobileMode = sharedPreferences.getBoolean("mobileMode", false);
        checkAutoLogin = sharedPreferences.getBoolean("autoLogin", false);
        autoUpdate = sharedPreferences.getBoolean("autoUpdate", false);
        localIp = getLocalIpAddress(this);
        marquee = sharedPreferences.getBoolean("marquee", false);
        appsCount = sharedPreferences.getInt("appsCount", 0);
        toSetting = sharedPreferences.getInt("urlTimeout", 0);
        File marqueeFile = new File("/storage/emulated/0/Pictures/marquee.png");
        if (marqueeFile.exists()) {
            marqueeText="<img src=\"file:///storage/emulated/0/Pictures/marquee.png\"/>";
        } else {
            marqueeText="<img src=\"file:///android_res/drawable/logo_splash_web.png\"/>";
        }
        marqueeSpeed = sharedPreferences.getInt("marqueeSpeed", 25);
        marqueeBgColor = getResources().getColor(R.color.colorMarquee);
        marqueeTxColor = getResources().getColor(R.color.colorDarkGray);
        mqtoSetting = sharedPreferences.getInt("marqueeTimeout", 0);
        int[] marqueeTimeouts = {300000, 600000, 900000, 1200000, 1500000, 1800000, 2100000};
        if (mqtoSetting >= 1 && mqtoSetting <= 6) {
            marqueeTimeout = marqueeTimeouts[mqtoSetting - 1];
        } else {
            marqueeTimeout = 300000;
        }

        if (toSetting > 0) {
            urlTimeout = toSetting * 30000;
        } else {
            urlTimeout = toSetting;
        }

        if (isTablet() && marquee && marqueeTimeout > 0) {
            marqueeHandler = new Handler();
            marqueeRunnable = () -> {
                String htmlContent = generateMarqueeHtml(marqueeText, marqueeSpeed, marqueeBgColor);
                loadHtmlContent(htmlContent);
                marqueeVisible = true;
            };
            startMarqueeHandler();
        }

        if (isTv() && urlTimeout > 0) {
            urlHandler = new Handler();
            urlRunnable = this::toggleUrl;
            startUrlHandler();
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
        if (savedInstanceState != null)
            ((WebView)findViewById(R.id.kioskView)).restoreState(savedInstanceState);
        urlHandler = new Handler();
        urlRunnable = this::toggleUrl;

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

        setupSettings();

        if (ChecksAndConfigs.isScanner()) {
            Intent startScannerActivityIntent = new Intent(getApplicationContext(), ScannerActivity.class);
            startActivity(startScannerActivityIntent);
        } else {
            commitURL(urlPreset + clientUrl1);
        }
        nextUrl = clientUrl2;
        Intent mIntent = getIntent();
        String action = mIntent.getAction();
        if (action != null && action.equals(Intent.ACTION_VIEW)) {
            commitURL(mIntent.getData().toString());
        }
    }

    public void checkPassword(String title) {
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
                    else if (PwInput.equals("ad")) {
                        if (checkApps(this, adUri))
                        {
                            appClick(adUri);
                        } else {
                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + adUri));
                            startActivity(intent);
                        }                    }
                    else if (PwInput.equals("m")) {
                        String htmlContent = generateMarqueeHtml(marqueeText, marqueeSpeed, marqueeBgColor);
                        loadHtmlContent(htmlContent);
                        marqueeVisible = true;
                    }
                    else if (PwInput.equals("b")) {
                        Intent intent = new Intent(Settings.ACTION_BLUETOOTH_SETTINGS);
                        startActivity(intent);
                    }
                    else if (PwInput.equals("r")) {
                        startService(new Intent(this, ShutdownService.class));
                    }
                    else if (PwInput.equals("s")) {
                        openStorageManager(this);
                    }
                    else if (PwInput.equals("ra") && isTablet()) {
                        if (checkApps(this, adbUri))
                        {
                            appClick(adbUri);
                        } else {
                            Update("https://nass-ek.de/android/remote-adb-shell.apk", null);                        }
                    }
                    else if (PwInput.equals("sk")) {
                        Update("https://nass-ek.de/android/simple-keyboard-w3c.apk", null);
                    }
                    else if (PwInput.equals("tv")) {
                        if (checkApps(this, tvUri)) {
                            appClick(tvUri);
                        } else {
                            if (isTv()) {
                                // Install teamviewer-quicksupport.apk and tvaddon_TV.apk for TV
                                Update("https://download.teamviewer.com/download/TeamViewerQS.apk",
                                        "https://nass-ek.de/android/Teamviewer/tvaddon_TV.apk");
                            } else if (isTablet()) {
                                // Install teamviewer-quicksupport.apk and tvaddon_Tablet.apk for Tablet
                                Update("https://nass-ek.de/android/Teamviewer/teamviewer-quicksupport.apk",
                                        "https://nass-ek.de/android/Teamviewer/tvaddon_Tablet.apk");
                            } else {
                                // Open the Play Store link
                                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + tvUri));
                                startActivity(intent);
                            }
                        }
                    }
                    else if (PwInput.equals(PW1) || PwInput.equals(PW2) || PwInput.equals(PW3) || PwInput.equals(PW4)) {
                        openSettingsActivity();
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
                request.grant(request.getResources());
            }
        });

        marqueeBgColor = getResources().getColor(R.color.colorMarquee);
        String reLoad = context.getString(R.string.reLoad);
        String rawHTML = "<HTML>"+ "<body bgcolor=\"" + marqueeBgColor + "\"><table width=\"100%\" height=\"100%\"><td height=\"30%\"></td><tr><td height=\"40%\" align=\"center\" valign=\"middle\"><h1>" + reLoad +"</h1></td><tr><td height=\"30%\"></td></table></body>"+ "</HTML>";
        kioskWeb.loadData(rawHTML, "text/HTML", "UTF-8");

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
        if (isTv()) {
            registerForContextMenu(kioskWeb);
        }
    }

    @SuppressLint("SourceLockedOrientationActivity")
    public void setMobileMode(final boolean enabled) {
        final WebSettings webSettings = this.kioskWeb.getSettings();
        final String newUserAgent;
        if ((enabled) || ChecksAndConfigs.isScanner()) {
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
        previousUrl=url;
        kioskWeb.getSettings().setTextZoom(75 + (zoom * 5));
            if (url.equals(urlPreset)) {
                @SuppressLint({"NewApi", "LocalSuppress"}) Intent startSettingsActivityIntent = new Intent(getApplicationContext(), SettingsActivity.class);
                startActivity(startSettingsActivityIntent);
            }
            if (ChecksAndConfigs.isNetworkConnected(this)) {
                if (!autoName.isEmpty() && !autoPassWord.isEmpty()) {
                    JavaString = "javascript:window.frames[\"Mainpage\"].document.getElementsByName('login')[0].value='" +
                            autoName + "';" +
                            "javascript:window.frames[\"Mainpage\"].document.getElementsByName('pwd')[0].value='" +
                            autoPassWord + "';";
                    if (checkAutoLogin) {
                        JavaString += "javascript:window.frames[\"Mainpage\"].document.getElementById('logon').click()";
                    }
                    kioskWeb.setWebViewClient(new WebViewClient() {
                        public void onPageFinished(WebView view, String url) {
                            CookieSyncManager.getInstance().sync();
                            view.evaluateJavascript(JavaString, s -> {
                            });
                        }
                    });
                }
                setWebContentsDebuggingEnabled(true);
                kioskWeb.loadUrl(url);
            } else {
                String noNet = context.getString(R.string.noNetwork);
                String rawHTML = "<HTML>" + "<body><table width=\"100%\" height=\"100%\"><td height=\"30%\"></td><tr><td height=\"40%\" align=\"center\" valign=\"middle\"><h1>" + noNet + "</h1></td><tr><td height=\"30%\"></td></table></body>" + "</HTML>";
                kioskWeb.loadData(rawHTML, "text/HTML", "UTF-8");
            }
            hideKeyboard(this);
            findViewById(R.id.settingsButton).bringToFront();
    }

    private void toggleUrl(){
        if (nextUrl.equals(clientUrl3)){
            if (clientUrl3.startsWith("http")) {
                if (ChecksAndConfigs.isTablet()) {
                    TrustAllCertificates.install();
                    kioskWeb.getSettings().setUserAgentString("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/81.0.4044.66 Safari/537.36");
                }
                commitURL(clientUrl3);
            } else {
                commitURL(urlPreset + clientUrl3);
            }
            nextUrl = clientUrl1;
        }
        else if (nextUrl.equals(clientUrl2)){
            if (clientUrl2.startsWith("http")) {
                if (ChecksAndConfigs.isTablet()) {
                    TrustAllCertificates.install();
                    kioskWeb.getSettings().setUserAgentString("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/81.0.4044.66 Safari/537.36");
                }
                commitURL(clientUrl2);
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
        }
        if (isTv() && urlTimeout > 0) {
            stopUrlHandler();
            startUrlHandler();
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (!isTv()) {
            kioskWeb.goBack();
        }
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
        upPressCount = 0;
        rightPressCount = 0;
        enableImmersiveMode();
        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(connectionReceiver, filter);
        if (isTablet() && marquee && marqueeTimeout > 0) {
            startMarqueeHandler();
        }
        if (isTv() && urlTimeout > 0) {
            startUrlHandler();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            unregisterReceiver(connectionReceiver);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
        if (isTablet() && marquee && marqueeTimeout > 0) {
            stopMarqueeHandler();
        }
        if (isTv() && urlTimeout > 0) {
            stopUrlHandler();
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (isTv()) {
            if (event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_MENU) {
                kioskWeb.showContextMenu();
                return true;
            } else if (event.getAction() == KeyEvent.ACTION_DOWN && (event.getKeyCode() == 19)){
                upPressCount++;
                if (upPressCount == 3) {
                    rightPressCount = 0;
                }
                return true;
            } else if (event.getAction() == KeyEvent.ACTION_DOWN && (event.getKeyCode() == 20 && upPressCount >= 3)){
                rightPressCount++;
                if (rightPressCount == 3) {
                    appClick(tvUri);
                    upPressCount = 0;
                    rightPressCount = 0;
                }
                return true;
            } else if (event.getAction() == KeyEvent.ACTION_DOWN && (event.getKeyCode() == 21 && upPressCount >= 3)){
                rightPressCount++;
                if (rightPressCount == 3) {
                    openSettingsActivity();
                    upPressCount = 0;
                    rightPressCount = 0;
                }
                return true;
            } else if (event.getAction() == KeyEvent.ACTION_DOWN && (event.getKeyCode() == 22 && upPressCount >= 3)){
                rightPressCount++;
                if (rightPressCount == 3) {
                    openSettings();
                    upPressCount = 0;
                    rightPressCount = 0;
                }
                return true;
            } else if (event.getAction() == KeyEvent.ACTION_DOWN && (event.getKeyCode() == 23 || event.getKeyCode() == 23)){
                recreate();
                return true;
            } else if (event.getAction() == KeyEvent.ACTION_DOWN && (event.getKeyCode() == 82 || event.getKeyCode() == 4)){
                toggleUrl();
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

        // Create the 5th item with centered and smaller text
        String ipText = String.format("IP: %s", localIp);
        SpannableString smallerCenteredText = new SpannableString(ipText);
        // Center the text
        smallerCenteredText.setSpan(new AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER), 0, ipText.length(), 0);
        // Make the text half the size
        smallerCenteredText.setSpan(new RelativeSizeSpan(0.5f), 0, ipText.length(), 0);

        menu.add(0, 5, 0, smallerCenteredText);
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

        if (isTablet() && marquee && marqueeTimeout > 0) {
            if (isMarqueeRunning()) {
                stopMarqueeHandler();
                marqueeVisible = false;
                kioskWeb.loadUrl(previousUrl);
                startMarqueeHandler();
            }
        }

        if (nextUrl.equals(clientUrl1) && urlTimeout > 0) {
            stopUrlHandler();
            startUrlHandler();
        }
    }

    /**
     * Helper method to check if the marquee is currently running.
     */
    private boolean isMarqueeRunning() {
        return marqueeVisible;
    }

    /**
     * Restores the WebView to the content it had before the marquee started.
     */
    private void restorePreviousContent() {
        if (previousUrl != null && !previousUrl.isEmpty()) {
            kioskWeb.loadUrl(previousUrl);
        }
    }

    public void startUrlHandler() {
        urlHandler.postDelayed(urlRunnable, urlTimeout);
    }

    private void stopUrlHandler() { urlHandler.removeCallbacks(urlRunnable); }

    private void startMarqueeHandler() {
        marqueeRunnable = () -> {
            String htmlContent = generateMarqueeHtml(marqueeText, marqueeSpeed, marqueeBgColor);
            loadHtmlContent(htmlContent);
            marqueeVisible = true;
        };
        marqueeHandler.postDelayed(marqueeRunnable, marqueeTimeout);
    }
    private void stopMarqueeHandler() {
        marqueeHandler.removeCallbacks(marqueeRunnable);
    }

    @SuppressLint("StaticFieldLeak")
    public void Update(final String apkUrl1, @Nullable final String apkUrl2) {
        new AsyncTask<Void, String, String>() {
            String result = "";

            @Override
            protected String doInBackground(Void... params) {
                try {
                    // Download and install the first APK
                    downloadAndInstallAPK(apkUrl1);

                    // Download and install the second APK if provided
                    if (apkUrl2 != null) {
                        downloadAndInstallAPK(apkUrl2);
                    }

                } catch (IOException e) {
                    result = "Update error! " + e.getMessage();
                    e.printStackTrace();
                }
                return result;
            }

            @Override
            protected void onPostExecute(String result) {
                if (!result.isEmpty()) {
                    Toast.makeText(getApplicationContext(), result, Toast.LENGTH_LONG).show();
                }
            }
        }.execute();
    }

    // Helper method to download and install APK
    private void downloadAndInstallAPK(String apkUrl) throws IOException {
        URL url = new URL(apkUrl);
        HttpURLConnection c = (HttpURLConnection) url.openConnection();
        c.setRequestMethod("GET");
        c.connect();

        String PATH = Environment.getExternalStorageDirectory() + "/download/";
        File file = new File(PATH);
        file.mkdirs();
        File outputFile = new File(file, apkUrl.substring(apkUrl.lastIndexOf('/') + 1)); // Get the file name from the URL
        FileOutputStream fos = new FileOutputStream(outputFile);

        InputStream is = c.getInputStream();
        byte[] buffer = new byte[1024];
        int len1;
        while ((len1 = is.read(buffer)) != -1) {
            fos.write(buffer, 0, len1);
        }
        fos.close();
        is.close();

        Intent intent;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Uri apkUri = FileProvider.getUriForFile(getApplicationContext(),
                    getApplicationContext().getPackageName() + ".provider", outputFile);
            intent = new Intent(Intent.ACTION_INSTALL_PACKAGE);
            intent.setData(apkUri);
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } else {
            intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.fromFile(outputFile), "application/vnd.android.package-archive");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }

        startActivity(intent);
    }

    public void checkUpdate() {

        String updateFound=(String.format(getString(R.string.UpdateAvailable), getString(R.string.app_name)));
        UpdateWrapper updateWrapper = new UpdateWrapper.Builder(MainActivity.this)
                .setTime(3000)
                .setNotificationIcon(R.mipmap.ic_launcher)
                .setUpdateTitle(updateFound)
                .setUpdateContentText(getString(R.string.UpdateDescription))
                .setUrl(BuildConfig.UPDATE_URL)
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
    @Override
    protected void onSaveInstanceState(Bundle outState )
    {
        super.onSaveInstanceState(outState);
        kioskWeb.saveState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState)
    {
        super.onRestoreInstanceState(savedInstanceState);
        kioskWeb.restoreState(savedInstanceState);
    }

    private String generateMarqueeHtml(String text, int speed, int bgColor) {
        String сolorString = String.format("%X", marqueeTxColor).substring(2);

        return "<html><head><style>" +
                "body { display: flex; align-items: center; justify-content: center; height: 100vh; margin: 0; }" +
                "marquee { font-size: 20vh; white-space: nowrap; " +
                "color: " + сolorString + ";}" +
                "</style></head><body bgcolor=\"" + bgColor + "\">" +
                "<marquee id='marqueeText' behavior=\"scroll\" direction=\"left\" scrollamount=\"" + speed + "\">" + text + "</marquee>" +
                "<script>" +
                "var marquee = document.getElementById('marqueeText');" +
                "</script>" +
                "</body></html>";
    }

    private void loadHtmlContent(String htmlContent) {
        kioskWeb.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null);
    }
    private void openStorageManager(Context context) {
        Intent intent = new Intent();
        intent.setAction(android.provider.Settings.ACTION_INTERNAL_STORAGE_SETTINGS);
        context.startActivity(intent);
    }
    private void onAlertDialog(Context context, String message, String toastMsg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Warning!");
        builder.setMessage(message);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Handle OK button click
            }
        });
        builder.show();
    }
    public static String getLocalIpAddress(Context context) {
        try {
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connectivityManager != null) {
                Network[] allNetworks = connectivityManager.getAllNetworks();
                for (Network network : allNetworks) {
                    NetworkCapabilities networkCapabilities = connectivityManager.getNetworkCapabilities(network);
                    if (networkCapabilities != null && networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) {
                        LinkProperties linkProperties = connectivityManager.getLinkProperties(network);
                        if (linkProperties != null) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) { // Android 7 (Nougat) or higher
                                for (LinkAddress linkAddress : linkProperties.getLinkAddresses()) {
                                    InetAddress address = linkAddress.getAddress();
                                    // Check if it's a valid local IP address (IPv4 or IPv6)
                                    if (!address.isLoopbackAddress() && (address instanceof Inet4Address)) {
                                        return address.getHostAddress();
                                    }
                                }
                            }                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null; // Return null if no IP address was found
    }
    private void enableImmersiveMode() {
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
    }

    private void openSettingsActivity() {
        try {
            @SuppressLint({"NewApi", "LocalSuppress"}) Intent startSettingsActivityIntent = new Intent(getApplicationContext(), SettingsActivity.class);
            startActivity(startSettingsActivityIntent);
        } catch (Exception e) {
            Log.e(TAG, "Failed to open settingsActivity", e);
        }
    }
    private void openSettings() {
        try {
            Intent intent = new Intent(Settings.ACTION_SETTINGS);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "Failed to open settings", e);
        }
    }
}

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
// AsyncTask entfernt - ersetzt durch ExecutorService
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

    // NEUE FELDER: Flag und Handler für automatisches Wiederstarten nach Benutzer-Pause
    private boolean marqueeUserPaused = false;
    private Handler marqueeResumeHandler;

    public SharedPreferences sharedPreferences;
    public static String tvUri = "com.teamviewer.quicksupport.market";
    public static String adUri = "com.anydesk.anydeskandroid";
    public String adbUri = "com.cgutman.androidremotedebugger";

    private DevicePolicyManager mDevicePolicyManager;
    private ComponentName mComponentName;

    private void initKioskMode() {
        mDevicePolicyManager = (DevicePolicyManager) getSystemService(DEVICE_POLICY_SERVICE);
        mComponentName = new ComponentName(this, KioskAdminReceiver.class);
        if (mDevicePolicyManager.isDeviceOwnerApp(getPackageName())) {
            mDevicePolicyManager.setLockTaskPackages(
                    mComponentName, new String[]{
                            getPackageName(),
                            "com.android.settings",
                            "com.teamviewer.quicksupport.market",
                            "com.teamviewer.quicksupport.addon.universal"
                    }
            );
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                mDevicePolicyManager.setLockTaskFeatures(
                        mComponentName, DevicePolicyManager.LOCK_TASK_FEATURE_NONE
                );
            }
            startLockTask();
        }
    }

    private int upPressCount = 0;
    private int rightPressCount = 0;
    private long lastKeyPressTime = 0;
    private static final long KEY_SEQUENCE_TIMEOUT = 2000; // 2 seconds timeout
    private static final String TAG = "KeyEventDebug";



    BroadcastReceiver connectionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null && Objects.equals(intent.getAction(), ConnectivityManager.CONNECTIVITY_ACTION)) {

                // Sicherer Zugriff auf NetworkInfo (vermeidet NPE)
                android.net.NetworkInfo networkInfo = ConnectivityManagerCompat.getNetworkInfoFromBroadcast(
                        (ConnectivityManager) context.getSystemService(CONNECTIVITY_SERVICE),
                        intent);
                boolean isConnected = networkInfo != null && networkInfo.isConnected();

                if (isConnected) {
                    commitURL(urlPreset + clientUrl1);
                } else {
                    String noNet = context.getString(R.string.noNetwork);
                    String rawHTML = "<HTML>"+ "<body><table width=\"100%\" height=\"100%\"><td height=\"30%\"></td><tr><td height=\"40%\" align=\"center\" valign=\"middle\"><h1>" + noNet +"</h1></td><tr><td height=\"30%\"></td></table></body>"+ "</HTML>";
                    if (kioskWeb != null) kioskWeb.loadData(rawHTML, "text/HTML", "UTF-8");
                }
            }
        }
    };

    // Android 11+: NetworkCallback
    private android.net.ConnectivityManager.NetworkCallback networkCallback;

    private void registerNetworkCallback() {
        android.net.ConnectivityManager cm =
                (android.net.ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        networkCallback = new android.net.ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(android.net.Network network) {
                runOnUiThread(() -> commitURL(urlPreset + clientUrl1));
            }
            @Override
            public void onLost(android.net.Network network) {
                runOnUiThread(() -> {
                    String noNet = getString(R.string.noNetwork);
                    String rawHTML = "<HTML><body><table width=\"100%\" height=\"100%\"><td height=\"30%\"></td><tr><td height=\"40%\" align=\"center\" valign=\"middle\"><h1>" + noNet + "</h1></td></table></body></HTML>";
                    if (kioskWeb != null) kioskWeb.loadData(rawHTML, "text/HTML", "UTF-8");
                });
            }
        };
        android.net.NetworkRequest request = new android.net.NetworkRequest.Builder()
                .addCapability(android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build();
        cm.registerNetworkCallback(request, networkCallback);
    }

    private void unregisterNetworkCallback() {
        if (networkCallback != null) {
            android.net.ConnectivityManager cm =
                    (android.net.ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
            cm.unregisterNetworkCallback(networkCallback);
            networkCallback = null;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @SuppressLint({"ApplySharedPref", "HardwareIds"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // setContentView und FLAG_KEEP_SCREEN_ON wurden an eine zentrale Stelle verschoben,
        // um doppelte Initialisierung zu vermeiden
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

        // zentrale einmalige setContentView / View-Init
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        kioskWeb = findViewById(R.id.kioskView);
        if (savedInstanceState != null)
            ((WebView)findViewById(R.id.kioskView)).restoreState(savedInstanceState);
        // urlHandler/urlRunnable bereits oben gesetzt für TV/Timeout-Fall

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
        initKioskMode();

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
                    else if (PwInput.equals(PW1)) {
                        openSettingsActivity();
                    }
                    else if (PwInput.equals(PW2)) {
                        openSettings();
                    }
                    else if (PwInput.equals(PW3)) {
                        openStorageManager(this);
                    }
                    else if (PwInput.equals(PW4)) {
                        if (checkApps(this, tvUri))
                        {
                            appClick(tvUri);
                        } else {
                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + tvUri));
                            startActivity(intent);
                        }
                    }
                })
                .setNegativeButton("Cancel", (dialog, id) -> dialog.cancel())
                .setNeutralButton("Autologin", (dialog, id) -> commitURL(urlPreset + clientUrl1));
        checkPasswordDialog.show();
    }

    private void toggleUrl() {
        if (previousUrl == null || previousUrl.isEmpty()) {
            previousUrl = urlPreset + clientUrl1;
        }
        String currentUrl = kioskWeb.getUrl();
        commitURL(nextUrl.isEmpty() ? (urlPreset + clientUrl1) : nextUrl);
        previousUrl = currentUrl;
        nextUrl = clientUrl3.isEmpty() ? clientUrl1 : clientUrl3;
    }

    private void toggleSettingsButton() {
        ImageButton settingsButton = findViewById(R.id.settingsButton);
        if (settingsButton.getVisibility() == View.VISIBLE) {
            settingsButton.setVisibility(View.GONE);
        } else {
            settingsButton.setVisibility(View.VISIBLE);
        }
    }

    private void setupSettings() {
        ImageButton settingsButton = findViewById(R.id.settingsButton);
        settingsButton.setOnClickListener(view -> checkPassword(getString(R.string.code_or_help)));
        registerForContextMenu(findViewById(R.id.kioskView));
        kioskWeb.setWebChromeClient(new WebChromeClient() {
        });
        String rawHTML = "<HTML>" + "<body><table width=\"100%\" height=\"100%\"><td height=\"30%\"></td><tr><td height=\"40%\" align=\"center\" valign=\"middle\"><h1>" + getString(R.string.noNetwork) + "</h1></td><tr><td height=\"30%\"></td></table></body>" + "</HTML>";
        kioskWeb.loadData(rawHTML, "text/HTML", "UTF-8");
        kioskWeb.setWebViewClient(new WebViewClient() {
            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                if (kioskWeb != null) {
                    String rawHTML = "<HTML><body><table width=\"100%\" height=\"100%\"><td height=\"30%\"></td><tr><td height=\"40%\" align=\"center\" valign=\"middle\"><h1>" + description + "</h1></td></table></body></HTML>";
                    kioskWeb.loadData(rawHTML, "text/HTML", "UTF-8");
                    android.app.AlertDialog alertDialog = new android.app.AlertDialog.Builder(MainActivity.this).create();
                    alertDialog.setTitle(getString(R.string.error));
                    alertDialog.setMessage(description);
                    alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.try_again), (dialog, which) -> commitURL(urlPreset + clientUrl1));
                    alertDialog.show();
                }
            }
        });
        kioskWeb.clearCache(true);
        kioskWeb.clearHistory();
        kioskWeb.getSettings().setJavaScriptEnabled(true);
        kioskWeb.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        kioskWeb.getSettings().setDomStorageEnabled(true);
        kioskWeb.getSettings().setDatabaseEnabled(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            kioskWeb.getSettings().setAllowUniversalAccessFromFileURLs(true);
            kioskWeb.getSettings().setAllowFileAccessFromFileURLs(true);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            kioskWeb.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            kioskWeb.getSettings().setMediaPlaybackRequiresUserGesture(false);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            kioskWeb.getSettings().setGeolocationDatabasePath(getFilesDir().getPath());
        }
        kioskWeb.getSettings().setGeolocationEnabled(true);
        // setAppCacheEnabled und setAppCachePath sind deprecated seit API 21
        // Verwende stattdessen standard WebView cache
        kioskWeb.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        kioskWeb.getSettings().setUserAgentString("Mozilla/5.0 (Linux; Android 12; SM-T870)");
        kioskWeb.getSettings().setTextZoom(zoom * 20); // zoom ist 0-10, TextZoom ist 50-200
        kioskWeb.getSettings().setUseWideViewPort(true);
        kioskWeb.getSettings().setLoadWithOverviewMode(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            kioskWeb.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE);
        }
        kioskWeb.setDownloadListener((url, userAgent, contentDisposition, mimetype, contentLength) ->
        {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            startActivity(intent);
        });
        if (checkAutoLogin && !autoName.isEmpty() && !autoPassWord.isEmpty()) {
            String javaScript = "javascript: (function() {" +
                    "var inputs = document.getElementsByTagName('input');" +
                    "for (var i = 0; i < inputs.length; i++) {" +
                    "if (inputs[i].type == 'text' || inputs[i].type == 'email') { inputs[i].value='" + autoName.replaceAll("'", "\'") + "'; }" +
                    "if (inputs[i].type == 'password') { inputs[i].value='" + autoPassWord.replaceAll("'", "\'") + "'; }" +
                    "}" +
                    "})();";
            kioskWeb.loadUrl(javaScript);
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    public void commitURL(String url) {
        try {
            String trimmedUrl = url.trim();
            if (!trimmedUrl.startsWith("http://") && !trimmedUrl.startsWith("https://") && !trimmedUrl.startsWith("file://")) {
                trimmedUrl = "https://" + trimmedUrl;
            }
            previousUrl = kioskWeb.getUrl();
            kioskWeb.loadUrl(trimmedUrl);
        } catch (Exception e) {
            Log.e(TAG, "Error loading URL: " + url, e);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (kioskWeb != null) {
            kioskWeb.onResume();
        }
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
        registerNetworkCallback();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (kioskWeb != null) {
            kioskWeb.onPause();
        }
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
        unregisterNetworkCallback();
    }

    @Override
    protected void onDestroy() {
        stopUrlHandler();
        stopMarqueeHandler();
        stopMarqueeResumeHandler();
        unregisterNetworkCallback();
        if (sharedPreferences != null) {
            sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
        }
        super.onDestroy();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        // Handle shared preference changes
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                if (upPressCount == 0) {
                    lastKeyPressTime = System.currentTimeMillis();
                }
                upPressCount++;
                if (upPressCount >= 3 && (System.currentTimeMillis() - lastKeyPressTime) < KEY_SEQUENCE_TIMEOUT) {
                    resetKeyPressCounter();
                    return true;
                }
            }
        } else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                if (rightPressCount == 0) {
                    lastKeyPressTime = System.currentTimeMillis();
                }
                rightPressCount++;
                if (rightPressCount >= 3 && (System.currentTimeMillis() - lastKeyPressTime) < KEY_SEQUENCE_TIMEOUT) {
                    resetKeyPressCounter();
                    return true;
                }
            }
        } else if (keyCode == toggleKey) {
            return true;
        } else {
            resetKeyPressCounter();
        }
        return super.onKeyDown(keyCode, event);
    }

    private void resetKeyPressCounter() {
        upPressCount = 0;
        rightPressCount = 0;
        lastKeyPressTime = 0;
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
                // Stoppe Marquee bei Benutzerinteraktion und stelle vorherigen Inhalt wieder her.
                // Marquee wird nicht dauerhaft deaktiviert, sondern pausiert und dann automatisch neu gestartet.
                stopMarqueeHandler();
                marqueeVisible = false;
                marqueeUserPaused = true;
                restorePreviousContent();
                scheduleMarqueeResume();
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
        if (urlHandler != null && urlRunnable != null && urlTimeout > 0) {
            urlHandler.postDelayed(urlRunnable, urlTimeout);
        }
    }

    private void stopUrlHandler() {
        if (urlHandler != null && urlRunnable != null) {
            urlHandler.removeCallbacks(urlRunnable);
        }
    }

    private void startMarqueeHandler() {
        // Wenn der Benutzer das Marquee pausiert hat, nicht sofort neu starten
        if (marqueeUserPaused) return;

        if (marqueeHandler == null) marqueeHandler = new Handler();
        if (marqueeRunnable == null) {
            marqueeRunnable = () -> {
                String htmlContent = generateMarqueeHtml(marqueeText, marqueeSpeed, marqueeBgColor);
                loadHtmlContent(htmlContent);
                marqueeVisible = true;
            };
        }
        if (marqueeTimeout > 0) marqueeHandler.postDelayed(marqueeRunnable, marqueeTimeout);
    }
    private void stopMarqueeHandler() {
        if (marqueeHandler != null && marqueeRunnable != null) {
            marqueeHandler.removeCallbacks(marqueeRunnable);
        }
    }

    // NEUE METHODEN: Resume-Planung und Aufräumen
    private void scheduleMarqueeResume() {
        if (marqueeTimeout <= 0) return;
        if (marqueeResumeHandler == null) marqueeResumeHandler = new Handler();
        // Entferne vorherige Resume-Aufrufe und plane einen neuen Restart nach marqueeTimeout
        marqueeResumeHandler.removeCallbacksAndMessages(null);
        marqueeResumeHandler.postDelayed(() -> {
            marqueeUserPaused = false;
            startMarqueeHandler();
        }, marqueeTimeout);
    }

    private void stopMarqueeResumeHandler() {
        if (marqueeResumeHandler != null) {
            marqueeResumeHandler.removeCallbacksAndMessages(null);
        }
    }

    public void Update(final String apkUrl1, @Nullable final String apkUrl2) {
        java.util.concurrent.ExecutorService executor =
                java.util.concurrent.Executors.newSingleThreadExecutor();
        android.os.Handler handler = new android.os.Handler(android.os.Looper.getMainLooper());
        executor.execute(() -> {
            String result = "";
            try {
                downloadAndInstallAPK(apkUrl1);
                if (apkUrl2 != null) {
                    downloadAndInstallAPK(apkUrl2);
                }
            } catch (IOException e) {
                result = "Update error! " + e.getMessage();
                e.printStackTrace();
            }
            final String finalResult = result;
            handler.post(() -> {
                if (!finalResult.isEmpty()) {
                    Toast.makeText(getApplicationContext(), finalResult, Toast.LENGTH_LONG).show();
                }
            });
        });
    }

    // APK herunterladen und silent installieren (kein Dialog, kein LockTask-Problem)
    private void downloadAndInstallAPK(String apkUrl) throws IOException {
        URL url = new URL(apkUrl);
        HttpURLConnection c = (HttpURLConnection) url.openConnection();
        c.setRequestMethod("GET");
        c.setInstanceFollowRedirects(true);
        c.setRequestProperty("User-Agent", "Mozilla/5.0");
        c.connect();

        File outputFile = new File(getExternalFilesDir(null),
                apkUrl.substring(apkUrl.lastIndexOf('/') + 1));
        FileOutputStream fos = new FileOutputStream(outputFile);
        InputStream is = c.getInputStream();
        byte[] buffer = new byte[4096];
        int len1;
        while ((len1 = is.read(buffer)) != -1) {
            fos.write(buffer, 0, len1);
        }
        fos.close();
        is.close();

        silentInstallApk(outputFile);
    }

    // Silent Install via PackageInstaller - kein Dialog, funktioniert im LockTask-Modus
    private void silentInstallApk(File apkFile) {
        android.content.pm.PackageInstaller packageInstaller =
                getPackageManager().getPackageInstaller();
        android.content.pm.PackageInstaller.SessionParams params =
                new android.content.pm.PackageInstaller.SessionParams(
                        android.content.pm.PackageInstaller.SessionParams.MODE_FULL_INSTALL);
        try {
            int sessionId = packageInstaller.createSession(params);
            android.content.pm.PackageInstaller.Session session =
                    packageInstaller.openSession(sessionId);

            try (InputStream in = new java.io.FileInputStream(apkFile);
                 java.io.OutputStream out = session.openWrite("package", 0, apkFile.length())) {
                byte[] buffer = new byte[4096];
                int len;
                while ((len = in.read(buffer)) != -1) {
                    out.write(buffer, 0, len);
                }
                session.fsync(out);
            }

            android.app.PendingIntent intent = android.app.PendingIntent.getBroadcast(
                    this, sessionId,
                    new Intent("com.nass.ek.w3kiosk.INSTALL_COMPLETE"),
                    android.app.PendingIntent.FLAG_UPDATE_CURRENT |
                            (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ?
                                    android.app.PendingIntent.FLAG_MUTABLE : 0)
            );
            session.commit(intent.getIntentSender());
            session.close();

            runOnUiThread(() ->
                    Toast.makeText(getApplicationContext(),
                            "Update wird installiert...", Toast.LENGTH_SHORT).show()
            );
        } catch (Exception e) {
            e.printStackTrace();
            runOnUiThread(() ->
                    Toast.makeText(getApplicationContext(),
                            "Install failed: " + e.getMessage(), Toast.LENGTH_LONG).show()
            );
        }
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
        // Korrekte Benennung und Formatierung der Farben als Hex
        String colorString = String.format("#%06X", (0xFFFFFF & marqueeTxColor));
        String bgString = String.format("#%06X", (0xFFFFFF & bgColor));

        return "<html><head><style>" +
                "body { display: flex; align-items: center; justify-content: center; height: 100vh; margin: 0; background:" + bgString + "; }" +
                "marquee { font-size: 20vh; white-space: nowrap; color: " + colorString + ";}" +
                "</style></head><body>" +
                "<marquee id='marqueeText' behavior=\"scroll\" direction=\"left\" scrollamount=\"" + speed + "\">" + text + "</marquee>" +
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

    /**
     * ========== ANDROID 13 FIX ==========
     *
     * WICHTIG: Diese Methode wird GANZ AM ANFANG in onCreate() aufgerufen!
     * Das ist KORREKT, weil enableImmersiveMode() VOR setContentView() aufgerufen werden MUSS.
     *
     * Das ursprüngliche Problem war, dass getWindow().getInsetsController() manchmal null zurückgibt
     * auf Android 13, aber das ist OK - wir haben einen Fallback!
     *
     * Die Lösung:
     * 1. Null-Check für getInsetsController()
     * 2. Fallback zu älteren Methode wenn null
     * 3. Try-Catch für zusätzliche Sicherheit
     * 4. Keine Exceptions! ✅
     */
    @SuppressWarnings("deprecation")
    private void enableImmersiveMode() {
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                // Für Android 11+ (R)
                try {
                    getWindow().setDecorFitsSystemWindows(false);

                    // KRITISCH: Null-Check!
                    android.view.WindowInsetsController controller = getWindow().getInsetsController();

                    if (controller != null) {
                        // Nur wenn controller nicht null ist
                        controller.hide(android.view.WindowInsets.Type.statusBars()
                                | android.view.WindowInsets.Type.navigationBars());
                        controller.setSystemBarsBehavior(
                                android.view.WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
                    } else {
                        // Fallback wenn controller null ist
                        useDeprecatedImmersiveMode();
                    }
                } catch (Exception e) {
                    Log.w(TAG, "WindowInsetsController failed, using fallback: " + e.getMessage());
                    useDeprecatedImmersiveMode();
                }
            } else {
                // Für Android < 11
                useDeprecatedImmersiveMode();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in enableImmersiveMode: " + e.getMessage());
        }
    }

    /**
     * Fallback-Methode mit älteren API (funktioniert auf allen Versionen)
     */
    @SuppressWarnings("deprecation")
    private void useDeprecatedImmersiveMode() {
        try {
            final int flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;

            final View decorView = getWindow().getDecorView();
            if (decorView != null) {
                decorView.setSystemUiVisibility(flags);
                decorView.setOnSystemUiVisibilityChangeListener(visibility -> {
                    if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                        decorView.setSystemUiVisibility(flags);
                    }
                });
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in useDeprecatedImmersiveMode: " + e.getMessage());
        }
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

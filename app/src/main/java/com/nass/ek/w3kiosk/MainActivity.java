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
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.media.session.MediaSessionCompat;
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

    private boolean marqueeUserPaused = false;
    private Handler marqueeResumeHandler;

    public SharedPreferences sharedPreferences;
    public static String tvUri = "com.teamviewer.quicksupport.market";
    public static String adUri = "com.anydesk.anydeskandroid";
    public String adbUri = "com.cgutman.androidremotedebugger";

    private DevicePolicyManager mDevicePolicyManager;
    private ComponentName mComponentName;

    // Google TV Streamer Scancodes
    private static final int SCANCODE_YOUTUBE     = 0x000c0077;
    private static final int SCANCODE_NETFLIX      = 0x000c0078;
    private static final int SCANCODE_SPREADSHEET  = 0x000c0186;

    // NEU: MediaSession und MediaButton-Receiver
    private MediaSessionCompat mediaSession;
    private BroadcastReceiver mediaButtonReceiver;

    private int upPressCount = 0;
    private int rightPressCount = 0;
    private static final String TAG = "KeyEventDebug";

    // -------------------------------------------------------------------------
    // Netzwerk
    // -------------------------------------------------------------------------

    BroadcastReceiver connectionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null && Objects.equals(intent.getAction(), ConnectivityManager.CONNECTIVITY_ACTION)) {
                android.net.NetworkInfo networkInfo = ConnectivityManagerCompat.getNetworkInfoFromBroadcast(
                        (ConnectivityManager) context.getSystemService(CONNECTIVITY_SERVICE), intent);
                boolean isConnected = networkInfo != null && networkInfo.isConnected();
                if (isConnected) {
                    commitURL(urlPreset + clientUrl1);
                } else {
                    String noNet = context.getString(R.string.noNetwork);
                    String rawHTML = "<HTML><body><table width=\"100%\" height=\"100%\"><td height=\"30%\"></td><tr><td height=\"40%\" align=\"center\" valign=\"middle\"><h1>" + noNet + "</h1></td><tr><td height=\"30%\"></td></table></body></HTML>";
                    if (kioskWeb != null) kioskWeb.loadData(rawHTML, "text/HTML", "UTF-8");
                }
            }
        }
    };

    private android.net.ConnectivityManager.NetworkCallback networkCallback;

    private void registerNetworkCallback() {
        android.net.ConnectivityManager cm = (android.net.ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        networkCallback = new android.net.ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(Network network) {
                runOnUiThread(() -> commitURL(urlPreset + clientUrl1));
            }
            @Override
            public void onLost(Network network) {
                runOnUiThread(() -> {
                    String noNet = getString(R.string.noNetwork);
                    String rawHTML = "<HTML><body><table width=\"100%\" height=\"100%\"><td height=\"30%\"></td><tr><td height=\"40%\" align=\"center\" valign=\"middle\"><h1>" + noNet + "</h1></td></table></body></HTML>";
                    if (kioskWeb != null) kioskWeb.loadData(rawHTML, "text/HTML", "UTF-8");
                });
            }
        };
        android.net.NetworkRequest request = new android.net.NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET).build();
        cm.registerNetworkCallback(request, networkCallback);
    }

    private void unregisterNetworkCallback() {
        if (networkCallback != null) {
            android.net.ConnectivityManager cm = (android.net.ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
            cm.unregisterNetworkCallback(networkCallback);
            networkCallback = null;
        }
    }

    // -------------------------------------------------------------------------
    // Kiosk / Device Owner
    // -------------------------------------------------------------------------

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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                mDevicePolicyManager.setLockTaskFeatures(
                        mComponentName, DevicePolicyManager.LOCK_TASK_FEATURE_NONE);
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                mDevicePolicyManager.setKeyguardDisabledFeatures(
                        mComponentName, DevicePolicyManager.KEYGUARD_DISABLE_FEATURES_ALL);
            }
            startLockTask();
        }
    }

    // -------------------------------------------------------------------------
    // MediaSession – fängt Medientasten ab bevor Google TV sie verarbeitet
    // -------------------------------------------------------------------------

    private void initMediaSession() {
        mediaSession = new MediaSessionCompat(this, "KioskSession");
        mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS
                | MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        mediaSession.setCallback(new MediaSessionCompat.Callback() {
            @Override
            public boolean onMediaButtonEvent(Intent intent) {
                KeyEvent event = intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
                if (event != null && event.getAction() == KeyEvent.ACTION_DOWN) {
                    handleMediaKey(event.getKeyCode(), event.getScanCode());
                }
                return true; // true = System soll nichts weiter tun
            }
        });
        mediaSession.setActive(true);
    }

    private void releaseMediaSession() {
        if (mediaSession != null) {
            mediaSession.setActive(false);
            mediaSession.release();
            mediaSession = null;
        }
    }

    // -------------------------------------------------------------------------
    // MediaButton BroadcastReceiver – Fallback falls MediaSession nicht greift
    // -------------------------------------------------------------------------

    private void registerMediaButtonReceiver() {
        mediaButtonReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int keyCode  = intent.getIntExtra("keyCode", -1);
                int scanCode = intent.getIntExtra("scanCode", -1);
                handleMediaKey(keyCode, scanCode);
            }
        };
        IntentFilter filter = new IntentFilter("com.nass.ek.w3kiosk.MEDIA_BUTTON");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(mediaButtonReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            registerReceiver(mediaButtonReceiver, filter);
        }
    }

    private void unregisterMediaButtonReceiver() {
        if (mediaButtonReceiver != null) {
            try { unregisterReceiver(mediaButtonReceiver); } catch (Exception ignored) {}
            mediaButtonReceiver = null;
        }
    }

    /**
     * Zentrale Verarbeitung aller abgefangenen Medientasten –
     * wird sowohl von MediaSession als auch vom BroadcastReceiver aufgerufen.
     */
    private void handleMediaKey(int keyCode, int scanCode) {
        if (keyCode == KeyEvent.KEYCODE_MENU ||
                scanCode == SCANCODE_YOUTUBE) {
            if (kioskWeb != null) kioskWeb.showContextMenu();

        } else if (scanCode == SCANCODE_SPREADSHEET) {
            toggleUrl();

        } else if (scanCode == SCANCODE_NETFLIX) {
            rightPressCount++;
            if (rightPressCount >= 3) {
                openSettingsActivity();
                rightPressCount = 0;
            }
        }
    }

    // -------------------------------------------------------------------------
    // onCreate
    // -------------------------------------------------------------------------

    @RequiresApi(api = Build.VERSION_CODES.M)
    @SuppressLint({"ApplySharedPref", "HardwareIds"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        enableImmersiveMode();
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        checkmobileMode  = sharedPreferences.getBoolean("mobileMode", false);
        checkAutoLogin   = sharedPreferences.getBoolean("autoLogin", false);
        autoUpdate       = sharedPreferences.getBoolean("autoUpdate", false);
        localIp          = getLocalIpAddress(this);
        marquee          = sharedPreferences.getBoolean("marquee", false);
        appsCount        = sharedPreferences.getInt("appsCount", 0);
        toSetting        = sharedPreferences.getInt("urlTimeout", 0);

        File marqueeFile = new File("/storage/emulated/0/Pictures/marquee.png");
        if (marqueeFile.exists()) {
            marqueeText = "<img src=\"file:///storage/emulated/0/Pictures/marquee.png\"/>";
        } else {
            marqueeText = "<img src=\"file:///android_res/drawable/logo_splash_web.png\"/>";
        }

        marqueeSpeed    = sharedPreferences.getInt("marqueeSpeed", 25);
        marqueeBgColor  = getResources().getColor(R.color.colorMarquee);
        marqueeTxColor  = getResources().getColor(R.color.colorDarkGray);
        mqtoSetting     = sharedPreferences.getInt("marqueeTimeout", 0);

        int[] marqueeTimeouts = {300000, 600000, 900000, 1200000, 1500000, 1800000, 2100000};
        marqueeTimeout = (mqtoSetting >= 1 && mqtoSetting <= 6)
                ? marqueeTimeouts[mqtoSetting - 1] : 300000;

        urlTimeout = (toSetting > 0) ? toSetting * 30000 : toSetting;

        if (isTablet() && marquee && marqueeTimeout > 0) {
            marqueeHandler  = new Handler();
            marqueeRunnable = () -> {
                String htmlContent = generateMarqueeHtml(marqueeText, marqueeSpeed, marqueeBgColor);
                loadHtmlContent(htmlContent);
                marqueeVisible = true;
            };
            startMarqueeHandler();
        }

        if (isTv(this) && urlTimeout > 0) {
            urlHandler  = new Handler();
            urlRunnable = this::toggleUrl;
            startUrlHandler();
        }

        zoom         = sharedPreferences.getInt("zoomFactor", 5);
        clientUrl1   = sharedPreferences.getString("clientUrl1", "");
        clientUrl2   = sharedPreferences.getString("clientUrl2", "");
        clientUrl3   = sharedPreferences.getString("clientUrl3", "");
        autoName     = sharedPreferences.getString("loginName", "");
        toggleKey    = sharedPreferences.getInt("toggleKey", 82);
        autoPassWord = sharedPreferences.getString("loginPassword", "");
        urlPreset    = getString(R.string.url_preset);

        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        kioskWeb = findViewById(R.id.kioskView);

        // Fokus-Einstellungen für TV
        if (isTv(this)) {
            kioskWeb.setFocusable(true);
            kioskWeb.setFocusableInTouchMode(false);
        } else {
            kioskWeb.setFocusableInTouchMode(true);
        }
        kioskWeb.requestFocus();

        if (savedInstanceState != null)
            ((WebView) findViewById(R.id.kioskView)).restoreState(savedInstanceState);

        if (autoUpdate) checkUpdate();

        if (isTv(this)) {
            new CountDownTimer(60000, 1000) {
                public void onTick(long millisUntilFinished) {}
                public void onFinish() {
                    findViewById(R.id.settingsButton).setVisibility(View.GONE);
                }
            }.start();
        }

        if (!ChecksAndConfigs.checkApps(this, "rkr.simplekeyboard.inputmethod")) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                registerReceiver(new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        DevicePolicyManager dpm = (DevicePolicyManager) getSystemService(DEVICE_POLICY_SERVICE);
                        ComponentName admin = new ComponentName(MainActivity.this, KioskAdminReceiver.class);
                        dpm.setSecureSetting(admin, Settings.Secure.DEFAULT_INPUT_METHOD,
                                "rkr.simplekeyboard.inputmethod/.latin.LatinIME");
                        unregisterReceiver(this);
                    }
                }, new IntentFilter("com.nass.ek.w3kiosk.INSTALL_COMPLETE"), Context.RECEIVER_NOT_EXPORTED);
            }
            Update("https://nass-ek.de/android/simple-keyboard-w3c.apk", null);
        } else {
            DevicePolicyManager dpm = (DevicePolicyManager) getSystemService(DEVICE_POLICY_SERVICE);
            ComponentName admin = new ComponentName(this, KioskAdminReceiver.class);
            dpm.setSecureSetting(admin, Settings.Secure.DEFAULT_INPUT_METHOD,
                    "rkr.simplekeyboard.inputmethod/.latin.LatinIME");
        }

        if (Build.VERSION.SDK_INT >= 26 && !isTv(this)) {
            Intent dialogIntent = new Intent(Settings.ACTION_REQUEST_SET_AUTOFILL_SERVICE);
            dialogIntent.setData(Uri.parse("package:none"));
            if (getSystemService(android.view.autofill.AutofillManager.class).isEnabled()) {
                android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(context);
                builder.setTitle(getString(R.string.disable_autofill))
                        .setMessage(R.string.autofill_text)
                        .setCancelable(false)
                        .setPositiveButton("OK", (dialog, which) -> startActivity(dialogIntent));
                builder.create().show();
            }
        }

        setupSettings();
        initKioskMode();

        // NEU: MediaSession + MediaButton-Receiver starten
        if (isTv(this)) {
            initMediaSession();
        }

        if (ChecksAndConfigs.isScanner()) {
            startActivity(new Intent(getApplicationContext(), ScannerActivity.class));
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

    // -------------------------------------------------------------------------
    // Passwort-Dialog
    // -------------------------------------------------------------------------

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
                    } else if (PwInput.equals("h")) {
                        startActivity(new Intent(getApplicationContext(), SupportActivity.class));
                    } else if (PwInput.equals("i")) {
                        startActivity(new Intent(getApplicationContext(), AboutActivity.class));
                    } else if (PwInput.equals("ad")) {
                        if (checkApps(this, adUri)) appClick(adUri);
                        else startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + adUri)));
                    } else if (PwInput.equals("m")) {
                        String htmlContent = generateMarqueeHtml(marqueeText, marqueeSpeed, marqueeBgColor);
                        loadHtmlContent(htmlContent);
                        marqueeVisible = true;
                    } else if (PwInput.equals("b")) {
                        startActivity(new Intent(Settings.ACTION_BLUETOOTH_SETTINGS));
                    } else if (PwInput.equals("r")) {
                        ShutdownService.rebootDevice(this);
                    } else if (PwInput.equals("s")) {
                        openStorageManager(this);
                    } else if (PwInput.equals("ra") && isTablet()) {
                        if (checkApps(this, adbUri)) appClick(adbUri);
                        else Update("https://nass-ek.de/android/remote-adb-shell.apk", null);
                    } else if (PwInput.equals("sk")) {
                        Update("https://nass-ek.de/android/simple-keyboard-w3c.apk", null);
                    } else if (PwInput.equals("tv")) {
                        if (checkApps(this, tvUri)) {
                            appClick(tvUri);
                        } else {
                            if (isTv(this)) {
                                Update("https://download.teamviewer.com/download/TeamViewerQS.apk",
                                        "https://nass-ek.de/android/Teamviewer/tvaddon_TV.apk");
                            } else if (isTablet()) {
                                Update("https://nass-ek.de/android/Teamviewer/teamviewer-quicksupport.apk",
                                        "https://nass-ek.de/android/Teamviewer/tvaddon_Tablet.apk");
                            } else {
                                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + tvUri)));
                            }
                        }
                    } else if (PwInput.equals(PW1) || PwInput.equals(PW2) || PwInput.equals(PW3) || PwInput.equals(PW4)) {
                        openSettingsActivity();
                    } else if (PwInput.equals("w")) {
                        startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                    } else {
                        dialog.cancel();
                    }
                });
        checkPasswordDialog.setNegativeButton(R.string.cancel, (dialog, id) -> dialog.cancel());
        if (checkAutoLogin && !autoName.isEmpty() && !autoPassWord.isEmpty() && clientUrl2.isEmpty()) {
            checkPasswordDialog.setNeutralButton("Autologin", (dialog, id) -> commitURL(urlPreset + clientUrl1));
        } else if (!clientUrl2.isEmpty()) {
            checkPasswordDialog.setNeutralButton(R.string.toggleUrl, (dialog, id) -> toggleUrl());
        } else if (appsCount > 0) {
            checkPasswordDialog.setNeutralButton(R.string.apps, (dialog, id) -> startActivity(new Intent(this, AppsActivity.class)));
        } else {
            checkPasswordDialog.setNeutralButton(R.string.reboot, (dialog, id) -> ShutdownService.rebootDevice(this));
        }
        AlertDialog dialog = checkPasswordDialog.create();
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setAllCaps(false);
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setAllCaps(false);
        dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setAllCaps(false);
    }

    // -------------------------------------------------------------------------
    // setupSettings
    // -------------------------------------------------------------------------

    @RequiresApi(api = Build.VERSION_CODES.M)
    @SuppressLint("SetJavaScriptEnabled")
    private void setupSettings() {
        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);
        ImageButton settingsButton = findViewById(R.id.settingsButton);
        if (isTv(this)) {
            settingsButton.setOnLongClickListener(v -> { toggleUrl(); return true; });
        } else {
            settingsButton.setOnLongClickListener(v -> { recreate(); return true; });
        }
        settingsButton.setOnClickListener(view -> checkPassword(getString(R.string.code_or_help)));

        kioskWeb.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onPermissionRequest(PermissionRequest request) {
                request.grant(request.getResources());
            }
        });

        marqueeBgColor = getResources().getColor(R.color.colorMarquee);
        String reLoad  = context.getString(R.string.reLoad);
        String rawHTML = "<HTML><body bgcolor=\"" + marqueeBgColor + "\"><table width=\"100%\" height=\"100%\"><td height=\"30%\"></td><tr><td height=\"40%\" align=\"center\" valign=\"middle\"><h1>" + reLoad + "</h1></td><tr><td height=\"30%\"></td></table></body></HTML>";
        kioskWeb.loadData(rawHTML, "text/HTML", "UTF-8");

        kioskWeb.setWebViewClient(new WebViewClient() {
            public void onReceivedError(WebView webView, int errorCode, String description, String failingUrl) {
                try { webView.stopLoading(); } catch (Exception ignored) {}
                String noNet  = context.getString(R.string.noNetwork);
                String rawHTML = "<HTML><body><table width=\"100%\" height=\"100%\"><td height=\"30%\"></td><tr><td height=\"40%\" align=\"center\" valign=\"middle\"><h1>" + noNet + "</h1></td><tr><td height=\"30%\"></td></table></body></HTML>";
                kioskWeb.loadData(rawHTML, "text/HTML", "UTF-8");
                AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
                alertDialog.setTitle(getString(R.string.error));
                alertDialog.setMessage(getString(R.string.check_internet));
                alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.try_again),
                        (dialog, which) -> commitURL(urlPreset + clientUrl1));
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

        if (isTv(this)) {
            kioskWeb.setOverScrollMode(View.OVER_SCROLL_NEVER);
            registerForContextMenu(kioskWeb);
        }

        kioskWeb.setOnTouchListener((v, event) -> {
            if (isTablet() && marquee && marqueeTimeout > 0 && marqueeVisible) {
                stopMarqueeHandler();
                marqueeVisible    = false;
                marqueeUserPaused = true;
                restorePreviousContent();
                scheduleMarqueeResume();
                return true;
            }
            return false;
        });
    }

    // -------------------------------------------------------------------------
    // WebView Hilfsmethoden
    // -------------------------------------------------------------------------

    @SuppressLint("SourceLockedOrientationActivity")
    public void setMobileMode(final boolean enabled) {
        final WebSettings ws = kioskWeb.getSettings();
        final String newUserAgent;
        if (enabled || ChecksAndConfigs.isScanner()) {
            newUserAgent = ws.getUserAgentString().replace("Safari", "Mobile Safari");
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } else {
            newUserAgent = ws.getUserAgentString().replace("Mobile Safari", "Safari");
        }
        ws.setUserAgentString(newUserAgent);
        ws.setUseWideViewPort(enabled);
        ws.setLoadWithOverviewMode(enabled);
        ws.setSupportZoom(enabled);
        ws.setBuiltInZoomControls(enabled);
    }

    private void commitURL(String url) {
        previousUrl = url;
        kioskWeb.getSettings().setTextZoom(75 + (zoom * 5));
        String w3Agent       = getString(R.string.app_name) + " " + BuildConfig.VERSION_CODE;
        String baseUserAgent = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/81.0.4044.66 Safari/537.36";
        kioskWeb.getSettings().setUserAgentString(baseUserAgent + " " + w3Agent);
        if (url.equals(urlPreset)) {
            @SuppressLint({"NewApi", "LocalSuppress"})
            Intent startSettingsActivityIntent = new Intent(getApplicationContext(), SettingsActivity.class);
            startActivity(startSettingsActivityIntent);
        }
        if (ChecksAndConfigs.isNetworkConnected(this)) {
            if (!autoName.isEmpty() && !autoPassWord.isEmpty()) {
                JavaString = "javascript:window.frames[\"Mainpage\"].document.getElementsByName('login')[0].value='" + autoName + "';" +
                        "javascript:window.frames[\"Mainpage\"].document.getElementsByName('pwd')[0].value='" + autoPassWord + "';";
                if (checkAutoLogin) {
                    JavaString += "javascript:window.frames[\"Mainpage\"].document.getElementById('logon').click()";
                }
                kioskWeb.setWebViewClient(new WebViewClient() {
                    public void onPageFinished(WebView view, String url) {
                        CookieSyncManager.getInstance().sync();
                        view.evaluateJavascript(JavaString, s -> {});
                        if (isTv(MainActivity.this)) injectDpadScrollSupport(view);
                    }
                });
            } else if (isTv(this)) {
                kioskWeb.setWebViewClient(new WebViewClient() {
                    public void onPageFinished(WebView view, String url) {
                        injectDpadScrollSupport(view);
                    }
                });
            }
            setWebContentsDebuggingEnabled(true);
            kioskWeb.loadUrl(url);
        } else {
            String noNet  = context.getString(R.string.noNetwork);
            String rawHTML = "<HTML><body><table width=\"100%\" height=\"100%\"><td height=\"30%\"></td><tr><td height=\"40%\" align=\"center\" valign=\"middle\"><h1>" + noNet + "</h1></td><tr><td height=\"30%\"></td></table></body></HTML>";
            kioskWeb.loadData(rawHTML, "text/HTML", "UTF-8");
        }
        hideKeyboard(this);
        findViewById(R.id.settingsButton).bringToFront();
    }

    /**
     * Injiziert JavaScript für D-Pad-Navigation in die geladene Seite.
     * Pfeiltasten scrollen die Seite, falls die Web-App keine eigene Navigation hat.
     */
    private void injectDpadScrollSupport(WebView view) {
        view.evaluateJavascript(
                "(function() {" +
                "  if (window.__dpadInjected) return;" +
                "  window.__dpadInjected = true;" +
                "  document.addEventListener('keydown', function(e) {" +
                "    switch(e.key) {" +
                "      case 'ArrowDown':  window.scrollBy({top:  150, behavior:'smooth'}); break;" +
                "      case 'ArrowUp':    window.scrollBy({top: -150, behavior:'smooth'}); break;" +
                "      case 'ArrowRight': window.scrollBy({left:  150, behavior:'smooth'}); break;" +
                "      case 'ArrowLeft':  window.scrollBy({left: -150, behavior:'smooth'}); break;" +
                "    }" +
                "  });" +
                "})();",
                null
        );
    }

    private void toggleUrl() {
        if (ChecksAndConfigs.isTablet()) TrustAllCertificates.install();

        if (nextUrl.equals(clientUrl3)) {
            commitURL(clientUrl3.startsWith("http") ? clientUrl3 : urlPreset + clientUrl3);
            nextUrl = clientUrl1;
        } else if (nextUrl.equals(clientUrl2)) {
            commitURL(clientUrl2.startsWith("http") ? clientUrl2 : urlPreset + clientUrl2);
            nextUrl = !clientUrl3.equals("") ? clientUrl3 : clientUrl1;
        } else if (nextUrl.equals(clientUrl1)) {
            commitURL(urlPreset + clientUrl1);
            nextUrl = clientUrl2;
        }
        if (isTv(this) && urlTimeout > 0) {
            stopUrlHandler();
            startUrlHandler();
        }
    }

    private void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        View view = activity.getCurrentFocus();
        if (view == null) view = new View(activity);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    // -------------------------------------------------------------------------
    // Lifecycle
    // -------------------------------------------------------------------------

    @Override
    protected void onDestroy() {
        PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(this);
        if (mDevicePolicyManager != null && mDevicePolicyManager.isDeviceOwnerApp(getPackageName())) {
            stopLockTask();
        }
        releaseMediaSession();
        stopMarqueeResumeHandler();
        if (kioskWeb != null) {
            try {
                kioskWeb.stopLoading();
                kioskWeb.clearHistory();
                kioskWeb.removeAllViews();
                kioskWeb.destroy();
            } catch (Exception ignored) {}
            kioskWeb = null;
        }
        super.onDestroy();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        recreate();
    }

    @Override
    public void onBackPressed() {
        if (!isTv(this) && kioskWeb != null && kioskWeb.canGoBack()) {
            kioskWeb.goBack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            enableImmersiveMode();
            if (isTv(this) && kioskWeb != null) kioskWeb.requestFocus();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        upPressCount    = 0;
        rightPressCount = 0;
        enableImmersiveMode();
        if (kioskWeb != null) kioskWeb.requestFocus();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            registerNetworkCallback();
        } else {
            IntentFilter filter = new IntentFilter();
            filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
            registerReceiver(connectionReceiver, filter);
        }
        if (isTablet() && marquee && marqueeTimeout > 0) startMarqueeHandler();
        if (isTv(this) && urlTimeout > 0) startUrlHandler();

        // NEU: MediaButton-Receiver und MediaSession aktivieren
        if (isTv(this)) {
            registerMediaButtonReceiver();
            if (mediaSession != null) mediaSession.setActive(true);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                unregisterNetworkCallback();
            } else {
                unregisterReceiver(connectionReceiver);
            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
        if (isTablet() && marquee && marqueeTimeout > 0) {
            stopMarqueeHandler();
            stopMarqueeResumeHandler();
        }
        if (isTv(this) && urlTimeout > 0) stopUrlHandler();

        // NEU: MediaButton-Receiver und MediaSession deaktivieren
        if (isTv(this)) {
            unregisterMediaButtonReceiver();
            if (mediaSession != null) mediaSession.setActive(false);
        }
    }

    // -------------------------------------------------------------------------
    // Key Event Handling (D-Pad)
    // -------------------------------------------------------------------------

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (isTv(this)) return dispatchKeyEventTv(event);
        return super.dispatchKeyEvent(event);
    }

    /**
     * Key handling für den Google TV Streamer.
     * Medientasten (YouTube, Netflix, Favourites) werden über MediaSession / BroadcastReceiver
     * abgefangen – hier nur noch D-Pad und Back behandeln.
     */
    private boolean dispatchKeyEventTv(KeyEvent event) {
        int keyCode  = event.getKeyCode();
        int scanCode = event.getScanCode();

        // Spezial-Tasten nur bei ACTION_DOWN auslösen
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            if (keyCode == KeyEvent.KEYCODE_MENU) {
                if (kioskWeb != null) kioskWeb.showContextMenu();
                return true;
            }
            if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == 82) {
                toggleUrl();
                return true;
            }
        }
        // ACTION_UP für Spezial-Tasten ebenfalls verschlucken
        if (event.getAction() == KeyEvent.ACTION_UP) {
            if (keyCode == KeyEvent.KEYCODE_MENU ||
                    keyCode == KeyEvent.KEYCODE_BACK ||
                    keyCode == 82) {
                return true;
            }
        }

        // D-Pad und alle anderen Tasten (ACTION_DOWN + ACTION_UP) an WebView
        if (kioskWeb != null) {
            if (!kioskWeb.hasFocus()) kioskWeb.requestFocus();
            return kioskWeb.dispatchKeyEvent(event);
        }
        return super.dispatchKeyEvent(event);
    }

    // -------------------------------------------------------------------------
    // Kontextmenü
    // -------------------------------------------------------------------------

    public void toggleSettingsButton() {
        View buttonView = findViewById(R.id.settingsButton);
        if (buttonView.getVisibility() == View.GONE)
            buttonView.setVisibility(View.VISIBLE);
        else
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
        String ipText = String.format("IP: %s", localIp);
        SpannableString smallerCenteredText = new SpannableString(ipText);
        smallerCenteredText.setSpan(new AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER), 0, ipText.length(), 0);
        smallerCenteredText.setSpan(new RelativeSizeSpan(0.5f), 0, ipText.length(), 0);
        menu.add(0, 5, 0, smallerCenteredText);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if      (item.getItemId() == 1) toggleUrl();
        else if (item.getItemId() == 2) toggleSettingsButton();
        else if (item.getItemId() == 3) checkPassword(getString(R.string.code_or_help));
        else if (item.getItemId() == 4) startActivity(new Intent(getApplicationContext(), SupportActivity.class));
        return true;
    }

    // -------------------------------------------------------------------------
    // Marquee / URL Handler
    // -------------------------------------------------------------------------

    @Override
    public void onUserInteraction() {
        super.onUserInteraction();
        if (isTablet() && marquee && marqueeTimeout > 0 && isMarqueeRunning()) {
            stopMarqueeHandler();
            marqueeVisible    = false;
            marqueeUserPaused = true;
            restorePreviousContent();
            scheduleMarqueeResume();
        }
        if (nextUrl.equals(clientUrl1) && urlTimeout > 0) {
            stopUrlHandler();
            startUrlHandler();
        }
    }

    private boolean isMarqueeRunning() { return marqueeVisible; }

    private void restorePreviousContent() {
        if (previousUrl != null && !previousUrl.isEmpty()) kioskWeb.loadUrl(previousUrl);
    }

    public void startUrlHandler() {
        if (urlHandler != null && urlRunnable != null && urlTimeout > 0)
            urlHandler.postDelayed(urlRunnable, urlTimeout);
    }

    private void stopUrlHandler() {
        if (urlHandler != null && urlRunnable != null)
            urlHandler.removeCallbacks(urlRunnable);
    }

    private void startMarqueeHandler() {
        if (marqueeUserPaused) return;
        if (marqueeHandler  == null) marqueeHandler  = new Handler();
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
        if (marqueeHandler != null && marqueeRunnable != null)
            marqueeHandler.removeCallbacks(marqueeRunnable);
    }

    private void scheduleMarqueeResume() {
        if (marqueeTimeout <= 0) return;
        if (marqueeResumeHandler == null) marqueeResumeHandler = new Handler();
        marqueeResumeHandler.removeCallbacksAndMessages(null);
        marqueeResumeHandler.postDelayed(() -> {
            marqueeUserPaused = false;
            startMarqueeHandler();
        }, marqueeTimeout);
    }

    private void stopMarqueeResumeHandler() {
        if (marqueeResumeHandler != null) marqueeResumeHandler.removeCallbacksAndMessages(null);
    }

    // -------------------------------------------------------------------------
    // Update / Install
    // -------------------------------------------------------------------------

    public void Update(final String apkUrl1, @Nullable final String apkUrl2) {
        java.util.concurrent.ExecutorService executor = java.util.concurrent.Executors.newSingleThreadExecutor();
        android.os.Handler handler = new android.os.Handler(android.os.Looper.getMainLooper());
        executor.execute(() -> {
            String result = "";
            try {
                downloadAndInstallAPK(apkUrl1);
                if (apkUrl2 != null) downloadAndInstallAPK(apkUrl2);
            } catch (IOException e) {
                result = "Update error! " + e.getMessage();
                e.printStackTrace();
            }
            final String finalResult = result;
            handler.post(() -> {
                if (!finalResult.isEmpty())
                    Toast.makeText(getApplicationContext(), finalResult, Toast.LENGTH_LONG).show();
            });
        });
    }

    private void downloadAndInstallAPK(String apkUrl) throws IOException {
        URL url = new URL(apkUrl);
        HttpURLConnection c = (HttpURLConnection) url.openConnection();
        c.setRequestMethod("GET");
        c.setInstanceFollowRedirects(true);
        c.setRequestProperty("User-Agent", "Mozilla/5.0");
        c.connect();
        File outputFile = new File(getExternalFilesDir(null), apkUrl.substring(apkUrl.lastIndexOf('/') + 1));
        FileOutputStream fos = new FileOutputStream(outputFile);
        InputStream is = c.getInputStream();
        byte[] buffer = new byte[4096];
        int len1;
        while ((len1 = is.read(buffer)) != -1) fos.write(buffer, 0, len1);
        fos.close();
        is.close();
        silentInstallApk(outputFile);
    }

    private void silentInstallApk(File apkFile) {
        android.content.pm.PackageInstaller packageInstaller = getPackageManager().getPackageInstaller();
        android.content.pm.PackageInstaller.SessionParams params =
                new android.content.pm.PackageInstaller.SessionParams(
                        android.content.pm.PackageInstaller.SessionParams.MODE_FULL_INSTALL);
        try {
            int sessionId = packageInstaller.createSession(params);
            android.content.pm.PackageInstaller.Session session = packageInstaller.openSession(sessionId);
            try (InputStream in  = new java.io.FileInputStream(apkFile);
                 java.io.OutputStream out = session.openWrite("package", 0, apkFile.length())) {
                byte[] buffer = new byte[4096];
                int len;
                while ((len = in.read(buffer)) != -1) out.write(buffer, 0, len);
                session.fsync(out);
            }
            android.app.PendingIntent intent = android.app.PendingIntent.getBroadcast(
                    this, sessionId,
                    new Intent("com.nass.ek.w3kiosk.INSTALL_COMPLETE"),
                    android.app.PendingIntent.FLAG_UPDATE_CURRENT |
                            (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
                                    ? android.app.PendingIntent.FLAG_MUTABLE : 0));
            session.commit(intent.getIntentSender());
            session.close();
            runOnUiThread(() -> Toast.makeText(getApplicationContext(), "Update wird installiert...", Toast.LENGTH_SHORT).show());
        } catch (Exception e) {
            e.printStackTrace();
            runOnUiThread(() -> Toast.makeText(getApplicationContext(), "Install failed: " + e.getMessage(), Toast.LENGTH_LONG).show());
        }
    }

    public void checkUpdate() {
        String updateFound = String.format(getString(R.string.UpdateAvailable), getString(R.string.app_name));
        new UpdateWrapper.Builder(MainActivity.this)
                .setTime(3000)
                .setNotificationIcon(R.mipmap.ic_launcher)
                .setUpdateTitle(updateFound)
                .setUpdateContentText(getString(R.string.UpdateDescription))
                .setUrl(BuildConfig.UPDATE_URL)
                .setIsShowToast(true)
                .setCallback((model, hasNewVersion) -> {
                    Log.d("Latest Version",    hasNewVersion + "");
                    Log.d("Version Name",       model.getVersionName());
                    Log.d("Version Code",       model.getVersionCode() + "");
                    Log.d("Version Description",model.getContentText());
                    Log.d("Min Support",        model.getMinSupport() + "");
                    Log.d("Download URL",       model.getUrl() + "");
                })
                .build()
                .start();
    }

    public void appClick(String uri) {
        PackageManager manager = getPackageManager();
        try {
            Intent t = manager.getLaunchIntentForPackage(uri);
            if (t == null) throw new PackageManager.NameNotFoundException();
            t.addCategory(Intent.CATEGORY_LAUNCHER);
            t.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(t);
        } catch (PackageManager.NameNotFoundException ignored) {}
    }

    // -------------------------------------------------------------------------
    // State Save/Restore
    // -------------------------------------------------------------------------

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        kioskWeb.saveState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        kioskWeb.restoreState(savedInstanceState);
    }

    // -------------------------------------------------------------------------
    // Hilfsmethoden
    // -------------------------------------------------------------------------

    private String generateMarqueeHtml(String text, int speed, int bgColor) {
        String colorString = String.format("#%06X", (0xFFFFFF & marqueeTxColor));
        String bgString    = String.format("#%06X", (0xFFFFFF & bgColor));
        return "<html><head><style>" +
                "body { display:flex; align-items:center; justify-content:center; height:100vh; margin:0; background:" + bgString + "; }" +
                "marquee { font-size:20vh; white-space:nowrap; color:" + colorString + "; }" +
                "</style></head><body>" +
                "<marquee id='marqueeText' behavior=\"scroll\" direction=\"left\" scrollamount=\"" + speed + "\">" + text + "</marquee>" +
                "</body></html>";
    }

    private void loadHtmlContent(String htmlContent) {
        kioskWeb.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null);
    }

    private void openStorageManager(Context context) {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_INTERNAL_STORAGE_SETTINGS);
        context.startActivity(intent);
    }

    public static String getLocalIpAddress(Context context) {
        try {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm != null) {
                for (Network network : cm.getAllNetworks()) {
                    NetworkCapabilities nc = cm.getNetworkCapabilities(network);
                    if (nc != null && nc.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) {
                        LinkProperties lp = cm.getLinkProperties(network);
                        if (lp != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            for (LinkAddress la : lp.getLinkAddresses()) {
                                InetAddress addr = la.getAddress();
                                if (!addr.isLoopbackAddress() && addr instanceof Inet4Address)
                                    return addr.getHostAddress();
                            }
                        }
                    }
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    @SuppressWarnings("deprecation")
    private void enableImmersiveMode() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                try {
                    getWindow().setDecorFitsSystemWindows(false);
                    android.view.WindowInsetsController controller = getWindow().getInsetsController();
                    if (controller != null) {
                        controller.hide(android.view.WindowInsets.Type.statusBars()
                                | android.view.WindowInsets.Type.navigationBars());
                        controller.setSystemBarsBehavior(
                                android.view.WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
                    } else {
                        useDeprecatedImmersiveMode();
                    }
                } catch (Exception e) {
                    useDeprecatedImmersiveMode();
                }
            } else {
                useDeprecatedImmersiveMode();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in enableImmersiveMode: " + e.getMessage());
        }
    }

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
                    if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0)
                        decorView.setSystemUiVisibility(flags);
                });
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in useDeprecatedImmersiveMode: " + e.getMessage());
        }
    }

    private void openSettingsActivity() {
        try {
            @SuppressLint({"NewApi", "LocalSuppress"})
            Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
            startActivity(intent);
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

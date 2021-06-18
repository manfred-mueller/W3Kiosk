package com.nass.ek.w3kiosk;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.net.ConnectivityManagerCompat;
import androidx.preference.PreferenceManager;

public class MainActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    public static final int RequestPermissionCode = 1;
    public String PASSWORD = "0000";
    public WebView kioskWeb;
    private int currentApiVersion;
    Context context = this;
    public String urlPreset;
    public boolean checkAutofill;
    public boolean mobileMode;
    public String clientUrl;

    String tvUri = "com.teamviewer.quicksupport.market";
    String adUri = "com.anydesk.anydeskandroid";
    String keUri = "com.rscja.scanner";

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public boolean isScanner() {
        return android.os.Build.MODEL.startsWith("C4050") || android.os.Build.MODEL.startsWith("C72") || android.os.Build.MODEL.startsWith("C61");
    }

    BroadcastReceiver connectionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null && intent.getAction().equals(ConnectivityManager
                    .CONNECTIVITY_ACTION)) {

                boolean isConnected = ConnectivityManagerCompat.getNetworkInfoFromBroadcast
                        ((ConnectivityManager) context.getSystemService(CONNECTIVITY_SERVICE),
                                intent).isConnected();

                if (isConnected) {
                    commitURL(urlPreset + clientUrl);
                } else {
                    kioskWeb.loadUrl("about:blank");
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mobileMode = sharedPreferences.getBoolean("mobileMode", false);
        checkAutofill = sharedPreferences.getBoolean("checkAutofill", true);
        clientUrl = sharedPreferences.getString("clientUrl", "w3c");
        urlPreset = getString(R.string.url_preset);
        setContentView(R.layout.activity_main);
        kioskWeb = findViewById(R.id.kioskView);

        if (android.os.Build.VERSION.SDK_INT >= 26 && checkAutofill) {
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

        if (android.os.Build.VERSION.SDK_INT >= 23 && !Settings.canDrawOverlays(this)) {
            Toast.makeText(this, getString(R.string.additional_perms), Toast.LENGTH_LONG).show();
        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
        startActivityForResult(intent, 4712);
        }

        currentApiVersion = Build.VERSION.SDK_INT;
        final int flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        if (currentApiVersion >= Build.VERSION_CODES.KITKAT) {
            getWindow().getDecorView().setSystemUiVisibility(flags);
            final View decorView = getWindow().getDecorView();
            decorView.setOnSystemUiVisibilityChangeListener(visibility -> {
                if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                    decorView.setSystemUiVisibility(flags);
                }
            });
        }

        setupSettings();
        commitURL(urlPreset + clientUrl);

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
                .setPositiveButton("OK", (dialog, id) -> {
                    String PwInput = password.getText().toString();
                    if (PwInput.equals("exit")) {
                        finish();
                    }
                    else if (PwInput.equals("h")) {
                        Intent startSupportActivityIntent = new Intent(getApplicationContext(), SupportActivity.class);
                        startActivity(startSupportActivityIntent);
                    }
                    else if (PwInput.equals(PASSWORD)) {
                        Intent startSettingsActivityIntent = new Intent(getApplicationContext(), SettingsActivity.class);
                        startActivity(startSettingsActivityIntent);
                    }
                    else if (PwInput.equals("teamviewer")) {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + tvUri));
                        startActivity(intent);
                    }
                    else if (PwInput.equals("anydesk")) {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + adUri));
                        startActivity(intent);
                    }
                    else if (PwInput.equals("ke")) {
                        Intent keyboardEmulator;
                        PackageManager manager = getPackageManager();
                        try {
                            keyboardEmulator = manager.getLaunchIntentForPackage(keUri);
                            if (keyboardEmulator == null)
                                throw new PackageManager.NameNotFoundException();
                            keyboardEmulator.addCategory(Intent.CATEGORY_LAUNCHER);
                            startActivity(keyboardEmulator);
                        } catch (PackageManager.NameNotFoundException ignored) {
                        }
                    }
                    else if (PwInput.equals("i")) {
                        Toast.makeText(MainActivity.this, getString(R.string.running_on) + Build.PRODUCT, Toast.LENGTH_LONG).show();
                    }
                     else {
                        checkPassword(getString(R.string.code_or_help));
                    }
                });

        checkPasswordDialog.setNegativeButton(R.string.cancel, (dialog, id) -> dialog.cancel());
        checkPasswordDialog.show();
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void setupSettings() {
        EnableRuntimePermission();
        if (android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.M) {
            CheckWritePermission();
        }
        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);
        ImageButton settingsButton = findViewById(R.id.settingsButton);
        settingsButton.setOnClickListener(view -> checkPassword(getString(R.string.code_or_help)));

        kioskWeb.setWebChromeClient(new WebChromeClient() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onPermissionRequest(PermissionRequest request) {
                request.grant(request.getResources());
            }
        });

        kioskWeb.setWebViewClient(new WebViewClient()
        {
            public void onReceivedError(WebView webView, int errorCode, String description, String failingUrl) {
            try {
                webView.stopLoading();
            } catch (Exception ignored) {
            }
            webView.loadUrl("about:blank");
            AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
            alertDialog.setTitle(getString(R.string.error));
            alertDialog.setMessage(getString(R.string.check_internet));
            alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.try_again), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    commitURL(urlPreset + clientUrl);
                }
            });

            alertDialog.show();
            super.onReceivedError(webView, errorCode, description, failingUrl);
        }
        });

        kioskWeb.clearCache(true);
        kioskWeb.clearHistory();
        kioskWeb.getSettings().setJavaScriptEnabled(true);
        kioskWeb.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        kioskWeb.getSettings().setMediaPlaybackRequiresUserGesture(false);
        kioskWeb.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        setMobileMode(mobileMode);
        kioskWeb.setOnLongClickListener(view -> {
            commitURL(urlPreset + clientUrl);
            return true;
        });

    }

    public void setMobileMode(final boolean enabled) {
        final WebSettings webSettings = this.kioskWeb.getSettings();
        final String newUserAgent;
        if ((enabled) || isScanner()) {
            newUserAgent = webSettings.getUserAgentString().replace("Safari", "Mobile Safari");
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
        if (url.equals(urlPreset))
        {
            url += getString(R.string.default_url);
        }
        if (isNetworkAvailable())
        {
            kioskWeb.loadUrl(url);
        }
        else
        {
            kioskWeb.loadUrl("about:blank");
        }
        hideKeyboard(this);
        findViewById(R.id.settingsButton).bringToFront();

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
    }

    @Override
    public void onBackPressed() {
        kioskWeb.goBack();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (currentApiVersion >= Build.VERSION_CODES.KITKAT && hasFocus) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                    View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }

    public void EnableRuntimePermission(){
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(MainActivity.this,new String[]{
                    Manifest.permission.CAMERA}, RequestPermissionCode);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void CheckWritePermission(){
        if (! Settings.System.canWrite(this)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
            intent.setData(Uri.parse("package:" + this.getPackageName()));
            startActivity(intent);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] result) {
        if (requestCode == RequestPermissionCode) {
            if (result.length > 0 && result[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(MainActivity.this, getString(R.string.cam_permission), Toast.LENGTH_LONG).show();
            }
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
}

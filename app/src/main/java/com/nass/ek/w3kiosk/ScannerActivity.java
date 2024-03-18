package com.nass.ek.w3kiosk;

import static android.text.TextUtils.isEmpty;
import static com.nass.ek.w3kiosk.ChecksAndConfigs.PW1;
import static com.nass.ek.w3kiosk.ChecksAndConfigs.PW2;
import static com.nass.ek.w3kiosk.ChecksAndConfigs.PW3;
import static com.nass.ek.w3kiosk.ChecksAndConfigs.PW4;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.accessibility.AccessibilityManager;
import android.webkit.CookieManager;
import android.webkit.PermissionRequest;
import android.webkit.URLUtil;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.net.ConnectivityManagerCompat;
import androidx.preference.PreferenceManager;

import com.nass.ek.appupdate.UpdateWrapper;

import org.json.JSONArray;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;


public class ScannerActivity extends AppCompatActivity {
    static String TAG = ScannerActivity.class.getSimpleName();

    private static final int INPUT_FILE_REQUEST_CODE = 1;
    private final int REQUEST_ID_MULTIPLE_PERMISSIONS = 100;
    private WebView webView;
    private ValueCallback<Uri[]> mFilePathCallback;
    private String mCameraPhotoPath;
    public String urlPreset;
    public String clientUrl1;
    public String clientUrl2;
    public String nextUrl;
    public int appsCount;
    public boolean useChrome;
    public boolean autoUpdate;
    Context context = this;
    public Intent intent;

    private boolean connected = false;
    public static String tvUri = "com.teamviewer.quicksupport.market";
    public static String adUri = "com.anydesk.anydeskandroid";
    public static String dhlUri = "com.dhl.ESPcapture";

    // Declare BroadcastReceiver globally in your activity
    BroadcastReceiver onComplete = new BroadcastReceiver() {
        public void onReceive(Context ctxt, Intent intent) {
            Intent i = new Intent(DownloadManager.ACTION_VIEW_DOWNLOADS);
            startActivity(i);
        }
    };
    BroadcastReceiver connectionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null && intent.getAction().equals(ConnectivityManager
                    .CONNECTIVITY_ACTION)) {

                boolean isConnected = Objects.requireNonNull(ConnectivityManagerCompat.getNetworkInfoFromBroadcast
                        ((ConnectivityManager) context.getSystemService(CONNECTIVITY_SERVICE),
                                intent)).isConnected();

                if (connected == isConnected) {
                    return;
                }

                connected = isConnected;

                if (isConnected) {
                    Log.i(TAG, "*** Init WebView connectionReceiver ***");

                    initWebView(urlPreset + clientUrl1);
                    nextUrl = clientUrl2;
                } else {
                    String noNet = context.getString(R.string.noNetwork);
                    String rawHTML = "<HTML>" + "<body><table width=\"100%\" height=\"100%\"><td height=\"30%\"></td><tr><td height=\"40%\" align=\"center\" valign=\"middle\"><h1>" + noNet + "</h1></td><tr><td height=\"30%\"></td></table></body>" + "</HTML>";
                    webView.loadData(rawHTML, "text/HTML", "UTF-8");
                }
            }
        }
    };

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        webView.restoreState(savedInstanceState);

    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        webView.saveState(savedInstanceState);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.i(TAG, "***************** create *************");

        connected = ChecksAndConfigs.isNetworkConnected(this);
        setContentView(R.layout.activity_scanner);
        registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
        ImageButton settingsButton = findViewById(R.id.settingsButton);
        settingsButton.setOnLongClickListener(v -> {
            recreate();
            return true;
        });
        settingsButton.setOnClickListener(view -> checkPassword(getString(R.string.code_or_help)));
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        appsCount = sharedPreferences.getInt("appsCount", 0);
        clientUrl1 = sharedPreferences.getString("clientUrl1", "");
        clientUrl2 = sharedPreferences.getString("clientUrl2", "");
        urlPreset = getString(R.string.url_preset);
        webView = findViewById(R.id.scannerView);
        autoUpdate = sharedPreferences.getBoolean("autoUpdate", false);
        if (autoUpdate) {
            checkUpdate();
        }
        initWebView(urlPreset + clientUrl1);
        nextUrl = clientUrl2;
        findViewById(R.id.settingsButton).bringToFront();
        if (getIntent().getBooleanExtra("EXIT", false)) {
            finish();
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void initWebView(String web_url) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkPermissions();
        }
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        useChrome = sharedPreferences.getBoolean("useChrome", false);
        if (useChrome) {
            openInChrome(web_url);
        } else {
            Log.i(TAG, "*** Init WebView ***");

            webView.setVisibility(View.VISIBLE);

            if (BuildConfig.DEBUG) {
                WebView.setWebContentsDebuggingEnabled(true);
            }

            webView.setWebViewClient(new myWebClient());
            webView.getSettings().setJavaScriptEnabled(true);
            webView.getSettings().setAllowFileAccess(true);
            webView.getSettings().setAllowFileAccessFromFileURLs(true);
            webView.getSettings().setAllowUniversalAccessFromFileURLs(true);
            webView.loadUrl(web_url);

            webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);

            CookieManager.getInstance().setAcceptCookie(true);
            webView.getSettings().setRenderPriority(WebSettings.RenderPriority.HIGH);

            webView.setWebChromeClient(new WebChromeClient() {


                @Override
                public void onPermissionRequest(PermissionRequest request) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        request.grant(request.getResources());
                    }
                }

                public boolean onShowFileChooser(WebView view, ValueCallback<Uri[]> filePath, FileChooserParams fileChooserParams) {
                    if (mFilePathCallback != null) {
                        mFilePathCallback.onReceiveValue(null);
                    }
                    mFilePathCallback = filePath;

                    Intent takePictureIntent;

                    takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                        File photoFile = null;
                        try {
                            photoFile = createImageFile();
                            takePictureIntent.putExtra("PhotoPath", mCameraPhotoPath);
                        } catch (IOException ex) {
                            Log.e("TAG", "Unable to create Image File", ex);
                        }
                        if (photoFile != null) {
                            Uri uri = FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() + ".provider", photoFile);

                            mCameraPhotoPath = "file:" + photoFile.getAbsolutePath();
                            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                            takePictureIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        }
                    }
                    startActivityForResult(takePictureIntent, INPUT_FILE_REQUEST_CODE);
                    return true;
                }
            });

            webView.setWebViewClient(new WebViewClient() {
                public void onPageStarted(WebView view, String url, Bitmap favicon) {
                    Log.i(TAG, "WebView load page " + url);
                }
            });

            webView.setDownloadListener((url, userAgent, contentDisposition, mimetype, contentLength) -> {
                if (url.endsWith(".apk")) {
                    DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
                    request.setMimeType(mimetype);
                    String cookies = CookieManager.getInstance().getCookie(url);
                    request.addRequestHeader("cookie", cookies);
                    request.addRequestHeader("User-Agent", userAgent);
                    request.setDescription("Downloading file...");
                    request.setTitle(URLUtil.guessFileName(url, contentDisposition, mimetype));
                    request.allowScanningByMediaScanner();
                    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                    request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, URLUtil.guessFileName(url, contentDisposition, mimetype));

                    DownloadManager downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                    downloadManager.enqueue(request);
                }

            });
        }
    }

    private File createImageFile() throws IOException {
        String imageFileName = "w3coach";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File tempFile = new File(storageDir + "/" + imageFileName + ".jpg");
        if (tempFile.exists())
            tempFile.delete();
        return tempFile;
    }

    private void runIntent(String wantedIntent){
        switch (wantedIntent) {
            case "Install_Apps":
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES);
                }
                intent.setData(Uri.parse("package:" + ScannerActivity.this.getPackageName()));
                break;
            case "Write_Settings":
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                }
                intent.setData(Uri.parse("package:" + ScannerActivity.this.getPackageName()));
                break;
            case "Manage_Overlay":
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                }
                intent.setData(Uri.parse("package:" + ScannerActivity.this.getPackageName()));
                break;
            case "Power_Menu":
                intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                break;
        }
        startActivity(intent);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void checkPermissions() {

        int permissionCamera = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        int permissionLocation = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        int permissionStorage = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        List<String> listPermissionsNeeded = new ArrayList<>();
        if (permissionCamera != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.CAMERA);
        }
        if (permissionLocation != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (permissionStorage != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }

        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[0]), REQUEST_ID_MULTIPLE_PERMISSIONS);
        }
        PackageManager packageManager = context.getPackageManager();
        if (android.os.Build.VERSION.SDK_INT >= 26 && !packageManager.canRequestPackageInstalls()) {
            runIntent("Install_Apps");
        }
        if (!Settings.System.canWrite(this))
        {
            runIntent("Write_Settings");
        }
        if (android.os.Build.VERSION.SDK_INT >= 23 && !Settings.canDrawOverlays(ScannerActivity.this)) {
            runIntent("Manage_Overlay");
        }
        if (!isAccessibilitySettingsOn()) {
            runIntent("Power_Menu");
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Uri[] results = null;
            if (resultCode == Activity.RESULT_OK) {
                if (mCameraPhotoPath != null) {
                    results = new Uri[]{Uri.parse(mCameraPhotoPath)};
                }
            }
            mFilePathCallback.onReceiveValue(results);
            mFilePathCallback = null;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_ID_MULTIPLE_PERMISSIONS) {
            Map<String, Integer> perms = new HashMap<>();

            perms.put(Manifest.permission.CAMERA, PackageManager.PERMISSION_GRANTED);
            perms.put(Manifest.permission.ACCESS_FINE_LOCATION, PackageManager.PERMISSION_GRANTED);
            perms.put(Manifest.permission.WRITE_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED);
            perms.put(Manifest.permission.RECORD_AUDIO, PackageManager.PERMISSION_GRANTED);

            if (grantResults.length > 0) {
                for (int i = 0; i < permissions.length; i++)
                    perms.put(permissions[i], grantResults[i]);

                if (perms.get(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                        && perms.get(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                        && perms.get(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                        && perms.get(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                    Log.e("Response", "Granted");

                } else {
                    Log.e("Response", "Denied");
                    JSONArray array = new JSONArray();
                    for (String permission : permissions) {
                        array.put(permission);
                    }
                }
            }
        }

    }

    public static class myWebClient extends WebViewClient {
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) { super.onPageStarted(view, url, favicon); }

        @Override
        public void onPageFinished(WebView view, String url) { super.onPageFinished(view, url); }
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
                        Intent intent = new Intent(ScannerActivity.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        intent.putExtra("EXIT", true);
                        startActivity(intent);                    }
                    else if (PwInput.equals("h")) {
                        Intent startSupportActivityIntent = new Intent(getApplicationContext(), SupportActivity.class);
                        startActivity(startSupportActivityIntent);
                    }
                    else if (PwInput.equals("i")) {
                        Intent startAboutActivityIntent = new Intent(getApplicationContext(), AboutActivity.class);
                        startActivity(startAboutActivityIntent);
                    }
                    else if (PwInput.equals("ad")) {
                        if (checkApps(adUri))
                        {
                            appClick(adUri);
                        } else {
                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + adUri));
                            startActivity(intent);
                        }                    }
                    else if (PwInput.equals("dhl")) {
                        if (checkApps(dhlUri))
                        {
                            appClick(dhlUri);
                        } else {
                            initWebView("https://nass-ek.de/android/ESP_Capture_2.5.121.apk");                        }                    }
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
                    else if (PwInput.equals("tv")) {
                        if (checkApps(tvUri))
                        {
                            appClick(tvUri);
                        } else {
                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + tvUri));
                            startActivity(intent);
                        }                    }
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
        if (appsCount > 0 && !useChrome)
        {
            checkPasswordDialog.setNeutralButton(R.string.apps, (dialog, id) -> startActivity(new Intent(this, AppsActivity.class)));
        } else if (clientUrl1.startsWith("ber") && checkApps(dhlUri)) {
            checkPasswordDialog.setNeutralButton(R.string.dhl, (dialog, id) -> appClick(dhlUri));
        }
        else if (!clientUrl2.isEmpty() && !useChrome) {
            checkPasswordDialog.setNeutralButton(R.string.toggleUrl, (dialog, id) -> toggleUrl());
        }
        else
        {
            checkPasswordDialog.setNeutralButton(R.string.reboot, (dialog, id) -> startService(new Intent(this, ShutdownService.class)));
        }
        AlertDialog dialog = checkPasswordDialog.create();
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setAllCaps(false);
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setAllCaps(false);
        dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setAllCaps(false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(connectionReceiver, filter);
        registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
        webView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            unregisterReceiver(connectionReceiver);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }

        webView.onPause();
    }

    public void checkUpdate() {

        String updateFound=(String.format(getString(R.string.UpdateAvailable), getString(R.string.app_name)));
        UpdateWrapper updateWrapper = new UpdateWrapper.Builder(ScannerActivity.this)
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
    public boolean isAccessibilitySettingsOn() {
        AccessibilityManager am = (AccessibilityManager) getSystemService(ACCESSIBILITY_SERVICE);
        ContentResolver cr = getApplicationContext().getContentResolver();
        if (am.isEnabled()) {
            String settingValue = Settings.Secure.getString(cr, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            return settingValue != null && settingValue.contains("com.nass.ek.w3kiosk/com.nass.ek.w3kiosk.ShutdownService");
        }
        return false;
    }

    private void toggleUrl(){
        if (nextUrl.equals(clientUrl2)){
            if (clientUrl2.startsWith("http")) {
                initWebView(clientUrl2);
            } else {
                initWebView(urlPreset + clientUrl2);
            }
            findViewById(R.id.settingsButton).bringToFront();
            nextUrl = clientUrl1;
        }
        else if (nextUrl.equals(clientUrl1)){
            initWebView(urlPreset + clientUrl1);
            findViewById(R.id.settingsButton).bringToFront();
            nextUrl = clientUrl2;
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
    public void openInChrome(String UriString) {

        int marqueeBgColor = getResources().getColor(R.color.colorMarquee);
        String reLoad = context.getString(R.string.reLoad);
        String rawHTML = "<HTML>"+ "<body bgcolor=\"" + marqueeBgColor + "\"><table width=\"100%\" height=\"100%\"><td height=\"30%\"></td><tr><td height=\"40%\" align=\"center\" valign=\"middle\"><h1>" + reLoad +"</h1></td><tr><td height=\"30%\"></td></table></body>"+ "</HTML>";
        webView.loadData(rawHTML, "text/HTML", "UTF-8");
        if (!isEmpty(UriString)) {
            Uri uri = Uri.parse(UriString);
            CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
            builder.setShowTitle(false);
            builder.setShareState(CustomTabsIntent.SHARE_STATE_OFF);
            builder.setToolbarColor(ContextCompat.getColor(this, R.color.colorPrimary));
            CustomTabsIntent customTabsIntent = builder.build();
            customTabsIntent.intent.setPackage("com.android.chrome");
            customTabsIntent.launchUrl(this, uri);
        }
    }
    private void openStorageManager(Context context) {
        Intent intent = new Intent();
        intent.setAction(android.provider.Settings.ACTION_INTERNAL_STORAGE_SETTINGS);
        context.startActivity(intent);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(onComplete);
    }
}
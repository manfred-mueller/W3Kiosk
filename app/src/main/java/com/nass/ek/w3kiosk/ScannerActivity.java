package com.nass.ek.w3kiosk;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.PermissionRequest;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

import org.json.JSONArray;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScannerActivity extends AppCompatActivity {

    private static final int INPUT_FILE_REQUEST_CODE = 1;
    private final int REQUEST_ID_MULTIPLE_PERMISSIONS = 100;
    private WebView webView;
    private ValueCallback<Uri[]> mFilePathCallback;
    private String mCameraPhotoPath;
    public String urlPreset;
    public String clientUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner);
        ImageButton settingsButton = findViewById(R.id.settingsButton);
        settingsButton.setOnLongClickListener(v -> {
            recreate();
            return true;
        });
        settingsButton.setOnClickListener(view -> checkPassword(getString(R.string.code_or_help)));
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        clientUrl = sharedPreferences.getString("clientUrl1", "w3c");
        urlPreset = getString(R.string.url_preset);
        webView = findViewById(R.id.scannerView);
        initWebView(urlPreset + clientUrl);
        findViewById(R.id.settingsButton).bringToFront();
        if (getIntent().getBooleanExtra("EXIT", false))
        {
            finish();
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void initWebView(String web_url) {

        webView.setVisibility(View.VISIBLE);

        checkPermissions();

        webView.setWebViewClient(new myWebClient());
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setAllowFileAccess(true);
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
                        mCameraPhotoPath = "file:" + photoFile.getAbsolutePath();
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                                Uri.fromFile(photoFile));
                    }
                }
                startActivityForResult(takePictureIntent, INPUT_FILE_REQUEST_CODE);
                return true;
            }
        });
    }

    private File createImageFile() throws IOException {
        String imageFileName = "w3coach";
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File tempFile = new File(storageDir + imageFileName + ".jpg");
        if (tempFile.exists())
            tempFile.delete();
        return tempFile;
    }

    private void checkPermissions() {

        int permissionCamera = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        int permissionStorage = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        List<String> listPermissionsNeeded = new ArrayList<>();
        if (permissionCamera != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.CAMERA);
        }
        if (permissionStorage != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }

        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), REQUEST_ID_MULTIPLE_PERMISSIONS);
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
            perms.put(Manifest.permission.WRITE_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED);
            perms.put(Manifest.permission.RECORD_AUDIO, PackageManager.PERMISSION_GRANTED);

            if (grantResults.length > 0) {
                for (int i = 0; i < permissions.length; i++)
                    perms.put(permissions[i], grantResults[i]);

                if (perms.get(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
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
                    else if (PwInput.equals("a")) {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + MainActivity.adUri));
                        startActivity(intent);
                    }
                    else if (PwInput.equals("r")) {
                        startService(new Intent(this, ShutdownService.class));
                    }
                    else if (PwInput.equals("t")) {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + MainActivity.tvUri));
                        startActivity(intent);
                    }
                    else if (PwInput.equals(MainActivity.PASSWORD)) {
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
        checkPasswordDialog.setNeutralButton(R.string.reboot, (dialog, id) -> startService(new Intent(this, ShutdownService.class)));
        AlertDialog dialog = checkPasswordDialog.create();
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setAllCaps(false);
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setAllCaps(false);
        dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setAllCaps(false);
    }


}
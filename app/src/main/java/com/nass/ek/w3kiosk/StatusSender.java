package com.nass.ek.w3kiosk;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.util.Base64;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class StatusSender {
    @SuppressLint("StaticFieldLeak")
    static void sendData(String Device, String URL, String Connection) {
        new AsyncTask<Void, Void, Integer>() {
            @Override
            protected Integer doInBackground(Void... voids) {

                Map<String, String> postData = new HashMap<>();
                postData.put("device_name", Device);
                postData.put("client_url", URL);
                postData.put("connection_type", Connection);
                postData.put("last_contact_time", new SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault()).format(new Date()));

                try {
                    java.net.URL url = new URL(BuildConfig.API_URL);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("POST");
                    String userCredentials = BuildConfig.API_NAME + ":" + BuildConfig.API_KEY;
                    String basicAuth = "Basic " + Base64.encodeToString(userCredentials.getBytes(), Base64.NO_WRAP);
                    connection.setRequestProperty("Authorization", basicAuth);
                    connection.setDoInput(true);
                    connection.setDoOutput(true);

                    StringBuilder postDataString = new StringBuilder();
                    for (Map.Entry<String, String> entry : postData.entrySet()) {
                        if (postDataString.length() > 0) {
                            postDataString.append('&');
                        }
                        postDataString.append(entry.getKey());
                        postDataString.append('=');
                        postDataString.append(entry.getValue());
                    }
                    byte[] postDataBytes = postDataString.toString().getBytes(StandardCharsets.UTF_8);
                    connection.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));

                    OutputStream outputStream = connection.getOutputStream();
                    outputStream.write(postDataBytes);
                    outputStream.close();

                    int responseCode = connection.getResponseCode();
                    connection.disconnect();
                    return responseCode;
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }
        }.execute();
    }
}

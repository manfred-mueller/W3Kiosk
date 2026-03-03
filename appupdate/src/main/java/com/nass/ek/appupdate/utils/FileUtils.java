package com.nass.ek.appupdate.utils;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInstaller;
import android.net.Uri;
import android.os.Build;

import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class FileUtils {

    public static String getApkFilePath(Context context, String downLoadUrl) {
        File externalFile = context.getExternalFilesDir(null);
        String filePath = externalFile.getAbsolutePath();
        String fileName;
        if (downLoadUrl.endsWith(".apk")) {
            int index = downLoadUrl.lastIndexOf("/");
            if (index != -1) {
                fileName = downLoadUrl.substring(index);
            } else {
                fileName = context.getPackageName() + ".apk";
            }
        } else {
            fileName = context.getPackageName() + ".apk";
        }
        return new File(filePath, fileName).getAbsolutePath();
    }

    // Silent Install via PackageInstaller - kein Dialog, funktioniert im LockTask-Modus
    public static void silentInstallApk(Context context, File apkFile) {
        PackageInstaller packageInstaller = context.getPackageManager().getPackageInstaller();
        PackageInstaller.SessionParams params = new PackageInstaller.SessionParams(
                PackageInstaller.SessionParams.MODE_FULL_INSTALL);
        try {
            int sessionId = packageInstaller.createSession(params);
            PackageInstaller.Session session = packageInstaller.openSession(sessionId);

            try (InputStream in = new FileInputStream(apkFile);
                 OutputStream out = session.openWrite("package", 0, apkFile.length())) {
                byte[] buffer = new byte[4096];
                int len;
                while ((len = in.read(buffer)) != -1) {
                    out.write(buffer, 0, len);
                }
                session.fsync(out);
            }

            int flags = PendingIntent.FLAG_UPDATE_CURRENT;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                flags |= PendingIntent.FLAG_MUTABLE;
            }
            PendingIntent intent = PendingIntent.getBroadcast(
                    context, sessionId,
                    new Intent("com.nass.ek.w3kiosk.INSTALL_COMPLETE"),
                    flags);
            session.commit(intent.getIntentSender());
            session.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Fallback - nicht mehr aktiv genutzt
    @Deprecated
    public static Intent openApkFile(Context context, File outputFile) {
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(android.content.Intent.ACTION_VIEW);
        Uri uri;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            uri = FileProvider.getUriForFile(context,
                    context.getPackageName() + ".provider.appupdatefileprovider", outputFile);
        } else {
            uri = Uri.fromFile(outputFile);
        }
        intent.setDataAndType(uri, "application/vnd.android.package-archive");
        return intent;
    }
}

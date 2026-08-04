package com.example.admin.filesystem;

import static com.example.admin.filesystem.MainActivity.LOGTAG;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.Manifest;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class PermissionUtils {

    public static boolean hasPermissions(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {

            return Environment.isExternalStorageManager();

        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;

        } else {
            return true;
        }
    }

    public static void requestPermissions(Activity activity, int requestCode) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {

            logPrintUtils(activity, "Build.VERSION.SDK_INT >= Build.VERSION_CODES.R");
            Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
            intent.addCategory("android.intent.category.DEFAULT");
            intent.setData(Uri.parse(String.format("package:%s", activity.getPackageName())));
            activity.startActivityForResult(intent, requestCode);

            // а вот так можно вызвать окно со списком всех программ у которых есть или нужно такое право на запись в корень диска и найти среди них свою
            /*logPrintUtils(activity, "Build.VERSION.SDK_INT >= Build.VERSION_CODES.R второе условие");
            Intent intent = new Intent();
            intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
            activity.startActivityForResult(intent, requestCode);*/

        } else {
            logPrintUtils(activity, "Build.VERSION.SDK_INT < Build.VERSION_CODES.R");
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    requestCode);
        }
    }

    private static void logPrintUtils(Activity activity, String msg){
        Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show();
        Log.d(LOGTAG, msg);
    }
}
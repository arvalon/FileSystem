package com.example.admin.filesystem;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import java.io.File;
import java.io.FileOutputStream;

public class SecondActivity extends AppCompatActivity {

    private static final String TAG = "vga";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.my_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_file) {
            doFileOperationWrapper();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public static final String P = Manifest.permission.WRITE_EXTERNAL_STORAGE;

    // прослойка для проверки прав
    private void doFileOperationWrapper() {
        if (ContextCompat.checkSelfPermission(this, P)
                == PackageManager.PERMISSION_GRANTED) {
            // права есть! все хорошо, работает
            doFileOperation();
            return;
        }

        // прав нет, запрашиваем
        ActivityCompat.requestPermissions(
                this,
                new String[] { P },
                55 // эта константа придет в onRequestPermissionsResult в requestCode
        );
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults) {

        if (requestCode == 55) {
            // ответ на наш запрос
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // пользователь разрешил
                doFileOperation();
            } else {
                Log.d(TAG, "access denied :(");
            }


        } else
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void doFileOperation() {
        Log.d(TAG, "file operation started");

        // личная папка приложения на внутреннем сторадже
        File internalFilesDir = getFilesDir();
        String FILENAME = "internal.txt";
        Log.d(TAG, "internalFilesDir " + internalFilesDir.getAbsolutePath());
        File target1 = new File(internalFilesDir, FILENAME);
        saveSomething(target1, 'a', 100);
        Log.d(TAG,"записали файл "+FILENAME+" в "+internalFilesDir.getPath());

        // личная папка приложения на внешнем сторадже
        File externalFilesDir = getExternalFilesDir(null);
        Log.d(TAG, "externalFilesDir " + externalFilesDir.getAbsolutePath());
        File target2 = new File(externalFilesDir, "external.txt");
        saveSomething(target2, 'b', 160);
// публичный каталог известного назначения
//        File externalPublicDir =
//                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
//        Log.d(TAG, "externalPublicDir " + externalPublicDir.getAbsolutePath());

        // публичный каталог == корень SD-карты
        File externalStorage = Environment.getExternalStorageDirectory();
        Log.d(TAG, "externalStorage " + externalStorage.getAbsolutePath());
        File target3 = new File(externalStorage, "public.txt");
        saveSomething(target3, 'c', 215);

        MediaScannerConnection.scanFile(this,
                new String[] { target3.toString() }, null,
                new MediaScannerConnection.OnScanCompletedListener() {
                    public void onScanCompleted(String path, Uri uri) {
                        Log.i(TAG, "Scanned " + path + ":");
                        Log.i(TAG, "-> uri=" + uri);
                    }
                });

        Log.d(TAG, "file operation finished");
    }

    private void saveSomething(File where, Character what, int len) {
        try {
            String str = new String(new char[len]).replace('\0', what);
//            if (where.exists()) {
//                where.delete();
//            }
            FileOutputStream stream = new FileOutputStream(where, true);
            try {
                stream.write(str.getBytes());
            } finally {
                stream.close();
            }

            Log.d(TAG, "ok: " + where.getAbsolutePath() + " " + where.length());

        } catch (Exception e) {
            Log.d(TAG, "error: " + e.getMessage());
            e.printStackTrace();
        }
    }


    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            // внешний сторадж доступен на запись
            return true;
        }

        // внешний сторадж НЕ доступен на запись
        return false;
    }

}

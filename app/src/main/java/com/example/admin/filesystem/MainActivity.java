package com.example.admin.filesystem;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.view.menu.ExpandedMenuView;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSIONS_WRITE_EXTERNAL_STORAGE = 333;
    private static final String TEMP_FILE_NAME = "tempfile.txt";
    private EditText editText;
    
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.secondactivity_btn).setOnClickListener(v -> {
            startActivity(new Intent(this,SecondActivity.class));
        });
        
        editText = (EditText) findViewById(R.id.edit_text);
    }

    public void filesDirs(View view) {
        // Приватный каталог приложения - недоступен другим приложениям
        Log.d("happy", "Internal storage dir: " + getFilesDir().toString());

        // Корень "внешнего" носителя
        // Для записи нужны права WRITE_EXTERNAL_STORAGE
        File externalStorage = Environment.getExternalStorageDirectory();
        if(externalStorage != null)
        {
            Log.d("happy", "External storage dir: " + externalStorage.toString());
            if(Build.VERSION.SDK_INT >= 18)
            {
                Log.d(
                        "happy",
                        "External storage dir has available bytes: "
                                + new StatFs(externalStorage.toString()).getAvailableBytes());
            }
        }

        // Каталог приложения на внешнем носителе
        // Для хранения файлов определенных типов
        // Будет удален при удалении приложения
        // Не нужны права WRITE_EXTERNAL_STORAGE
        File externalPics = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if(externalPics != null)
        {
            Log.d("happy", "External dir for pics: " + externalPics.toString());
        }

        // "Внещний" каталог для временных файлов приложения
        // Не требуются права WRITE_EXTERNAL_STORAGE
        // Доступ имеют все приложения
        // Удаляется с удалением приложения
        File externalCacheDir = getExternalCacheDir();
        if(externalCacheDir != null)
        {
            Log.d("happy", "External cache dir: " + externalCacheDir.toString());
        }

        // Общесистемный "внешний" каталог для файлов определенных типов
        // При удалении приложения его файлы не удаляются
        // Для записи нужны права WRITE_EXTERNAL_STORAGE
        File externalPublicPicsDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES
        );
        if(externalPublicPicsDir != null)
        {
            Log.d("happy", "External public dir for pics: " + externalPublicPicsDir.toString());
        }
    }

    public boolean isExternalStorageWritable()
    {
        String state = Environment.getExternalStorageState();
        if(Environment.MEDIA_MOUNTED.equals(state))
        {
            // "Внешний" носитель доступен на запись
            return true;
        }
        else
        {
            // "Внешний" носитель на запись не доступен
            return false;
        }
    }

    public void fileWrite(View view) {
        doWrite();
    }


    private void doWrite() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                    new String[]{
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                    },
                    PERMISSIONS_WRITE_EXTERNAL_STORAGE
            );
        } else {

            String text = editText.getText().toString();

            //File externalCacheDir = getExternalCacheDir();
            File externalCacheDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);

            if (externalCacheDir != null && isExternalStorageWritable() && !text.equals("")) {

                OutputStream oStream;
                File f = new File(externalCacheDir, TEMP_FILE_NAME);
                try {
                    oStream = new FileOutputStream(f);
                    oStream.write(text.getBytes());
                    oStream.flush();
                    oStream.close();
                    Log.d("happy", "writeToFile");
                } catch (Exception e) {
                    Log.d("happy", e.toString());
                }
            }
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSIONS_WRITE_EXTERNAL_STORAGE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission is granted
                doWrite();
            } else {
                Toast.makeText(this, "Cannot write to files without this permission", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void fileRead(View view) {

        File externalCacheDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);

        if(externalCacheDir != null)
        {
            File f = new File(externalCacheDir, TEMP_FILE_NAME);
            InputStream inputStream;
            try {
                inputStream = new FileInputStream(f);

                StringBuilder sb = new StringBuilder();
                Reader r = new InputStreamReader(inputStream, "UTF-8");
                char[] buffer = new char[1000];

                int amt = r.read(buffer);
                while (amt > 0)
                {
                    sb.append(buffer, 0, amt);
                    amt = r.read(buffer);
                }
                Toast.makeText(this, "File is :" + sb.toString(), Toast.LENGTH_SHORT).show();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }
}

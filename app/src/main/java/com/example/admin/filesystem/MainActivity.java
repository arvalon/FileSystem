package com.example.admin.filesystem;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.getserial.Foo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

/**
 * При переходе на SDK 29+ при запуске на Android 10+ требуется переход на новый способ работы с файлами<br>
 * <a href="https://stackoverflow.com/questions/63302047/android-10-eacces-permission-denied-issue">Android 10 open failed: EACCES (Permission denied)</a><br>
 * <a href="https://ourcodeworld.com/articles/read/1559/how-does-manage-external-storage-permission-work-in-android">How to</a><br>
 * <a href="https://android-tools.ru/coding/poluchaem-razreshenie-manage_external_storage-dlya-prilozheniya/?ysclid=llw1l0eel7465907824">How to 2</a><br>
 * <a href="https://stackoverflow.com/questions/36936914/list-of-android-permissions-normal-permissions-and-dangerous-permissions-in-api">Список обычных и опасных разрешений</a>
 */
public class MainActivity extends AppCompatActivity {

    public final static String LOGTAG = "filesystem.log";

    private static final int PERMISSIONS_STORAGE = 1;
    private static final int PERMISSIONS_PHONE_STATE = 2;
    private static final String TEMP_FILE_NAME = "tempfile.txt";

    private EditText editText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d(LOGTAG, getString(R.string.app_name) + " v. "+ BuildConfig.VERSION_NAME + ", onCreate, Build.VERSION.SDK_INT: " + Build.VERSION.SDK_INT);
        logPrint("Разрешение на запись в External Storage: "+PermissionUtils.hasPermissions(this));

        findViewById(R.id.saf_btn).setOnClickListener(v -> startActivity(new Intent(this, SafActivity.class)));

        findViewById(R.id.secondactivity_btn).setOnClickListener(v -> startActivity(new Intent(this,SecondActivity.class)));
        
        editText = findViewById(R.id.edit_text);

        //KotlinClassTest();
    }

    public void getPermissions(View view) {

        if (!PermissionUtils.hasPermissions(this)) {
            PermissionUtils.requestPermissions(this, PERMISSIONS_STORAGE);
        } else {
            logPrint("Разрешение на файлы уже получены");
        }
    }

    /* это отрабатывает от Android 6 до Android 10 */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSIONS_STORAGE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                logPrint("onRequestPermissionsResult Ура есть права на файлы!");
            } else {
                logPrint("onRequestPermissionsResult Права так и не прилетели :(");
            }
            requestOtherPermissions();
        }

        if (requestCode == PERMISSIONS_PHONE_STATE){
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                logPrint("onRequestPermissionsResult Ура есть права READ_PHONE_STATE!");

            } else {
                logPrint("onRequestPermissionsResult Права READ_PHONE_STATE так и не прилетели :(");
            }
        }
    }

    /* это отрабатывает на Android 11+ */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PERMISSIONS_STORAGE) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {

                if (PermissionUtils.hasPermissions(this)) {
                    logPrint("onActivityResult Ура есть права!");
                } else {
                    logPrint("onActivityResult Права так и не прилетели :(");
                }

                requestOtherPermissions();
            }
        }
    }

    private void requestOtherPermissions(){
        logPrint("requestOtherPermissions");

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, PERMISSIONS_PHONE_STATE);
        }
    }

    /** распечатить в лог пути к разным директириям, доступным приложению */
    public void filesDirs(View view) {

        // Приватный каталог приложения - недоступен другим приложениям
        Log.d(LOGTAG, "Internal storage: " + getFilesDir().toString());

        // Корень "внешнего" носителя
        // Для записи нужны права WRITE_EXTERNAL_STORAGE
        File externalStorage = Environment.getExternalStorageDirectory();

        if(externalStorage != null) {

            Log.d(LOGTAG, "External storage: " + externalStorage + ", свободно (байт): " + new StatFs(externalStorage.toString()).getAvailableBytes());
        }else {
            Log.d(LOGTAG, "External storage null");
        }

        // Каталог приложения на внешнем носителе
        // Для хранения файлов определенных типов
        // Будет удален при удалении приложения
        // Не нужны права WRITE_EXTERNAL_STORAGE
        File externalPics = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        if(externalPics != null) {
            Log.d(LOGTAG, "External dir for pics: " + externalPics);
        }

        // Общесистемный "внешний" каталог для файлов определенных типов
        // При удалении приложения его файлы не удаляются
        // Для записи нужны права WRITE_EXTERNAL_STORAGE
        File externalPublicPicDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES
        );

        if(externalPublicPicDir != null) {
            Log.d(LOGTAG, "External public dir for pic: " + externalPublicPicDir);
        }

        // "Внещний" каталог для временных файлов приложения
        // Не требуются права WRITE_EXTERNAL_STORAGE
        // Доступ имеют все приложения
        // Удаляется с удалением приложения
        File externalCacheDir = getExternalCacheDir();

        if(externalCacheDir != null) {
            Log.d(LOGTAG, "External cache dir: " + externalCacheDir);
        } else {
            Log.d(LOGTAG, "External cache dir null");
        }
    }

    public void fileWrite(View view) {

        String text = editText.getText().toString();

        //File externalDir = getExternalCacheDir();
        //File externalDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File externalDir = Environment.getExternalStorageDirectory();

        if (externalDir != null && isExternalStorageWritable() && !text.equals("")) {

            try (OutputStream oStream = new FileOutputStream(new File(externalDir, TEMP_FILE_NAME))) {

                oStream.write(text.getBytes());
                oStream.flush();
                oStream.close();
                Log.d(LOGTAG, "Запись в файл закончена: " + text);

            } catch (IOException e) {
                Log.d(LOGTAG, "Запись в файл в StoragePublicDirectory для картинок :" + e);
            }
        }
    }

    public void createDir(View view){

        File dir = new File(Environment.getExternalStorageDirectory(),"MyDir");

        logPrint("Создание папки "+dir.getAbsolutePath()+": "+dir.mkdir());
    }

    /** Доступен на запись или нет "Внешний" носитель */
    public boolean isExternalStorageWritable() {

        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    public void fileRead(View view) {

        File externalStorageDirectory = Environment.getExternalStorageDirectory();

        if(externalStorageDirectory != null) {

            try (InputStream inputStream = new FileInputStream(new File(externalStorageDirectory, TEMP_FILE_NAME))) {

                StringBuilder sb = new StringBuilder();
                Reader r = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
                char[] buffer = new char[1000];

                int amt = r.read(buffer);

                while (amt > 0)
                {
                    sb.append(buffer, 0, amt);
                    amt = r.read(buffer);
                }

                logPrint("Содежжимое "+ TEMP_FILE_NAME+": " + sb);

            } catch (IOException e) {

                Log.d(LOGTAG, e.toString());
                e.printStackTrace();
            }
        }
    }

    public void screenShotInputStream(View view) {

        Log.d(LOGTAG,"Получение скриншота экрана");

        Process process;
        try {
            //process = Runtime.getRuntime().exec("screencap -p /sdcard/screencap.png");

            process = Runtime.getRuntime().exec("screencap -p");

            InputStream is = process.getInputStream();

            Log.d(LOGTAG,"is.available(): "+is.available());

            File screenshot = new File (getFilesDir(),generateFileName());

            FileOutputStream fos = new FileOutputStream(screenshot);

            byte[] buffer = new byte[is.available()];

            is.read(buffer);

            fos.write(buffer);

            fos.flush();
            fos.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void screenShotScreenCap(View view) {

        Process process;

        String fileName = generateFileName();

        try {
            process = Runtime.getRuntime().exec("screencap -p "
                    +getFilesDir().getPath()
                    + fileName);

            process.waitFor();

        } catch (IOException | InterruptedException e) {
            Log.d(LOGTAG,"Ошибка получения снимка экрана: "+e.getMessage());
            e.printStackTrace();
        }finally {
            Log.d(LOGTAG,"получение снимка экрана "+ fileName);
        }
    }

    private String generateFileName() {
        return "screenshot_"+System.currentTimeMillis()+".png";
    }

    private static void KotlinClassTest() {

        new InnerFoo().printHelloWorld();

        Foo foo = new Foo();
        foo.printHelloWorld();
    }
    private void logPrint(String msg){
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        Log.d(LOGTAG, msg);
    }
}

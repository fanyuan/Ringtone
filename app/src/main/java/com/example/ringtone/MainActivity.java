package com.example.ringtone;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.databinding.DataBindingUtil;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import com.example.ringtone.databinding.ActivityMainBinding;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    //Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE,
    String [] perms = {Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.WRITE_SETTINGS,Manifest.permission.MODIFY_AUDIO_SETTINGS};

    ActivityMainBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this,R.layout.activity_main);
        binding.setAct(this);
        checkFilePermission();
    }

    /**
     * WRITE_SETTINGS权限申请
     */
    public void requestWriteSettings(){
        Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_WRITE_SETTINGS);
        intent.setData(Uri.parse("package:" + getPackageName()));
        //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); //开启一个新activity
        startActivity(intent);
    }

    /**
     * 设置来电铃声
     */
    public void setRingtone(){
        Context context = getApplicationContext();
        String path =  Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "abc.mp3";//getExternalCacheDir().getAbsolutePath() + File.separator + "abc.mp3";
        File sdfile = new File(path);
        boolean flag = sdfile.exists();
        Log.d("test","exists = " + flag + "   path = "+path + " --- " + sdfile.getName());

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.System.canWrite(MainActivity.this)) {
                requestWriteSettings();
                return;
            }

        }

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            setRingtoneAndroidQ(sdfile);
        }else {
            setRingtone(this,RingtoneManager.TYPE_RINGTONE,path,"wtete");
        }

    }

    /**
     * 适配android q以上版本  android 9
     * @param ringtoneFile
     */
    private void setRingtoneAndroidQ(File ringtoneFile){
        Log.d("test","---setRingtoneAndroidQ---");
        ContentValues values = new ContentValues();
        values.put(MediaStore.Audio.Media.DISPLAY_NAME, "ring_demo.mp3");
        values.put(MediaStore.Audio.Media.MIME_TYPE, "audio/mp3");
        values.put(MediaStore.Audio.Media.TITLE, "ring_demo.mp3");
        values.put(MediaStore.Audio.Media.RELATIVE_PATH, "Music/test");
        values.put(MediaStore.Audio.Media.IS_RINGTONE, true);
        values.put(MediaStore.Audio.Media.IS_NOTIFICATION, false);
        values.put(MediaStore.Audio.Media.IS_ALARM, false);
        values.put(MediaStore.Audio.Media.IS_MUSIC, false);

        Uri external = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;//MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        ContentResolver resolver = getContentResolver();

        Uri insertUri = resolver.insert(external, values);

        Log.d("test","insertUri: " + insertUri);

        String uriPaht = getFilePathFromContentUri(insertUri,getContentResolver());
        Log.d("test","setRingtoneAndroidQ uriPaht: " + uriPaht);


        OutputStream os = null;
        FileInputStream inputStream = null;

        if(insertUri != null){
            try {
                os = resolver.openOutputStream(insertUri);
                if (os != null) {
                    inputStream = new FileInputStream(ringtoneFile);
                    byte[] bytes = new byte[1024];
                    int len = 0;
                    while ((len=inputStream.read(bytes)) != -1) {
                        os.write(bytes,0,len);
                    }
                    inputStream.close();
                    os.close();
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        RingtoneManager.setActualDefaultRingtoneUri(this, RingtoneManager.TYPE_RINGTONE, insertUri);
        Log.d("test","123 insertUri: " + insertUri);
        Toast.makeText(this,"铃声设置完成",Toast.LENGTH_SHORT).show();
    }
    /**
     *
     * 设置铃声
     *
     * type RingtoneManager.TYPE_RINGTONE 来电铃声
     *             RingtoneManager.TYPE_NOTIFICATION 通知铃声
     *             RingtoneManager.TYPE_ALARM 闹钟铃声
     *
     *  path 下载下来的mp3全路径
     *  title 铃声的名字
     */
    public void setRingtone(Context context, int type, String path, String title) {

        Uri oldRingtoneUri = RingtoneManager.getActualDefaultRingtoneUri(context, RingtoneManager.TYPE_RINGTONE); //系统当前  通知铃声
        Uri oldNotification = RingtoneManager.getActualDefaultRingtoneUri(context, RingtoneManager.TYPE_NOTIFICATION); //系统当前  通知铃声
        Uri oldAlarm = RingtoneManager.getActualDefaultRingtoneUri(context, RingtoneManager.TYPE_ALARM); //系统当前  闹钟铃声

        File sdfile = new File(path);
        ContentValues values = new ContentValues();
        values.put(MediaStore.MediaColumns.DATA, sdfile.getAbsolutePath());
        values.put(MediaStore.MediaColumns.TITLE, title);
        values.put(MediaStore.MediaColumns.MIME_TYPE, "audio/mp3");
        values.put(MediaStore.Audio.Media.IS_RINGTONE, true);
        values.put(MediaStore.Audio.Media.IS_NOTIFICATION, true);
        values.put(MediaStore.Audio.Media.IS_ALARM, true);
        values.put(MediaStore.Audio.Media.IS_MUSIC, true);

        Uri uri = MediaStore.Audio.Media.getContentUriForPath(sdfile.getAbsolutePath());
        Uri newUri = null;
        String deleteId = "";
        try {
            Cursor cursor = context.getContentResolver().query(uri, null, MediaStore.MediaColumns.DATA + "=?", new String[] { path },null);
            if (cursor.moveToFirst()) {
                deleteId = cursor.getString(cursor.getColumnIndex("_id"));
            }
            //LogTool.e("AGameRing", "deleteId:" + deleteId);
            Log.d("test","checkFilePermission");
            context.getContentResolver().delete(uri,
                    MediaStore.MediaColumns.DATA + "=\"" + sdfile.getAbsolutePath() + "\"", null);
            newUri = context.getContentResolver().insert(uri, values);
        } catch (Exception e) {
            e.printStackTrace();
        }
        String uriPaht = getFilePathFromContentUri(newUri,getContentResolver());
        Log.d("test","setRingtone uriPaht: " + uriPaht);
        if (newUri != null) {

            String ringStoneId = "";
            String notificationId = "";
            String alarmId = "";
            if (null != oldRingtoneUri) {
                ringStoneId = oldRingtoneUri.getLastPathSegment();
            }

            if (null != oldNotification) {
                notificationId = oldNotification.getLastPathSegment();
            }

            if (null != oldAlarm) {
                alarmId = oldAlarm.getLastPathSegment();
            }

            Uri setRingStoneUri;
            Uri setNotificationUri;
            Uri setAlarmUri;

            if (type == RingtoneManager.TYPE_RINGTONE || ringStoneId.equals(deleteId)) {
                setRingStoneUri = newUri;
            } else {
                setRingStoneUri = oldRingtoneUri;
            }

            if (type == RingtoneManager.TYPE_NOTIFICATION || notificationId.equals(deleteId)) {
                setNotificationUri = newUri;
            } else {
                setNotificationUri = oldNotification;
            }

            if (type == RingtoneManager.TYPE_ALARM || alarmId.equals(deleteId)) {
                setAlarmUri = newUri;
            } else {
                setAlarmUri = oldAlarm;
            }

            RingtoneManager.setActualDefaultRingtoneUri(context, RingtoneManager.TYPE_RINGTONE, setRingStoneUri);
            RingtoneManager.setActualDefaultRingtoneUri(context, RingtoneManager.TYPE_NOTIFICATION, setNotificationUri);
            RingtoneManager.setActualDefaultRingtoneUri(context, RingtoneManager.TYPE_ALARM, setAlarmUri);

            switch (type) {
                case RingtoneManager.TYPE_RINGTONE:
                    Toast.makeText(context.getApplicationContext(), "设置来电铃声成功！", Toast.LENGTH_SHORT).show();
                    break;
                case RingtoneManager.TYPE_NOTIFICATION:
                    Toast.makeText(context.getApplicationContext(), "设置通知铃声成功！", Toast.LENGTH_SHORT).show();
                    break;
                case RingtoneManager.TYPE_ALARM:
                    Toast.makeText(context.getApplicationContext(), "设置闹钟铃声成功！", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }
    private String getFilePathFromContentUri(Uri selectedVideoUri, ContentResolver contentResolver) {
        String filePath;

        String[] filePathColumn = {MediaStore.MediaColumns.DATA};

        Cursor cursor = contentResolver.query(selectedVideoUri, filePathColumn, null, null, null);

        cursor.moveToFirst();

        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);

        filePath = cursor.getString(columnIndex);

        cursor.close();

        return filePath;

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void checkFilePermission() {
        if(ActivityCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, perms, 123);
        }
        Log.d("test","checkFilePermission");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.d("test","onRequestPermissionsResult permissions = " + Arrays.toString(permissions) + "  grantResults = " + Arrays.toString(grantResults));
    }

}
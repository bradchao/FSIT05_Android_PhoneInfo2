package tw.brad.phoneinfo2;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import java.sql.Date;
import java.text.SimpleDateFormat;

public class MainActivity extends AppCompatActivity {
    private TelephonyManager tmgr;
    private ContentResolver contentResolver;
    private ImageView img;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_PHONE_STATE,
                            Manifest.permission.READ_CALL_LOG,
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.READ_CONTACTS,},
                    1);

        }else{
            init();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        init();
    }

    private void init(){
        img = findViewById(R.id.img);
        tmgr = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        tmgr.listen(new MyPhoneStateListener(), PhoneStateListener.LISTEN_CALL_STATE);

        contentResolver = getContentResolver();

        // Write Setting
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if (!Settings.System.canWrite(this)){
                Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivity(intent);
            }
        }


    }

    public void test1(View view) {
        Uri uri = CallLog.Calls.CONTENT_URI;
        Cursor cursor = contentResolver.query(
                uri,null,null,null,null);
        while (cursor.moveToNext()){
            String name = cursor.getString(cursor.getColumnIndex(CallLog.Calls.CACHED_NAME));
            String number = cursor.getString(cursor.getColumnIndex(CallLog.Calls.NUMBER));
            String type = cursor.getString(cursor.getColumnIndex(CallLog.Calls.TYPE));
            long date = cursor.getLong(cursor.getColumnIndex(CallLog.Calls.DATE));
            String duration = cursor.getString(cursor.getColumnIndex(CallLog.Calls.DURATION));

            // CallLog.Calls.TYPE == below
            // INCOMING_TYPE
            //OUTGOING_TYPE
            //MISSED_TYPE
            //VOICEMAIL_TYPE
            //REJECTED_TYPE
            //BLOCKED_TYPE
            //ANSWERED_EXTERNALLY_TYPE
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date cdate = new Date(date);
            String calldate = sdf.format(cdate);

            Log.v("brad", name + ":" + number+":"+calldate+":"+duration);

        }
    }

    public void test2(View view) {
        Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        Cursor cursor = contentResolver.query(uri,
                null,null,null,null);
        String[] fields = cursor.getColumnNames();
        for (String field: fields){
//            Log.v("brad", field);
        }
        //Log.v("brad", ContactsContract.CommonDataKinds.Phone.NUMBER);


        while (cursor.moveToNext()){
            String name =
                    cursor.getString(
                            cursor.getColumnIndex(
                                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            String number =
                    cursor.getString(
                            cursor.getColumnIndex(
                                    ContactsContract.CommonDataKinds.Phone.NUMBER));
            Log.v("brad", name + ":" + number);
        }



    }

    public void test3(View view) {
//        Uri uri = Settings.System.CONTENT_URI;
//        Log.v("brad", uri.toString());
//        Uri uri2 = Uri.parse("content://settings/system");

//        Cursor cursor = contentResolver.query(uri,
//                null,null,null,null);
//        while (cursor.moveToNext()){
//            String name = cursor.getString(cursor.getColumnIndex("name"));
//            String value = cursor.getString(cursor.getColumnIndex("value"));
//            Log.v("brad", name + " => " + value);
//        }

        Log.v("brad", getSettingValue(Settings.System.FONT_SCALE));
        Log.v("brad", getSettingValue(Settings.System.SCREEN_BRIGHTNESS));
    }
    public void test4(View view) {
        Settings.System.putInt(
                contentResolver,Settings.System.SCREEN_BRIGHTNESS,127);
        contentResolver.notifyChange(Settings.System.CONTENT_URI,null);
        Log.v("brad", getSettingValue(Settings.System.SCREEN_BRIGHTNESS));
    }

    private String getSettingValue(String name){
        String ret = "";

        Uri uri = Settings.System.CONTENT_URI;
        Cursor cursor = contentResolver.query(uri,
                new String[]{"name", "value"},
                "name = ?",new String[]{name},
                null);
        if (cursor.getCount()>0){
            cursor.moveToNext();
            ret = cursor.getString(cursor.getColumnIndex("value"));
        }

        return ret;
    }

    public void test5(View view) {
        Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        Cursor cursor = contentResolver.query(uri,
                null,null,null,null);

        if (cursor.getCount()>0){
            cursor.moveToNext();
            String data = cursor.getString(
                    cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            Log.v("brad", data);

            Bitmap bmp = BitmapFactory.decodeFile(data);
            img.setImageBitmap(bmp);

        }
    }


    private class MyPhoneStateListener extends PhoneStateListener {
        @Override
        public void onCallStateChanged(int state, String phoneNumber) {
            super.onCallStateChanged(state, phoneNumber);

            switch (state){
                case TelephonyManager.CALL_STATE_IDLE:
                    Log.v("brad", "idle");
                    break;
                case TelephonyManager.CALL_STATE_RINGING:
                    Log.v("brad", "ring:" + phoneNumber);
                    break;
                case TelephonyManager.CALL_STATE_OFFHOOK:
                    Log.v("brad", "offhook:" + phoneNumber);
                    break;
            }
        }
    }



}

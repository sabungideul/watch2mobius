package com.example.watch2mobius;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;
import android.view.WindowManager;
import android.widget.TextView;

import com.example.watch2mobius.databinding.ActivityMainBinding;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends Activity implements SensorEventListener {

    private final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STROAGE = 1001;
    private PermissionSupport permission;
//    static final int ALARM_REQ_CODE = 100;

    private ActivityMainBinding binding;
    private static final String TAG = "MainActivity";

    private TextView mTextView;

    //    private TextView batteryTxt;

    private static PowerManager.WakeLock wakeLock = null;

    SensorManager sm;
    Sensor gyroSensor;
    Sensor accSensor;

    @SuppressLint("InvalidWakeLockTag")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 센서 설정
        sm = (SensorManager) getSystemService(SENSOR_SERVICE);
        gyroSensor = sm.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        accSensor = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

////          배터리 정보 리시버 등록
//        BroadcastReceiver mBatInfoReceiver = new BroadcastReceiver() {
//            @Override
//            public void onReceive(Context txt, Intent intent) {
//                int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
//                batteryTxt.setText("Battery: " + String.valueOf(level) + "%");
//            }
//        };

//        binding = ActivityMainBinding.inflate(getLayoutInflater());
//        setContentView(binding.getRoot());
//
//        mTextView = binding.text;

//        항상 화면 켜지도록
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

//        터치 방지
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mTextView = binding.text;

////        배터리 출력
//        batteryTxt = (TextView) this.findViewById(R.id.tv_battery);
//        this.registerReceiver(mBatInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
//
//        AlarmManager alarmManager =(AlarmManager) getSystemService(ALARM_SERVICE);
//
//        int time = 1;
//        long triggerTime = System.currentTimeMillis()+ (time*1000);
//
//        Intent iBroadCast = new Intent(MainActivity.this,MyAlarm.class);
//        PendingIntent pi = null;
//
//        if (Build.VERSION.SDK_INT>= Build.VERSION_CODES.S){
//            pi = PendingIntent.getBroadcast(MainActivity.this,ALARM_REQ_CODE,iBroadCast,PendingIntent.FLAG_MUTABLE);
//        }else {
//            pi = PendingIntent.getBroadcast(MainActivity.this,ALARM_REQ_CODE,iBroadCast,PendingIntent.FLAG_UPDATE_CURRENT);
//        }

        //alarmManager.set(AlarmManager.RTC_WAKEUP,triggerTime,pi);
        //setting the repeating alarm that will be fired every day
//        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, triggerTime, 3000, pi);

        permissionCheck();

        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, "DoNotDimScreen");

        wakeLock.acquire();

    }

    @Override

    protected void onResume() {
        super.onResume();

        // 센서에 대한 딜레이 설정. 이걸 해 줘야 리스너로 값이 떨어짐 NORMAL < UI < GAME < FASTEST
        sm.registerListener(this, gyroSensor, SensorManager.SENSOR_DELAY_NORMAL);
        sm.registerListener(this, accSensor , SensorManager.SENSOR_DELAY_NORMAL);

        // Data collection for machine learning
//        sm.registerListener(this, gyroSensor, SensorManager.SENSOR_DELAY_FASTEST);
//        sm.registerListener(this, accSensor , SensorManager.SENSOR_DELAY_FASTEST);

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // 정확도 변경시 사용된다는것 같은데 정확한 용도는 잘 모르겠다. 사용되는건 한번도 못 본듯 하다.
    }

    public String val_gyro_x;
    public String val_gyro_y;
    public String val_gyro_z;
    public String acc_gyro_x;
    public String acc_gyro_y;
    public String acc_gyro_z;
    public String sen_Data;

    @Override
    public void onSensorChanged(SensorEvent event) {
        //여기서 센서값이 변하는걸 체크한다.
        switch (event.sensor.getType()) {
            case Sensor.TYPE_GYROSCOPE:
//                Log.d("tag", "TYPE_GYROSCOPE");
//                Log.d("tag", event.values[0]+", "+event.values[1]+", "+event.values[2]);

                val_gyro_x = event.values[0] + ",";
                val_gyro_y = event.values[1] + ",";
                val_gyro_z = event.values[2] + ",";
                break;

            case Sensor.TYPE_ACCELEROMETER:
//                Log.d("tag", "TYPE_ACCELEROMETER");
//                Log.d("tag", event.values[0]+", "+event.values[1]+", "+event.values[2]);

                acc_gyro_x = event.values[0] + ",";
                acc_gyro_y = event.values[1] + ",";
                acc_gyro_z = event.values[2] + "";
                break;
        }

        sen_Data = val_gyro_x + val_gyro_y + val_gyro_z + acc_gyro_x + acc_gyro_y + acc_gyro_z;
        Log.d("[DATA]", sen_Data);

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        new Thread(
                ()-> method(sen_Data)

        ).start();

    }

    //    키 막기 1
    @Override
    public void onBackPressed () {
    }

    //    키 막기 2
    @Override
    public void onPause() {
        if (isApplicationSentToBackground(this)){
            // Do what you want to do on detecting Home Key being Pressed
            System.out.println("\nOUTOUTOUTOUTOUTOUTOUTOUTOUTOUTOUTOUTOUTOUTOUTOUTOUTOUTOUTOUT\n");
        }
        super.onPause();
    }


    public boolean isApplicationSentToBackground(final Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> tasks = am.getRunningTasks(1);
        if (!tasks.isEmpty()) {
            ComponentName topActivity = tasks.get(0).topActivity;
            if (!topActivity.getPackageName().equals(context.getPackageName())) {
                return true;
            }
        }
        return false;
    }

    //Mobius 올리기
    private void method(String sen_data) {
        OkHttpClient client = new OkHttpClient().newBuilder().build();
        MediaType mediaType = MediaType.parse("application/json;ty=4");
        RequestBody body = RequestBody.create( "{\n    \"m2m:cin\": {\n        \"con\": \""+sen_data+"\"\n    }\n}",mediaType);
        Request request = new Request.Builder()
                .url("http://203.253.128.177:7579/Mobius/SW_Hackaton/watch")
                .method("POST", body)
                .addHeader("Accept", "application/json")
                .addHeader("X-M2M-RI", "12345")
                .addHeader("X-M2M-Origin", "SOrigin")
                .addHeader("Content-Type", "application/json;ty=4")
                .build();

        try {
            Response response = client.newCall(request).execute();
            System.out.println(response.body().string());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void permissionCheck(){
        // sdk 23버전 이하 버전에서는 permission이 필요하지 않음
        if(Build.VERSION.SDK_INT >= 23){

            // 클래스 객체 생성
            permission =  new PermissionSupport(this, this);

            // 권한 체크한 후에 리턴이 false일 경우 권한 요청을 해준다.
            if(!permission.checkPermission()){
                permission.requestPermission();
            }
        }
    }

}
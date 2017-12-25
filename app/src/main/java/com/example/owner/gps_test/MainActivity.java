package com.example.owner.gps_test;

import android.Manifest;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    SensorManager sensorManager; //センサーマネージャ
    Sensor accelSensor; //加速度センサ
    MainActivity.MySensorEventListener mySensorEventListener; //センサーのイベントリスナ
    TextView textViewX; //x座標
    TextView textViewY; //y座標
    TextView textViewZ; //z座標
    int UPDATE_INTERVAL = 1000; //表示の変更間隔(1秒)
    long lastUpdate;
    private static final long MIN_TIME = 1000 * 10; //更新時間の最小値(10秒)
    private static final float MIN_DISTANCE = 5.0f; //更新距離の最小値(5m)
    private static final String PERMISSION_ERROR_MSG = "Location Permission Error";
    private TextView textViewTime;
    private TextView textViewLon;
    private TextView textViewLat;
    private TextView textViewAcc;
    private TextView textViewAlt;
    private LocationManager locationManager;
    private MyLocationListener myLocationListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (accelSensor != null) {
            //センサのイベントリスナをSensormanagerに登録
            mySensorEventListener = new MainActivity.MySensorEventListener();
            sensorManager.registerListener(mySensorEventListener, accelSensor, SensorManager.SENSOR_DELAY_UI);
        }
        lastUpdate = System.currentTimeMillis();

        //パーミッションチェック
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, PERMISSION_ERROR_MSG, Toast.LENGTH_SHORT).show();
        } else {
            locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

            if (null != locationManager.getProvider(LocationManager.GPS_PROVIDER)) {
                Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                setLocationText(location);

                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME, MIN_DISTANCE, myLocationListener);
            }
        }

        textViewTime = findViewById(R.id.textViewTime);
        textViewLon = findViewById(R.id.textViewLongitude);
        textViewLat = findViewById(R.id.textViewLatitude);
        textViewAcc = findViewById(R.id.textViewAccuracy);
        textViewAlt = findViewById(R.id.textViewAltitude);

        myLocationListener = new MyLocationListener();


        textViewX = findViewById(R.id.textViewX);
        textViewY = findViewById(R.id.textViewY);
        textViewZ = findViewById(R.id.textViewZ);


        //Sensormanagerの取得
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        //加速度センサ追加
        accelSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        if (accelSensor == null) {
            Toast.makeText(this, "加速度センサーは使用できません", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onPause() {
        //permission check
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, PERMISSION_ERROR_MSG, Toast.LENGTH_SHORT);
        } else {
            locationManager.removeUpdates(myLocationListener);
        }

        //permission check
        sensorManager.unregisterListener(mySensorEventListener);
        super.onPause();
    }

    class MySensorEventListener implements SensorEventListener {
        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {
            if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                //センサが加速センサの時の処理
                long actualTime = System.currentTimeMillis();

                if (actualTime - lastUpdate > UPDATE_INTERVAL) {
                    lastUpdate = actualTime;

                    //センサの値を取得
                    float x = sensorEvent.values[0];
                    float y = sensorEvent.values[1];
                    float z = sensorEvent.values[2];

                    //センサの値を表示
                    textViewX.setText("　　 　　X軸: " + String.valueOf(x));
                    textViewY.setText("　　　 　Y軸: " + String.valueOf(y));
                    textViewZ.setText("　 　　　Z軸: " + String.valueOf(z));
                }
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {

        }
    }

        private void setLocationText(Location location) {
            if (location != null) {
                String time = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault()).format(new Date(location.getTime()));
                double lon = location.getLongitude();
                double lat = location.getLatitude();
                textViewTime.setText("　　測定時間: " + time);
                textViewLon.setText("　　　　経度: "+ lon);
                textViewLat.setText("　　　　緯度: " + lat);

                if (location.hasAccuracy()) {
                    double acc = location.getAccuracy();
                    textViewAcc.setText("　　　　精度: " + acc);
                }

                if (location.hasAltitude()) {
                    double alt = location.getAltitude();
                    textViewAlt.setText("　　　　高度: " + alt);
                }
            }
        }

        class MyLocationListener implements LocationListener {
            @Override
            public void onLocationChanged(Location location) {
                setLocationText(location);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {
            }
        }
    }
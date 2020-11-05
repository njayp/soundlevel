package com.doepiccoding.voiceanalizer;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

public class MovementService extends Service implements SensorEventListener {

    private static SharedPreferences preferences ;
    private static SharedPreferences.Editor editor ;
    private boolean mInitialized;
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    // epoch time since last file write
    private long lastTime = 0;
    // minimum time in seconds to write to file after previous write
    private int period = 5;

    public MovementService(Context applicationContext) {
        super();
        Log.i("Movement service", "here I am!");
        preferences = PreferenceManager.getDefaultSharedPreferences(applicationContext);
        editor = preferences.edit();
    }

    public MovementService(){

    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Start recording
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        //here u should make your service foreground so it will keep working even if app closed
        return Service.START_STICKY;
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        float x = sensorEvent.values[0];
        float y = sensorEvent.values[1];
        float z = sensorEvent.values[2];
        if (!mInitialized) mInitialized = true;
        long tsLong = System.currentTimeMillis()/1000;
        if (tsLong > lastTime+period) {
            lastTime = tsLong;
            recordAccelData(x, y, z);
        }
    }

    // write to file a line in format:
    // epochtime, x, y, z
    public void recordAccelData(float x, float y, float z){
        double acceleration = Math.sqrt(Math.pow(x, 2) +
                Math.pow(y, 2) +
                Math.pow(z, 2)) - SensorManager.GRAVITY_EARTH;
        Log.d("Movement service",""+acceleration);
        editor.putLong("acceleration",(long)acceleration);
        editor.commit();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mSensorManager.unregisterListener(this);
    }
}

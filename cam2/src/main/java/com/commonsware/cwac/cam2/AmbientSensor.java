package com.commonsware.cwac.cam2;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import static android.content.Context.SENSOR_SERVICE;

/**
 * Created by Toufeeq on 1/27/2017.
 */

public class AmbientSensor implements SensorEventListener {

    private static boolean testMode = false;
    private SensorManager mSensorManager;
    private Sensor mLight;
    private static int sensorValue = 0;
    private int accuracyLevel = 0;

    public AmbientSensor(Context context){
        mSensorManager = (SensorManager)context.getSystemService(SENSOR_SERVICE);
        mLight = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        mSensorManager.registerListener(this, mLight, SensorManager.SENSOR_DELAY_NORMAL);
    }
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        // Do not change values in test mode.
        if (testMode) {
            return;
        }
        if( sensorEvent.sensor.getType() == Sensor.TYPE_LIGHT){
            sensorValue = (int)sensorEvent.values[0];
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        if(sensor.getType() == Sensor.TYPE_LIGHT){
            accuracyLevel = accuracy;
        }
    }

    // HACK to get tests working. Use dagger to mock this class in test setup.ß
    public static void setTestSensorValue(int sensorValue) {
        AmbientSensor.testMode = true;
        AmbientSensor.sensorValue = sensorValue;
    }

    public int getSensorValue(){
        return sensorValue;
    }

    public int getAccuracyLevel(){
        return accuracyLevel;
    }

    public void unregisterSensorListener(){
        mSensorManager.unregisterListener(this);
    }
}

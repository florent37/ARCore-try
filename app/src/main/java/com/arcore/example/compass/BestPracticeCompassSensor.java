package com.arcore.example.compass;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

public class BestPracticeCompassSensor extends BaseCompassSensor {
    private String LOG_TAG = BestPracticeCompassSensor.class.getSimpleName();

    private static final float mAccelerometerReading[] = new float[3];
    private static final float mMagnetometerReading[] = new float[3];
    private static final float mRotationMatrix[] = new float[9];
    private static final float mOrientationAngles[] = new float[3];

    private Sensor mAccelerometer;
    private Sensor mMagnetometer;


    public BestPracticeCompassSensor(OnAzimuthListener listener, AppCompatActivity activity) {
        super(listener, activity);
        mAccelerometer = getSensorManager().getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mMagnetometer = getSensorManager().getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
    }

    @Override
    protected void onResume() {
        getSensorManager().registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_UI);
        getSensorManager().registerListener(this, mMagnetometer, SensorManager.SENSOR_DELAY_UI);
        Log.e(LOG_TAG, "sensor resumed");
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(event.values, 0, mAccelerometerReading, 0, mAccelerometerReading.length);
        } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(event.values, 0, mMagnetometerReading, 0, mMagnetometerReading.length);
        }
        SensorManager.remapCoordinateSystem(mRotationMatrix, SensorManager.AXIS_X, SensorManager.AXIS_Z, mRotationMatrix);
        SensorManager.getRotationMatrix(mRotationMatrix, null, mAccelerometerReading, mMagnetometerReading);
        SensorManager.getOrientation(mRotationMatrix, mOrientationAngles);

        double azimuth = Math.toDegrees(mOrientationAngles[0]);

        onAzimuthChanged((float) azimuth);
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}

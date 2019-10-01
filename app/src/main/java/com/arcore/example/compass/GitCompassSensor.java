package com.arcore.example.compass;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.Surface;

public class GitCompassSensor extends BaseCompassSensor {
    private String LOG_TAG = GitCompassSensor.class.getSimpleName();
    private Sensor mAccelerometer;
    private Sensor mMagnetometer;
    private static final float ALPHA = 0.97f;
    private static final float[] mAccelerometerData = new float[3];
    private static final float[] mGeomagneticData = new float[3];

    private float[] mRawRotationMatrix = new float[9];
    private float[] mRotationMatrix = new float[9];

    private float[] mOrientationData = new float[3];


    public GitCompassSensor(OnAzimuthListener listener, AppCompatActivity activity) {
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

        synchronized (this) {
            storeValues(event);
            SensorManager.getRotationMatrix(mRawRotationMatrix, null, mAccelerometerData, mGeomagneticData);
            configureDeviceAngle();
            SensorManager.getOrientation(mRotationMatrix, mOrientationData);

            float azimuth = (float) Math.toDegrees(mOrientationData[0]);
            onAzimuthChanged(azimuth);
        }
    }

    private void configureDeviceAngle() {
        switch (getActivity().getWindowManager().getDefaultDisplay().getRotation()) {
            case Surface.ROTATION_0: // Portrait
                SensorManager.remapCoordinateSystem(mRawRotationMatrix, SensorManager.AXIS_Z,
                        SensorManager.AXIS_Y, mRotationMatrix);
                break;
            case Surface.ROTATION_90: // Landscape
                SensorManager.remapCoordinateSystem(mRawRotationMatrix, SensorManager.AXIS_Y,
                        SensorManager.AXIS_MINUS_Z, mRotationMatrix);
                break;
            case Surface.ROTATION_180: // Portrait
                SensorManager.remapCoordinateSystem(mRawRotationMatrix, SensorManager.AXIS_MINUS_Z,
                        SensorManager.AXIS_MINUS_Y, mRotationMatrix);
                break;
            case Surface.ROTATION_270: // Landscape
                SensorManager.remapCoordinateSystem(mRawRotationMatrix, SensorManager.AXIS_MINUS_Y,
                        SensorManager.AXIS_Z, mRotationMatrix);
                break;
        }
    }

    private void storeValues(SensorEvent event) {
        int type = event.sensor.getType();
        switch (type) {
            case Sensor.TYPE_ACCELEROMETER: {
                mAccelerometerData[0] = ALPHA * mAccelerometerData[0] + (1 - ALPHA) * event.values[0];
                mAccelerometerData[1] = ALPHA * mAccelerometerData[1] + (1 - ALPHA) * event.values[1];
                mAccelerometerData[2] = ALPHA * mAccelerometerData[2] + (1 - ALPHA) * event.values[2];
                break;
            }
            case Sensor.TYPE_MAGNETIC_FIELD: {
                mGeomagneticData[0] = ALPHA * mGeomagneticData[0] + (1 - ALPHA) * event.values[0];
                mGeomagneticData[1] = ALPHA * mGeomagneticData[1] + (1 - ALPHA) * event.values[1];
                mGeomagneticData[2] = ALPHA * mGeomagneticData[2] + (1 - ALPHA) * event.values[2];
                break;
            }
        }
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}

package com.arcore.example.compass;

import android.hardware.GeomagneticField;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.arcore.example.compass.custom.LowPassFilter;

import java.util.concurrent.atomic.AtomicBoolean;

public class CustomCompassSensor extends BaseCompassSensor  {
    private String LOG_TAG = CustomCompassSensor.class.getSimpleName();

    private static final float gravity[] = new float[3]; // Gravity (a.k.a accelerometer data)
    private static final float magnitude[] = new float[3]; // Magnetic
    private static final float rotation[] = new float[9]; // Rotation matrix in Android format
    private static final float orientation[] = new float[3]; // azimuth, pitch, roll
    private static float smoothed[] = new float[3];
    private static double floatBearing = 0;
    private static GeomagneticField gmf = null;

    private static final AtomicBoolean computing = new AtomicBoolean(false);


    public CustomCompassSensor(OnAzimuthListener listener, AppCompatActivity activity) {
        super(listener, activity);
    }

    @Override
    protected void onResume() {
        getSensorManager().registerListener(this, getSensorManager().getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
        getSensorManager().registerListener(this, getSensorManager().getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), SensorManager.SENSOR_DELAY_NORMAL);
        Log.e(LOG_TAG, "sensor resumed");
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (!computing.compareAndSet(false, true)) {
            return;
        }

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            smoothed = LowPassFilter.filter(event.values, gravity);
            gravity[0] = smoothed[0];
            gravity[1] = smoothed[1];
            gravity[2] = smoothed[2];
        } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            smoothed = LowPassFilter.filter(event.values, magnitude);
            magnitude[0] = smoothed[0];
            magnitude[1] = smoothed[1];
            magnitude[2] = smoothed[2];
        }

        // Get rotation matrix given the gravity and geomagnetic matrices
        SensorManager.getRotationMatrix(rotation, null, gravity, magnitude);
        SensorManager.getOrientation(rotation, orientation);
        floatBearing = orientation[0];

        // Convert from radians to degrees
        floatBearing = Math.toDegrees(floatBearing); // degrees east of true
        // north (180 to -180)

        // Compensate for the difference between true north and magnetic north
        if (gmf != null) {
            floatBearing += gmf.getDeclination();
        }


        // adjust to 0-360
        if (floatBearing < 0) {
            floatBearing += 360;
        }


        onAzimuthChanged((float) floatBearing);
        computing.set(false);
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}

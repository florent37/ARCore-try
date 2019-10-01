/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.arcore.example.compass;

import android.annotation.SuppressLint;
import android.content.Context;
import android.hardware.GeomagneticField;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import androidx.appcompat.app.AppCompatActivity;

import com.arcore.example.compass.custom.MathUtils;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class GoogleCompassSensor extends BaseCompassSensor implements LocationListener {

    private static final long METERS_BETWEEN_LOCATIONS = 2;
    private static final long MILLIS_BETWEEN_LOCATIONS = TimeUnit.SECONDS.toMillis(3);
    private static final long MAX_LOCATION_AGE_MILLIS = TimeUnit.MINUTES.toMillis(30);

    private boolean mTracking;
    private boolean mHasInterference;
    private float mPitch;
    private final float[] mRotationMatrix = new float[16];
    private final float[] mOrientation = new float[9];

    private LocationManager mLocationManager;
    private GeomagneticField mGeomagneticField;

    public GoogleCompassSensor(OnAzimuthListener listener, AppCompatActivity activity) {
        super(listener, activity);
        mLocationManager = (LocationManager) (activity.getSystemService(Context.LOCATION_SERVICE));
    }

    @SuppressLint("MissingPermission")
    @Override
    protected void onResume() {
        if (!mTracking) {
            getSensorManager().registerListener(this,
                    getSensorManager().getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR),
                    SensorManager.SENSOR_DELAY_UI);

            getSensorManager().registerListener(this,
                    getSensorManager().getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
                    SensorManager.SENSOR_DELAY_UI);

            Location lastLocation = mLocationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
            if (lastLocation != null) {
                long locationAge = lastLocation.getTime() - System.currentTimeMillis();
                if (locationAge < MAX_LOCATION_AGE_MILLIS) {
                    updateGeomagneticField(lastLocation);
                }
            }

            List<String> providers = mLocationManager.getProviders(true);
            for (String provider : providers) {
                mLocationManager.requestLocationUpdates(
                        provider,
                        MILLIS_BETWEEN_LOCATIONS,
                        METERS_BETWEEN_LOCATIONS,
                        this,
                        Looper.getMainLooper());
            }

            mTracking = true;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mTracking) {
            mLocationManager.removeUpdates(this);
            mTracking = false;
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (isRotationSensor(event)) {
            SensorManager.getRotationMatrixFromVector(mRotationMatrix, event.values);
            SensorManager.remapCoordinateSystem(mRotationMatrix, SensorManager.AXIS_X, SensorManager.AXIS_Z, mRotationMatrix);
//            SensorManager.remapCoordinateSystem(mRotationMatrix, SensorManager.AXIS_Z, SensorManager.AXIS_MINUS_X, mRotationMatrix);
            SensorManager.getOrientation(mRotationMatrix, mOrientation);

            // Store the pitch (used to display a message indicating that the user's head
            // angle is too steep to produce reliable results.
            mPitch = (float) Math.toDegrees(mOrientation[1]);

            // Convert the heading (which is relative to magnetic north) to one that is
            // relative to true north, using the user's current location to compute this.
            float magneticHeading = (float) Math.toDegrees(mOrientation[0]);
            float azimuth = MathUtils.mod(computeTrueNorth(magneticHeading), 360.0f);

            onAzimuthChanged(azimuth);
        } else if (isMagneticSensor(event)) {
            precessMagneticValues(event);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        if (sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            mHasInterference = (accuracy < SensorManager.SENSOR_STATUS_ACCURACY_HIGH);
        }
    }

    private void precessMagneticValues(SensorEvent event) {
        // TODO: 2/5/18
    }

    @Override
    public void onLocationChanged(Location location) {
        updateGeomagneticField(location);
    }

    private void updateGeomagneticField(Location location) {
        float latitude = (float) location.getLatitude();
        float longitude = (float) location.getLongitude();
        float altitude = (float) location.getAltitude();
        long time = location.getTime();
        mGeomagneticField = new GeomagneticField(latitude, longitude, altitude, time);
    }

    private boolean isMagneticSensor(SensorEvent event) {
        return event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD;
    }

    private boolean isRotationSensor(SensorEvent event) {
        return event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR;
    }

    private float computeTrueNorth(float heading) {
        return mGeomagneticField != null ? heading + mGeomagneticField.getDeclination() : heading;
    }


    @Override
    public void onProviderDisabled(String provider) {
        // Don't need to do anything here.
    }

    @Override
    public void onProviderEnabled(String provider) {
        // Don't need to do anything here.
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // Don't need to do anything here.
    }
}

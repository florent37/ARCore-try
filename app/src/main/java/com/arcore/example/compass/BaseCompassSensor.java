package com.arcore.example.compass;

import android.content.Context;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;

import florent37.github.com.rxlifecycle.RxLifecycle;

public abstract class BaseCompassSensor implements SensorEventListener {
    private String LOG_TAG = BaseCompassSensor.class.getSimpleName();

    private OnAzimuthListener mListener;
    private SensorManager mSensorManager;
    private AppCompatActivity mActivity;


    public BaseCompassSensor(OnAzimuthListener listener, AppCompatActivity activity) {
        mListener = listener;
        mSensorManager = (SensorManager) (activity.getSystemService(Context.SENSOR_SERVICE));
        mActivity = activity;
        subscribeOnLifeCycleEvents(activity);
    }

    private void subscribeOnLifeCycleEvents(AppCompatActivity activity) {
        RxLifecycle.with(activity)
                .onResume()
                .subscribe(event -> onResume(), throwable -> {
                    throwable.printStackTrace();
                    Log.e("++++++", throwable.getMessage());
                });

        RxLifecycle.with(activity)
                .onPause()
                .subscribe(event -> onPause());
    }

    protected abstract void onResume();

    protected void onPause() {
        getSensorManager().unregisterListener(this);
        Log.e(LOG_TAG, "sensor paused");
    }

    protected void onAzimuthChanged(float newAzimuth) {
        if (mListener != null) {
            mListener.onAzimuthChanged(newAzimuth);
        }
    }

    protected SensorManager getSensorManager() {
        return mSensorManager;
    }

    public AppCompatActivity getActivity() {
        return mActivity;
    }
}

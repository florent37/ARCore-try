/*
 * Copyright 2017 Google Inc. All Rights Reserved.
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

package com.arcore.example;

import android.opengl.GLSurfaceView;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.arcore.example.arcoremanager.ArCoreManager;
import com.arcore.example.arcoremanager.object.FerrariObjectDrawer;
import com.arcore.example.compass.BaseCompassSensor;
import com.arcore.example.compass.CompassViewGoogle;
import com.arcore.example.compass.CompassViewLM;
import com.arcore.example.settings.ObjectSettings;

import butterknife.BindView;
import butterknife.ButterKnife;

import butterknife.OnClick;

/**
 * This is a simple example that shows how to create an augmented reality (AR) application using
 * the ARCore API. The application will display any detected planes and will allow the user to
 * tap on a plane to place a 3d model of the Android robot.
 */
public class MainActivity extends AppCompatActivity implements ArCoreManager.Listener, ObjectSettings.Listener {

    // Rendering. The Renderers are created here, and initialized when the GL surface is created.
    @BindView(R.id.surfaceview)
    GLSurfaceView mSurfaceView;

    private ArCoreManager mARCoreManager;

    @BindView(R.id.message)
    protected TextView mMessage;

    @BindView(R.id.configLocal)
    protected ViewGroup mConfigLocal;

    @BindView(R.id.compass_view)
    protected CompassViewGoogle/*LM*/ mCompassView;

//    @BindView(R.id.compass_view_test)
//    protected CompassViewTest mCompassViewTest;

    protected BaseCompassSensor mCompassSensor;
    private float mAzimuth = 130;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
    }

//    @OnClick(R.id.compass_view_test)
//    public void rotate() {
//        mAzimuth = (mAzimuth + 123) % 360;
//        mCompassViewTest.onAzimuthChanged(mAzimuth);
//    }

    public void init() {
        if (mARCoreManager == null) {
//            mCompassSensor = new GitCompassSensor(mCompassView, this);

            mARCoreManager = new ArCoreManager(this, this);
            mARCoreManager.setup(mSurfaceView);
            mARCoreManager.addObjectToDraw(new FerrariObjectDrawer());
            mConfigLocal.addView(new ObjectSettings(this, this));
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] results) {
        if (!PermissionHelper.hasPermission(this)) {
            Toast.makeText(this, "Camera permission is needed to run this application", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (PermissionHelper.hasPermission(this)) {
            init();
        } else {
            PermissionHelper.requestPermission(this);
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            // Standard Android full-screen functionality.
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

    @Override
    public void onArCoreUnsupported() {
        Toast.makeText(MainActivity.this, "This device does not support AR", Toast.LENGTH_LONG).show();
        finish();
    }

    @Override
    public void onPermissionNotAllowed() {
        //on permission not allowed
        Toast.makeText(MainActivity.this, "Camera permission is needed to run this application", Toast.LENGTH_LONG).show();
        finish();
    }

    @Override
    public void showLoadingMessage() {
        runOnUiThread(() -> {
            mMessage.setText("Searching for surfaces...");
            mMessage.animate().alpha(1f);
        });
    }

    @Override
    public void hideLoadingMessage() {
        runOnUiThread(() -> mMessage.setVisibility(View.GONE));
    }

    @Override
    public void onTouchModeChanged(ArCoreManager.ObjectTouchMode objectTouchMode) {
        mARCoreManager.setTouchMode(objectTouchMode);
    }

    @Override
    public void onClearScreen() {
        mARCoreManager.onClearScreen();
    }
}

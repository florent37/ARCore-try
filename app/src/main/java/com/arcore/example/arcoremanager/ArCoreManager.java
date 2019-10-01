package com.arcore.example.arcoremanager;

import android.opengl.GLSurfaceView;
import android.support.annotation.NonNull;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import com.google.ar.core.Config;
import com.google.ar.core.Session;
import com.arcore.example.RotationGestureDetector;
import com.arcore.example.arcoremanager.object.ARCoreObjectDrawer;

import florent37.github.com.rxlifecycle.RxLifecycle;

public class ArCoreManager {

    private final AppCompatActivity mActivity;

    private final Session mARCoreSession;
    private final Config mDefaultConfig;
    private final Listener mListener;
    private ARCoreRenderer mARCoreRenderer;
    private GLSurfaceView mSurfaceView;
    private ObjectTouchMode touchMode = ObjectTouchMode.SCALE;

    public ArCoreManager(AppCompatActivity activity, @NonNull Listener listener) {
        this.mActivity = activity;
        this.mListener = listener;

        // Create default config, check is supported, create session from that config.
        mARCoreSession = new Session(/*context=*/activity);
        mDefaultConfig = Config.createDefaultConfig();

        if (!mARCoreSession.isSupported(mDefaultConfig)) {
            listener.onArCoreUnsupported();
        }
    }

    public void setup(final GLSurfaceView surfaceView) {
        mARCoreRenderer = new ARCoreRenderer(mActivity, mARCoreSession);

        this.mSurfaceView = surfaceView;

        // Set up renderer.
        surfaceView.setPreserveEGLContextOnPause(true);
        surfaceView.setEGLContextClientVersion(2);
        surfaceView.setEGLConfigChooser(8, 8, 8, 8, 16, 0); // Alpha used for plane blending.
        surfaceView.setRenderer(mARCoreRenderer);
        surfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);

        surfaceView.setOnTouchListener(new View.OnTouchListener() {

            private final GestureDetectorCompat mGestureDetector = new GestureDetectorCompat(mActivity, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onSingleTapUp(MotionEvent event) {
                    mARCoreRenderer.addSingleTapEvent(event);
                    return true;
                }

                @Override
                public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                    if (touchMode == ObjectTouchMode.MOVE) {
                        mARCoreRenderer.onTranslate(-distanceX / 200f, -distanceY / 200f);
                        return true;
                    } else {
                        return false;
                    }
                }
            });

            private ScaleGestureDetector mScaleDetector = new ScaleGestureDetector(mActivity, new ScaleGestureDetector.SimpleOnScaleGestureListener() {
                @Override
                public boolean onScale(ScaleGestureDetector scaleGestureDetector) {
                    if (touchMode == ObjectTouchMode.SCALE) {
                        mARCoreRenderer.onScale(scaleGestureDetector.getScaleFactor());
                        return true;
                    } else {
                        return false;
                    }
                }
            });

            private RotationGestureDetector mRotationDetector = new RotationGestureDetector(new RotationGestureDetector.OnRotationGestureListener() {
                @Override
                public void OnRotation(RotationGestureDetector rotationDetector) {
                    if (touchMode == ObjectTouchMode.ROTATE) {
                        mARCoreRenderer.onRotate(rotationDetector.getAngle());
                    }
                }
            });

            @Override
            public boolean onTouch(View view, MotionEvent event) {
                boolean res = true;
                if (touchMode == ObjectTouchMode.SCALE) {
                    res = mScaleDetector.onTouchEvent(event);
                    if (!mScaleDetector.isInProgress()) {
                        if (mGestureDetector.onTouchEvent(event)) {
                            return false;
                        }
                    }
                    return res;
                } else if (touchMode == ObjectTouchMode.ROTATE) {
                    res = mRotationDetector.onTouchEvent(event);
                    if (!res) {
                        if (mGestureDetector.onTouchEvent(event)) {
                            return false;
                        }
                    }
                } else if (touchMode == ObjectTouchMode.MOVE) {
                    if (mGestureDetector.onTouchEvent(event)) {
                        return false;
                    }
                }
                return res;
            }
        });

        subscribeOnLifeCycleEvents();

        mARCoreRenderer.setListener(() -> {
            if (mListener != null) {
                mListener.hideLoadingMessage();
            }
        });
    }

    private void subscribeOnLifeCycleEvents() {
        RxLifecycle.with(mActivity)
                .onResume()
                .subscribe(event -> {
                    mListener.showLoadingMessage();
                    mARCoreSession.resume(mDefaultConfig);
                    mSurfaceView.onResume();
                }, throwable -> {
                    throwable.printStackTrace();
                    mListener.onPermissionNotAllowed();
                });

        RxLifecycle.with(mActivity)
                .onPause()
                .subscribe(event -> {
                    mSurfaceView.onPause();
                    mARCoreSession.pause();
                });
    }

    public void addObjectToDraw(ARCoreObjectDrawer arCoreObjectDrawer) {
        mARCoreRenderer.addObjectToDraw(arCoreObjectDrawer);
    }

    public void setTouchMode(ObjectTouchMode objectTouchMode) {
        this.touchMode = objectTouchMode;
    }

    public void onClearScreen() {
        // TODO: 1/21/18
        mARCoreRenderer.onClearScreen();
    }

    public enum ObjectTouchMode {
        SCALE,
        ROTATE,
        MOVE
    }

    public interface Listener {
        void onArCoreUnsupported();

        void onPermissionNotAllowed();

        void showLoadingMessage();

        void hideLoadingMessage();
    }
}

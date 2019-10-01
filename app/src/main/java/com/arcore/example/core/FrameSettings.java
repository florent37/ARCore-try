package com.arcore.example.core;

import com.google.ar.core.Frame;

public class FrameSettings {
    private Frame mARCoreFrame;
    private float[] mCameraMatrix;
    private float[] mProjMatrix;
    private float mLightIntensity;
    private float mWidth;
    private float mHeight;

    public FrameSettings() {

    }

    public void setARCoreFrame(Frame arCoreFrame) {
        this.mARCoreFrame = arCoreFrame;
    }

    public void setCameraMatrix(float[] cameraMatrix) {
        this.mCameraMatrix = cameraMatrix;
    }

    public void setProjMatrix(float[] projMatrix) {
        this.mProjMatrix = projMatrix;
    }

    public void setLightIntensity(float lightIntensity) {
        this.mLightIntensity = lightIntensity;
    }

    public Frame getARCoreFrame() {
        return mARCoreFrame;
    }

    public float[] getCameraMatrix() {
        return mCameraMatrix;
    }

    public float[] getProjMatrix() {
        return mProjMatrix;
    }

    public float getLightIntensity() {
        return mLightIntensity;
    }

    public float getWidth() {
        return mWidth;
    }

    public void setWidth(float width) {
        this.mWidth = width;
    }

    public float getHeight() {
        return mHeight;
    }

    public void setHeight(float height) {
        this.mHeight = height;
    }
}

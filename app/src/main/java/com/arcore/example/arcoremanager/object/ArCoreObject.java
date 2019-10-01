package com.arcore.example.arcoremanager.object;

import com.google.ar.core.Anchor;
import com.arcore.example.core.rendering.PlaneAttachment;

public class ArCoreObject {
    private final PlaneAttachment planeAttachment;
    private boolean mIsAnimateAppearance;
    private float mScale;
    private float mRotationY;
    private float mRotationZ;
    private float mTranslationX;
    private float mTranslationZ;

    private final static float DELTA_Z = 10f;
    private final static float DELTA_X = 4f;
    private final static int DOUBLE_FRAMES = 2;
    private final static int CAMERA_MAX_FRAMES = 30;
    private final static int MAX_FRAMES = CAMERA_MAX_FRAMES * DOUBLE_FRAMES;
    private int mCurrentFrame = 0;

    public ArCoreObject(PlaneAttachment planeAttachment, boolean needAnimate) {
        this.planeAttachment = planeAttachment;
        mIsAnimateAppearance = needAnimate;
        if (mIsAnimateAppearance) {
            mTranslationZ -= DELTA_Z;
            mTranslationX += DELTA_X;
        }
        this.mScale = 1f;
    }

    public Anchor getAnchor() {
        return planeAttachment.getAnchor();
    }

    public PlaneAttachment getPlaneAttachment() {
        return planeAttachment;
    }

    public float getScale() {
        return mScale;
    }

    public void setScale(float scale) {
        this.mScale = scale;
    }

    public void setRotation(float angle) {
        this.mRotationY = angle;
    }

    public float getRotationY() {
        return mRotationY;
    }

    public void setTranslation(float distanceX, float distanceZ) {
        mTranslationX += distanceX;
        mTranslationZ += distanceZ;
    }

    public float getTranslationX() {
        if (mIsAnimateAppearance) {
            mCurrentFrame++;
            return mTranslationX -= DELTA_X / CAMERA_MAX_FRAMES;
        } else {
            return mTranslationX;
        }
    }

    public float getTranslationZ() {
        if (mIsAnimateAppearance) {
            mCurrentFrame++;
            checkFrames();
            return mTranslationZ += DELTA_Z / CAMERA_MAX_FRAMES;
        } else {
            return mTranslationZ;
        }
    }

    private void checkFrames() {
        if (mCurrentFrame >= MAX_FRAMES) {
            mIsAnimateAppearance = false;
        }
    }

    public boolean isTracking() {
        return planeAttachment.isTracking();
    }

    public void toMatrix(float[] anchorMatrix) {
        planeAttachment.getPose().toMatrix(anchorMatrix, 0);
    }

    public float getRotationZ() {
        return mRotationZ;
    }

    public void setRotationZ(float mRotationZ) {
        this.mRotationZ = mRotationZ;
    }
}

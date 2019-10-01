package com.arcore.example.arcoremanager.object;

import com.arcore.example.core.FrameSettings;

public class FerrariObjectDrawer extends SimpleArCoreObjectDrawer {

    private static final String TAG = FerrariObjectDrawer.class.getSimpleName();
    private boolean mIsFirstDraw = true;

    public FerrariObjectDrawer() {
        super("california.obj");
        setParentDirectory("ferr/");
        setRotationZ(-90);
    }

    @Override
    public void onDraw(FrameSettings settings) {
        // Visualize anchors created by touch.
        for (ArCoreObject object : positions) {
            if (object.isTracking()) {
                continue;
            }
            object.toMatrix(mAnchorMatrix);
            if (mIsFirstDraw && object.getRotationY() == 0) {
                object.setRotation(-32);
                mIsFirstDraw = false;
            }

            // Update and draw the model and its shadow.
            super.objectRenderer.updateModelMatrix(mAnchorMatrix, object.getScale(), object.getRotationY(), getRotationZ(), object.getTranslationX(), object.getTranslationZ());
            super.objectRenderer.draw(settings.getCameraMatrix(), settings.getProjMatrix(), settings.getLightIntensity());
        }
    }
}

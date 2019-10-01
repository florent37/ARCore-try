package com.arcore.example.arcoremanager.object;

import com.arcore.example.core.FrameSettings;

public class CastleObjectDrawer extends SimpleArCoreObjectDrawer {

    private static final String TAG = "AndyObjectDrawer";

    public CastleObjectDrawer() {
        super("Castle OBJ.obj");
        setParentDirectory("castle/");
    }


    @Override
    public void onDraw(FrameSettings settings) {
        // Visualize anchors created by touch.
        for (ArCoreObject object : positions) {
            if (object.isTracking()) {
                continue;
            }
            object.toMatrix(mAnchorMatrix);

            // Update and draw the model and its shadow.
//          should be instead of mScale  object.getScale()
            super.objectRenderer.updateModelMatrix(mAnchorMatrix, object.getScale(), object.getRotationY(), object.getRotationZ(), object.getTranslationX(), object.getTranslationZ());
            super.objectRenderer.draw(settings.getCameraMatrix(), settings.getProjMatrix(), settings.getLightIntensity());
        }
    }
}

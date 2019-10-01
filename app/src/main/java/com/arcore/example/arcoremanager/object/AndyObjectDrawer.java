package com.arcore.example.arcoremanager.object;

import android.content.Context;
import android.util.Log;

import com.arcore.example.core.FrameSettings;
import com.arcore.example.core.rendering.ComplexObjectRenderer;
import com.arcore.example.core.rendering.ObjectRenderer;

import java.io.IOException;

public class AndyObjectDrawer extends SimpleArCoreObjectDrawer {
    private static final String TAG = "AndyObjectDrawer";
    private final ComplexObjectRenderer androidObjectShadow = new ComplexObjectRenderer();

    public AndyObjectDrawer() {
        super("andy.obj", "andy.png");
        setParentDirectory("");
    }

    @Override
    public void onDraw(FrameSettings settings) {
        // Visualize anchors created by touch.
        for (ArCoreObject bugDroid : positions) {
            if (!bugDroid.isTracking()) {
                continue;
            }
            // Get the current combined pose of an Anchor and Plane in world space. The Anchor
            // and Plane poses are updated during calls to session.update() as ARCore refines
            // its estimate of the world.
            bugDroid.toMatrix(mAnchorMatrix);

            // Update and draw the model and its shadow.
            super.objectRenderer.updateModelMatrix(mAnchorMatrix, bugDroid.getScale(), bugDroid.getRotationY(), bugDroid.getRotationZ(), bugDroid.getTranslationX(), bugDroid.getTranslationZ());
            androidObjectShadow.updateModelMatrix(mAnchorMatrix, bugDroid.getScale(), bugDroid.getRotationY(), bugDroid.getRotationZ(), bugDroid.getTranslationX(), bugDroid.getTranslationZ());
            super.objectRenderer.draw(settings.getCameraMatrix(), settings.getProjMatrix(), settings.getLightIntensity());
            androidObjectShadow.draw(settings.getCameraMatrix(), settings.getProjMatrix(), settings.getLightIntensity());
        }
    }

    @Override
    public void prepare(Context context) {
        super.prepare(context); //draw the droid on super
        // Prepare the other rendering objects.
        try {
            androidObjectShadow.createOnGlThread(/*context=*/context, "andy_shadow.obj", "andy_shadow.png");
            androidObjectShadow.setBlendMode(ObjectRenderer.BlendMode.Shadow);
            androidObjectShadow.setMaterialProperties(1.0f, 0.0f, 0.0f, 1.0f);
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "Failed to read obj file");
        }
    }
}

package com.arcore.example.arcoremanager.object;

import android.content.Context;
import android.util.Log;

import com.arcore.example.core.rendering.ComplexObjectRenderer;
import com.google.ar.core.Anchor;
import com.google.ar.core.PlaneHitResult;
import com.google.ar.core.Session;
import com.arcore.example.core.FrameSettings;
import com.arcore.example.core.rendering.PlaneAttachment;
import com.google.ar.core.exceptions.NotTrackingException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class SimpleArCoreObjectDrawer implements ARCoreObjectDrawer {

    private static final String TAG = SimpleArCoreObjectDrawer.class.getSimpleName();

    public final List<ArCoreObject> positions = new ArrayList<>();
    private final int MAX_OBJECTS_ON_SCREEN = 10;
    //the 3D object
    protected final ComplexObjectRenderer objectRenderer = new ComplexObjectRenderer();
    // Temporary matrix allocated here to reduce number of allocations for each frame.
    protected final float[] mAnchorMatrix = new float[16];
    private final String mObjFile;
    private final String mObjTextureAsset;
    private float mRotationZ;

    public SimpleArCoreObjectDrawer(String objFile, String objTextureAsset) {
        this.mObjFile = objFile;
        this.mObjTextureAsset = objTextureAsset;
    }

    public SimpleArCoreObjectDrawer(String objFile) {
        this.mObjFile = objFile;
        this.mObjTextureAsset = "";
    }

    protected void setParentDirectory(String parentDir) {
        objectRenderer.setParentDirectory(parentDir);
    }

    @Override
    public void addPlaneAttachment(PlaneHitResult planeHitResult, Session arCoreSession) throws NotTrackingException {

        // Cap the number of objects created. This avoids overloading both the
        // rendering system and ARCore.
        if (positions.size() >= MAX_OBJECTS_ON_SCREEN) {
            arCoreSession.removeAnchors(Arrays.asList(positions.get(0).getAnchor()));
            positions.remove(0);
        }
        // Adding an Anchor tells ARCore that it should track this position in
        // space. This anchor will be used in PlaneAttachment to place the 3d model
        // in the correct position relative both to the world and to the plane.
        positions.add(new ArCoreObject(new PlaneAttachment(planeHitResult.getPlane(), arCoreSession.addAnchor(planeHitResult.getHitPose())), true));

        // Hits are sorted by depth. Consider only closest hit on a plane.
    }

    @Override
    public void setScaleFactor(float scaleFactor) {
        if (!positions.isEmpty()) {
            final ArCoreObject last = positions.get(positions.size() - 1);
            last.setScale(last.getScale() * scaleFactor);
        }
    }

    @Override
    public void rotate(float angle) {
        if (!positions.isEmpty()) {
            final ArCoreObject last = positions.get(positions.size() - 1);
            last.setRotation(angle);
        }
    }

    @Override
    public void translate(float distanceX, float distanceY) {
        if (!positions.isEmpty()) {
            final ArCoreObject last = positions.get(positions.size() - 1);
            last.setTranslation(distanceX, distanceY);
        }
    }

    @Override
    public Collection<Anchor> getAnchors() {
        List<Anchor> anchors = new ArrayList<Anchor>();
        for (ArCoreObject object : positions) {
            anchors.add(object.getAnchor());
        }
        return anchors;
    }

    @Override
    public void clearList() {
        positions.clear();
    }

    @Override
    public void onDraw(FrameSettings settings) {
        // Visualize anchors created by touch.
        for (ArCoreObject arCoreObject : positions) {
            if (arCoreObject.isTracking()) {
                continue;
            }

            drawObject(settings, arCoreObject);
        }
    }

    protected void drawObject(FrameSettings settings, ArCoreObject arCoreObject) {
        // Get the current combined pose of an Anchor and Plane in world space. The Anchor
        // and Plane poses are updated during calls to session.update() as ARCore refines
        // its estimate of the world.
        arCoreObject.getPlaneAttachment().getPose().toMatrix(mAnchorMatrix, 0);

        // Update and draw the model and its shadow.
        objectRenderer.updateModelMatrix(mAnchorMatrix, arCoreObject.getScale(), arCoreObject.getRotationY(), arCoreObject.getRotationZ(), arCoreObject.getTranslationX(), arCoreObject.getTranslationZ());
        objectRenderer.draw(settings.getCameraMatrix(), settings.getProjMatrix(), settings.getLightIntensity());
    }

    @Override
    public void prepare(Context context) {
        // Prepare the other rendering objects.
        try {
            objectRenderer.createOnGlThread(/*context=*/context, mObjFile, mObjTextureAsset);
            objectRenderer.setMaterialProperties(0.0f, 3.5f, 1.0f, 6.0f);
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "Failed to read obj file");
        }
    }

    protected float getRotationZ() {
        return mRotationZ;
    }

    protected void setRotationZ(float rotationZ) {
        this.mRotationZ = rotationZ;
    }

}

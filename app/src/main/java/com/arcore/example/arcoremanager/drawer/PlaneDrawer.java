package com.arcore.example.arcoremanager.drawer;

import android.content.Context;
import android.util.Log;

import com.google.ar.core.Session;
import com.arcore.example.core.FrameSettings;
import com.arcore.example.core.rendering.PlaneRenderer;

import java.io.IOException;

// Visualize planes.
public class PlaneDrawer implements Drawer {
    private final static String TAG = "PlaneDrawer";

    //will distplay triangles on the plane
    private final PlaneRenderer plane = new PlaneRenderer();

    private final Session mArCoreSession;

    public PlaneDrawer(Session arcoreSession) {
        this.mArCoreSession = arcoreSession;
    }

    @Override
    public void prepare(Context context) {
        try {
            plane.createOnGlThread(context, "trigrid.png");
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "Failed to read plane texture");
        }
    }

    @Override
    public void onDraw(FrameSettings arCanvas) {
        plane.drawPlanes(mArCoreSession.getAllPlanes(), arCanvas.getARCoreFrame().getPose(), arCanvas.getProjMatrix());
    }
}

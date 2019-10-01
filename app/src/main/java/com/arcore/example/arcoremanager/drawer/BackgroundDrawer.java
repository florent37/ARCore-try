package com.arcore.example.arcoremanager.drawer;

import android.content.Context;

import com.google.ar.core.Session;
import com.arcore.example.core.FrameSettings;
import com.arcore.example.core.rendering.BackgroundRenderer;

public class BackgroundDrawer implements Drawer {
    //the background / camera display
    public BackgroundRenderer background = new BackgroundRenderer();

    private final Session mArCoreSession;

    public BackgroundDrawer(Session mArCoreSession) {
        this.mArCoreSession = mArCoreSession;
    }


    @Override
    public void prepare(Context context) {
        background.createOnGlThread(/*context=*/context);

        mArCoreSession.setCameraTextureName(background.getTextureId());
    }

    @Override
    public void onDraw(FrameSettings arCanvas) {
        background.draw(arCanvas.getARCoreFrame());
    }
}

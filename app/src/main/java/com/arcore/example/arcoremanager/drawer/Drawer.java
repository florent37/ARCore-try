package com.arcore.example.arcoremanager.drawer;

import android.content.Context;

import com.arcore.example.core.FrameSettings;

public interface Drawer {
    void prepare(Context context);
    void onDraw(FrameSettings arCanvas);
}

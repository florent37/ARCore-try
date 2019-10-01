package com.arcore.example.arcoremanager.object;

import com.google.ar.core.Anchor;
import com.google.ar.core.PlaneHitResult;
import com.google.ar.core.Session;
import com.arcore.example.arcoremanager.drawer.Drawer;
import com.google.ar.core.exceptions.NotTrackingException;

import java.util.Collection;

public interface ARCoreObjectDrawer extends Drawer {

    void addPlaneAttachment(PlaneHitResult planeHitResult, Session arCoreSession) throws NotTrackingException;

    void setScaleFactor(float scaleFactor);

    void rotate(float angle);

    void translate(float distanceX, float distanceY);

    Collection<Anchor> getAnchors();

    void clearList();
}

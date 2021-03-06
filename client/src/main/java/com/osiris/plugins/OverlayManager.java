package com.osiris.plugins;

import java.util.ArrayList;
import java.util.List;


public class OverlayManager {
    public static List<Overlay> overlays = new ArrayList<>();

    public static void registerOverlay(Overlay overlay)
    {
        overlays.add(overlay);
    }
}

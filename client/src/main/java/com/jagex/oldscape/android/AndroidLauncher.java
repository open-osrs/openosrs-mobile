package com.jagex.oldscape.android;

import android.app.NativeActivity;

import net.runelite.rs.api.RSClient;

//import net.runelite.rs.api.RSClient;

/*
    This is a stub class that allows us to interface the target activity.
    for example: Calling it using a new intent, and using getClient()'s injected instance
 */
public class AndroidLauncher extends NativeActivity {
    //Static client instance
    public static RSClient getClient()
    {
        return null;
    }
}

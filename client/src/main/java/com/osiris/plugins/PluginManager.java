package com.osiris.plugins;

import com.osiris.MainActivity;
import com.osiris.plugins.grounditems.GroundItemsPlugin;


public class PluginManager {
    public static GroundItemsPlugin groundItemsPlugin = new GroundItemsPlugin();

    public static void registerPlugins()
    {
        MainActivity.client.getEventBus().register(groundItemsPlugin);
    }
}

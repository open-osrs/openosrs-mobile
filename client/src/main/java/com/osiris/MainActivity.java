/*
Copyright 2020 Null(zeruth)

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the
documentation and/or other materials provided with the distribution.

3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products derived from this
software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE
USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.osiris;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.jagex.oldscape.android.AndroidLauncher;
import com.osiris.api.ItemDefinitionManager;
import com.osiris.game.ItemManager;
import com.osiris.plugins.PluginManager;
import com.osiris.plugins.grounditems.GroundItemsPlugin;
import com.osiris.util.ExecutorServiceExceptionLogger;


import net.runelite.api.Tile;
import net.runelite.api.TileItem;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.ItemDespawned;
import net.runelite.api.events.ItemQuantityChanged;
import net.runelite.api.events.ItemSpawned;
import net.runelite.api.events.PostItemComposition;
import net.runelite.eventbus.Subscribe;
import net.runelite.http.api.RuneLiteAPI;
import net.runelite.http.api.item.ItemClient;
import net.runelite.rs.api.RSClient;

import java.io.IOException;
import java.io.InputStream;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN;
import static android.view.WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
import static android.view.WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
import static android.view.WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;

/*
    This class handles loading OSRS as well as initializing any of our added features.

    Keep all mobile required classes in the com.osiris package, as the patcher will only
    move that package to osrs mobile.
 */
public class MainActivity extends Activity {
    public String MAIN_ACTIVITY = this.getClass().getName();
    private String version = "0.1.0";
    static int gameTicks = 0;
    public static RSClient client;
    public static SurfaceHolder surfaceHolder;
    private WindowManager wm;
    private ImageView overlayView;
    public static Bitmap overlayBitmap;
    Canvas canvas;
    Paint paint;
    Typeface rsRegular;
    Typeface rsSmall;
    Paint wallpaint;
    boolean isRendering = false;
    public static String[] debug = new String[10];
    Paint fishingSpotPaint = new Paint();
    private int screenWidth;
    private int screenHeight;
    Map<Tile, List<TileItem>> groundItems = new HashMap<>();
    public static ScheduledExecutorService executor = new ExecutorServiceExceptionLogger(Executors.newSingleThreadScheduledExecutor());
    public static ItemManager itemManager;
    public static InputStream itemVariations;


    //private final Map<WorldPoint, Integer> offsetMap = new HashMap<>();

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(500, 500, WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY, FLAG_NOT_TOUCH_MODAL, PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.START | Gravity.TOP;
        params.x = 0;
        params.y = 0;
        params.alpha = 255;
        wm.updateViewLayout(overlayView, params);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!Settings.canDrawOverlays(getApplicationContext()))
        {
            startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION));
        }
        if (!Settings.canDrawOverlays(getApplicationContext()))
        {
            Toast.makeText(this, "Overlay permission required for OSiris. Exiting...", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            itemVariations = this.getResources().getAssets().open("item_variations.json");
        } catch (IOException e) {
            e.printStackTrace();
        }
        rsRegular = Typeface.createFromAsset(this.getResources().getAssets(), "runescape.ttf");
        rsSmall = Typeface.createFromAsset(this.getResources().getAssets(), "runescape_small.ttf");

        ItemDefinitionManager.init();

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        screenWidth = displayMetrics.widthPixels + getNavBarWidth();
        screenHeight = displayMetrics.heightPixels;


        Intent intent = new Intent(this, AndroidLauncher.class);

        //OSRS has checks for being task root so we must use these flags testing
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        startActivity(intent);

        Thread t1 = new Thread(() -> {
            while (client == null)
            {
                client = AndroidLauncher.getClient();
                if (client != null)
                {
                    client.getEventBus().register(this);
                    PluginManager.registerPlugins();
                    Log.e(MAIN_ACTIVITY, "EventBus Registered");
                    itemManager = new ItemManager(client, executor, RuneLiteAPI.CLIENT);
                }
            }
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        t1.start();

        wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(screenWidth, screenHeight, WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY, FLAG_NOT_TOUCHABLE | FLAG_NOT_FOCUSABLE | FLAG_NOT_TOUCH_MODAL | FLAG_FULLSCREEN, PixelFormat.OPAQUE);
        params.gravity = Gravity.START | Gravity.TOP;
        params.x = 0;
        params.y = 0;
        params.alpha = 255;
        overlayView = new ImageView(this);
        overlayView.setAlpha(1.0f);

        overlayBitmap = Bitmap.createBitmap(screenWidth,
                screenHeight, Bitmap.Config.ARGB_8888);
        overlayBitmap.eraseColor(Color.TRANSPARENT);
        overlayBitmap = drawTextToBitmap(overlayBitmap, "OSiris", 0, 0);
        overlayView.setImageBitmap(overlayBitmap);
        overlayView.setAlpha(1.0f);
        overlayView.setClickable(false);
        overlayView.setFocusableInTouchMode(false);
        wm.addView(overlayView, params);

        wallpaint = new Paint();
        wallpaint.setColor(Color.GRAY);
        wallpaint.setAlpha(60);
        wallpaint.setStyle(Paint.Style.FILL);
        Thread overlayThread = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(20);
                    if (!isRendering)
                        updateOverlay();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        overlayThread.start();

        Toast.makeText(this,
                "Welcome to OSiris!", Toast.LENGTH_LONG).show();
    }

    @Subscribe
    public void onPostItemComposition(PostItemComposition event)
    {
        Log.e(MAIN_ACTIVITY, event.getItemComposition().getName() + " composition posted!");
    }

    @Subscribe
    public void onGameTick(GameTick event)
    {
        Log.e(MAIN_ACTIVITY, "onGameTick!");
    }

    @Subscribe
    public void onItemDespawned(ItemDespawned event)
    {
        groundItems.get(event.getTile()).add(event.getItem());
        if (groundItems.get(event.getTile()).size() == 0)
            groundItems.remove(event.getTile());
        Log.e(MAIN_ACTIVITY, itemManager.getItemComposition(event.getItem().getId()).getName() + " Despawned");
    }

    @Subscribe
    public void onItemQuantityChanged(ItemQuantityChanged event)
    {
        groundItems.get(event.getTile()).remove(event.getItem());
        groundItems.get(event.getTile()).add(event.getItem());
        Log.e(MAIN_ACTIVITY, itemManager.getItemComposition(event.getItem().getId()).getName() + " Quantity Changed");
    }

    @Subscribe
    public void onItemSpawned(ItemSpawned event)
    {
        if (!groundItems.containsKey(event.getTile()))
        {
            List<TileItem> tileItemList = new ArrayList<>();
            tileItemList.add(event.getItem());
            groundItems.put(event.getTile(), tileItemList);
        }
        else groundItems.get(event.getTile()).add(event.getItem());
        String alchValue = NumberFormat.getNumberInstance(Locale.US).format(
                client.getItemComposition(event.getItem().getId()).getPrice() * event.getItem().getQuantity());
        String geValue = NumberFormat.getNumberInstance(Locale.US).format(
                itemManager.getItemPrice(event.getItem().getId())  * event.getItem().getQuantity());
        Log.e(MAIN_ACTIVITY, itemManager.getItemComposition(event.getItem().getId()).getName()
                + " x" + event.getItem().getQuantity()
                + " Spawned, worth - (HA:" + alchValue
                + ")(GE:" + geValue + ")");
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void updateOverlay()
    {
        runOnUiThread(() ->
        {
            isRendering = true;
            boolean erased = false;


            overlayBitmap.eraseColor(Color.TRANSPARENT);
            overlayBitmap = drawTextToBitmap(overlayBitmap, "OSiris", 0, 0);
            overlayBitmap = drawTextToBitmap(overlayBitmap, version, 0, 14 * 4);
            debug[0] = "ItemPrices: " + ItemManager.itemPrices.size();

            int i = 0;
            for (String s : debug)
            {
                if (s != null)
                {
                    overlayBitmap = drawTextToBitmap(overlayBitmap, "d: " + s, 0, 14 * 4 * (i + 2));
                    i++;
                }
            }
            overlayView.setImageBitmap(overlayBitmap);
            isRendering = false;
        });
    }

    public Bitmap drawTextToBitmap(Bitmap bitmap, String text, int x, int y) {
        if (canvas == null)
        canvas = new Canvas(bitmap);
        // new antialised Paint
        if (paint == null)
            paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        // text color - #3D3D3D
        paint.setColor(Color.rgb(61, 61, 61));
        // text size in pixels
        paint.setTextSize(14 * 4);
        paint.setTypeface(rsRegular);
        // text shadow
        paint.setShadowLayer(1f, 0f, 1f, Color.WHITE);
        // draw text to the Canvas center
        Rect bounds = new Rect();
        paint.getTextBounds(text, 0, text.length(), bounds);
        canvas.drawText(text, -bounds.left + x, -bounds.top + y, paint);

        return bitmap;
    }

    public Bitmap drawTextToBitmapWhite(Bitmap bitmap, String text, int x, int y) {
        if (canvas == null)
            canvas = new Canvas(bitmap);
        // new antialised Paint
        if (paint == null)
            paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        // text color - #3D3D3D
        paint.setColor(Color.rgb(255, 255, 255));
        // text size in pixels
        paint.setTextSize(14 * 4);

        paint.setTypeface(rsSmall);
        // text shadow
        paint.setShadowLayer(5f, 5f, 5f, Color.BLACK);

        // draw text to the Canvas center
        Rect bounds = new Rect();
        paint.getTextBounds(text, 0, text.length(), bounds);

        canvas.drawText(text, x - (float)bounds.width() / 2, -bounds.top + y, paint);

        return bitmap;
    }

    public Bitmap drawTextToBitmapYellow(Bitmap bitmap, String text, int x, int y) {
        if (canvas == null)
            canvas = new Canvas(bitmap);
        // new antialised Paint
        if (paint == null)
            paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        // text color - #3D3D3D
        paint.setColor(Color.rgb(255, 255, 0));
        // text size in pixels
        paint.setTextSize(14 * 4);

        paint.setTypeface(rsSmall);
        // text shadow
        paint.setShadowLayer(5f, 5f, 5f, Color.BLACK);

        // draw text to the Canvas center
        Rect bounds = new Rect();
        paint.getTextBounds(text, 0, text.length(), bounds);

        canvas.drawText(text, x - (float)bounds.width() / 2, -bounds.top + y, paint);

        return bitmap;
    }

    public int getNavBarWidth()
    {
        Resources resources = getApplicationContext().getResources();
        int resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId > 0) {
            return resources.getDimensionPixelSize(resourceId);
        }
        return 0;
    }
}
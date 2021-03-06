package com.osiris.plugins.grounditems;

import android.graphics.Bitmap;
import android.graphics.Color;

import com.osiris.MainActivity;
import com.osiris.game.ItemManager;
import com.osiris.plugins.Overlay;
import com.osiris.util.PaintUtil;

import net.runelite.api.Client;
import net.runelite.api.Perspective;
import net.runelite.api.Point;
import net.runelite.api.Tile;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class GroundItemsOverlay extends Overlay {

    public static Map<WorldPoint, Integer> itemPiles = new HashMap<>();

    @Override
    public void paint(Bitmap overlay)
    {
        Client client = MainActivity.client;
        ItemManager itemManager = MainActivity.itemManager;
        int alchValue;
        int geValue;
        Color insane = Color.valueOf(Color.rgb(255, 102, 178));
        int insaneValue = 10000000;
        Color high = Color.valueOf(Color.rgb(255, 150, 0));
        int highValue = 1000000;
        Color medium = Color.valueOf(Color.rgb(153, 255, 153));
        int mediumValue = 100000;
        Color low = Color.valueOf(Color.rgb(102, 178, 255));
        int lowValue = 10000;

        String alchValueText;
        String geValueText;

        String text;
        Point p;

        if (client == null || itemManager == null)
            return;

        List<GroundItem> itemsToDraw = new ArrayList<>(GroundItemsPlugin.collectedGroundItems.values());
        for (GroundItem groundItem : itemsToDraw)
        {
            WorldPoint w = groundItem.getLocation();
            int i;
            if (itemPiles.containsKey(w))
                i = itemPiles.get(w);
            else
                i = 0;


            alchValue = client.getItemComposition(groundItem.getId()).getPrice() * groundItem.getQuantity();
            geValue = itemManager.getItemPrice(groundItem.getId())  * groundItem.getQuantity();
            alchValueText = NumberFormat.getNumberInstance(Locale.US).format(alchValue);
            geValueText = NumberFormat.getNumberInstance(Locale.US).format(geValue);

            Color textColor = Color.valueOf(Color.WHITE);
            if (alchValue > insaneValue || geValue > insaneValue)
                textColor = insane;
            else if (alchValue > highValue || geValue > highValue)
                textColor = high;
            else if (alchValue > mediumValue || geValue > mediumValue)
                textColor = medium;
            else if (alchValue > lowValue || geValue > lowValue)
                textColor = low;

            text = itemManager.getItemComposition(groundItem.getId()).getName()
                    + " x" + groundItem.getQuantity()
                    + " - (HA:" + alchValueText
                    + ") (GE:" + geValueText + ")";
            p = Perspective.getCanvasTextLocation(client, LocalPoint.fromWorld(client, groundItem.getLocation()), text, 0);
            if (p!= null)
                PaintUtil.drawTextToBitmap(overlay, textColor, text, p.getX(), p.getY() - i);

            itemPiles.put(w, i + 40);
        }
        itemPiles.clear();
    }
}

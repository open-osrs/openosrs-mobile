package com.osiris.plugins.grounditems;

import com.google.common.cache.LoadingCache;
import com.google.common.collect.EvictingQueue;
import com.google.common.collect.ImmutableList;
import com.osiris.MainActivity;
import com.osiris.plugins.Plugin;

import net.runelite.api.GameState;
import net.runelite.api.ItemComposition;
import net.runelite.api.ItemID;
import net.runelite.api.Tile;
import net.runelite.api.TileItem;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.ItemDespawned;
import net.runelite.api.events.ItemQuantityChanged;
import net.runelite.api.events.ItemSpawned;
import net.runelite.eventbus.Subscribe;
import net.runelite.rs.api.RSClient;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Queue;

import lombok.Getter;

public class GroundItemsPlugin extends Plugin {

    public RSClient client = MainActivity.client;

    // The game won't send anything higher than this value to the plugin -
    // so we replace any item quantity higher with "Lots" instead.
    static final int MAX_QUANTITY = 65535;
    // ItemID for coins
    private static final int COINS = ItemID.COINS_995;
    private int lastUsedItem;

    @Getter
    private final Map<GroundItem.GroundItemKey, GroundItem> collectedGroundItems = new LinkedHashMap<>();
    private final Queue<Integer> droppedItemQueue = EvictingQueue.create(16); // recently dropped items

    @Subscribe
    public void onGameStateChanged(final GameStateChanged event)
    {
        if (event.getGameState() == GameState.LOADING)
        {
            collectedGroundItems.clear();
        }
    }

    @Subscribe
    public void onItemSpawned(ItemSpawned itemSpawned)
    {
        TileItem item = itemSpawned.getItem();
        Tile tile = itemSpawned.getTile();

        GroundItem groundItem = buildGroundItem(tile, item);

        GroundItem.GroundItemKey groundItemKey = new GroundItem.GroundItemKey(item.getId(), tile.getWorldLocation());
        GroundItem existing = collectedGroundItems.putIfAbsent(groundItemKey, groundItem);
        if (existing != null)
        {
            existing.setQuantity(existing.getQuantity() + groundItem.getQuantity());
            // The spawn time remains set at the oldest spawn
        }
    }

    @Subscribe
    public void onItemDespawned(ItemDespawned itemDespawned)
    {
        TileItem item = itemDespawned.getItem();
        Tile tile = itemDespawned.getTile();

        GroundItem.GroundItemKey groundItemKey = new GroundItem.GroundItemKey(item.getId(), tile.getWorldLocation());
        GroundItem groundItem = collectedGroundItems.get(groundItemKey);
        if (groundItem == null)
        {
            return;
        }

        if (groundItem.getQuantity() <= item.getQuantity())
        {
            collectedGroundItems.remove(groundItemKey);
        }
        else
        {
            groundItem.setQuantity(groundItem.getQuantity() - item.getQuantity());
            // When picking up an item when multiple stacks appear on the ground,
            // it is not known which item is picked up, so we invalidate the spawn
            // time
            groundItem.setSpawnTime(null);
        }
    }

    @Subscribe
    public void onItemQuantityChanged(ItemQuantityChanged itemQuantityChanged)
    {
        TileItem item = itemQuantityChanged.getItem();
        Tile tile = itemQuantityChanged.getTile();
        int oldQuantity = itemQuantityChanged.getOldQuantity();
        int newQuantity = itemQuantityChanged.getNewQuantity();

        int diff = newQuantity - oldQuantity;
        GroundItem.GroundItemKey groundItemKey = new GroundItem.GroundItemKey(item.getId(), tile.getWorldLocation());
        GroundItem groundItem = collectedGroundItems.get(groundItemKey);
        if (groundItem != null)
        {
            groundItem.setQuantity(groundItem.getQuantity() + diff);
        }
    }

    private GroundItem buildGroundItem(final Tile tile, final TileItem item)
    {
        // Collect the data for the item
        final int itemId = item.getId();
        final ItemComposition itemComposition = MainActivity.itemManager.getItemComposition(itemId);
        final int realItemId = itemComposition.getNote() != -1 ? itemComposition.getLinkedNoteId() : itemId;
        final int alchPrice = itemComposition.getHaPrice();
        final boolean dropped = tile.getWorldLocation().equals(client.getLocalPlayer().getWorldLocation()) && droppedItemQueue.remove(itemId);
        final boolean table = itemId == lastUsedItem && tile.getItemLayer().getHeight() > 0;

        final GroundItem groundItem = GroundItem.builder()
                .id(itemId)
                .location(tile.getWorldLocation())
                .itemId(realItemId)
                .quantity(item.getQuantity())
                .name(itemComposition.getName())
                .haPrice(alchPrice)
                .height(tile.getItemLayer().getHeight())
                .tradeable(itemComposition.isTradeable())
                .lootType(dropped ? LootType.DROPPED : (table ? LootType.TABLE : LootType.UNKNOWN))
                .spawnTime(Instant.now())
                .stackable(itemComposition.isStackable())
                .build();

        // Update item price in case it is coins
        if (realItemId == COINS)
        {
            groundItem.setHaPrice(1);
            groundItem.setGePrice(1);
        }
        else
        {
            groundItem.setGePrice(MainActivity.itemManager.getItemPrice(realItemId));
        }

        return groundItem;
    }
}

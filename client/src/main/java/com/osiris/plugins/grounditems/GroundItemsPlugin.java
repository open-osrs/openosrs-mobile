package com.osiris.plugins.grounditems;

import com.google.common.cache.LoadingCache;
import com.google.common.collect.EvictingQueue;
import com.google.common.collect.ImmutableList;
import com.osiris.MainActivity;

import net.runelite.api.GameState;
import net.runelite.api.ItemComposition;
import net.runelite.api.Tile;
import net.runelite.api.TileItem;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.ItemSpawned;
import net.runelite.eventbus.Subscribe;
import net.runelite.rs.api.RSClient;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Queue;

import lombok.Getter;

public class GroundItemsPlugin {

    public RSClient client = MainActivity.client;

    @Getter
    private final Map<GroundItem.GroundItemKey, GroundItem> collectedGroundItems = new LinkedHashMap<>();
    private LoadingCache<NamedQuantity, Boolean> highlightedItems;
    private LoadingCache<NamedQuantity, Boolean> hiddenItems;
    private final Queue<Integer> droppedItemQueue = EvictingQueue.create(16); // recently dropped items
    private int lastUsedItem;

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

    private GroundItem buildGroundItem(final Tile tile, final TileItem item)
    {
        // Collect the data for the item
        final int itemId = item.getId();
        final ItemComposition itemComposition = itemManager.getItemComposition(itemId);
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
            groundItem.setGePrice(itemManager.getItemPrice(realItemId));
        }

        return groundItem;
    }
}

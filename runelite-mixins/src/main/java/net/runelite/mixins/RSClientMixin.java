/*
 * Copyright (c) 2016-2017, Adam <Adam@sigterm.info>
 * Copyright (c) 2020, ThatGamerBlue <thatgamerblue@gmail.com>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.runelite.mixins;

import net.runelite.api.GameState;
import net.runelite.api.Scene;
import net.runelite.api.Tile;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.ItemSpawned;
import net.runelite.api.mixins.Copy;
import net.runelite.api.mixins.FieldHook;
import net.runelite.api.mixins.Inject;
import net.runelite.api.mixins.MethodHook;
import net.runelite.api.mixins.Mixin;
import net.runelite.api.mixins.Replace;
import net.runelite.api.mixins.Shadow;
import net.runelite.eventbus.EventBus;
import net.runelite.rs.api.RSClient;
import net.runelite.rs.api.RSItemComposition;
import net.runelite.rs.api.RSNode;
import net.runelite.rs.api.RSNodeDeque;
import net.runelite.rs.api.RSObjectComposition;
import net.runelite.rs.api.RSPacketBuffer;
import net.runelite.rs.api.RSScene;
import net.runelite.rs.api.RSTile;
import net.runelite.rs.api.RSTileItem;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mixin(RSClient.class)
public abstract class RSClientMixin implements RSClient
{
	@Shadow("client")
	private static RSClient client;

	@Inject
	public static EventBus eventBus = new EventBus();

	@Inject
	public static Logger logger = LoggerFactory.getLogger("master");

	@Inject
	public int plane;

	@Inject
	public int overlayWidth;

	@Inject
	public int overlayHeight;

	@Inject
	private String[] debugLines = new String[10];

	@Inject
	private static boolean isDrawCheapGroundItems = false;

	@Inject
	@Override
	public Logger getLogger()
	{
		return logger;
	}

	@Inject
	private static RSTileItem lastItemDespawn;

	@Inject
	@Override
	public EventBus getEventBus()
	{
		return eventBus;
	}

	@Inject
	@MethodHook("updateNPCs")
	public static void onNPCUpdate(boolean b1, RSPacketBuffer rspb, byte by)
	{
		eventBus.post(GameTick.INSTANCE);
	}

	@Inject
	@Override
	public GameState getGameState()
	{
		return GameState.of(client.getRSGameState());
	}

	@Inject
	@Override
	public RSTileItem getLastItemDespawn()
	{
		return lastItemDespawn;
	}

	@Inject
	@Override
	public void setLastItemDespawn(RSTileItem lastItemDespawn)
	{
		RSClientMixin.lastItemDespawn = lastItemDespawn;
	}

	@Inject
	@Override
	public RSObjectComposition getObjectComposition(int id)
	{
		return getObjectComposition(id, -1);
	}

	@Inject
	@Override
	public RSItemComposition getItemComposition(int id)
	{
		return getItemComposition(id, -1);
	}

	@FieldHook("gameState")
	@Inject
	public static void gameStateChanged(int idx)
	{
		GameStateChanged gameStateChange = new GameStateChanged();
		GameState gameState = client.getGameState();
		gameStateChange.setGameState(gameState);
		client.getEventBus().post(gameStateChange);

		if (gameState == GameState.LOGGED_IN)
		{
			int plane = client.getPlane();
			RSScene scene = client.getScene();
			RSTile[][][] tiles = scene.getTiles();
			RSNodeDeque[][][] allItemDeque = client.getGroundItemDeque();
			RSNodeDeque[][] planeItems = allItemDeque[plane];

			for (int x = 0; x < 104; x++)
			{
				for (int y = 0; y < 104; y++)
				{
					RSNodeDeque itemDeque = planeItems[x][y];
					if (itemDeque != null)
					{
						RSTile tile = tiles[plane][x][y];
						RSNode head = itemDeque.getSentinel();

						for (RSNode current = head.getNext(); current != head; current = current.getNext())
						{
							RSTileItem item = (RSTileItem) current;
							item.setX(x);
							item.setY(y);
							ItemSpawned event = new ItemSpawned(tile, item);
							client.getEventBus().post(event);
						}
					}
				}
			}
		}
	}

	@Inject
	@Override
	public boolean isClientThread()
	{
		return true;
	}

	@Inject
	@Override
	public int getPlane()
	{
		plane = getRSPlane();
		plane *= -660826149;
		if (plane < 0)
			return 0;
		return plane;
	}

	@Inject
	@Override
	public void setOverlayWidth(int width)
	{
		overlayWidth = width;
	}

	@Inject
	@Override
	public void setOverlayHeight(int height)
	{
		overlayHeight = height;
	}

	@Inject
	@Override
	public int getOverlayHeight()
	{
		return overlayHeight;
	}

	@Inject
	@Override
	public int getOverlayWidth()
	{
		return overlayWidth;
	}

	@Inject
	@Override
	public String[] getDebugLines()
	{
		return debugLines;
	}

	@Inject
	@Override
	public void setDebugLines(String[] debugLines)
	{
		this.debugLines = debugLines;
	}

	@Copy("doCheat")
	public static void rs$doCheat(String command, int garbage)
	{

	}

	//This is where the bubble will be toggled on and off, the entry way to plugin configuration
	@Replace("doCheat")
	public static void rl$doCheat(String command, int garbage)
	{
		//Open OSiris config bubble here
		if (command.equals("config"))
		{
			return;
		}
		else if (command.equals("items.drawcheap"))
		{
			isDrawCheapGroundItems = !isDrawCheapGroundItems;
			return;
		}
		rs$doCheat(command, garbage);
	}

	@Inject
	@Override
	public boolean drawCheapGroundItems()
	{
		return isDrawCheapGroundItems;
	}
}


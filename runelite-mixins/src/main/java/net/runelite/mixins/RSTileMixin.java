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

import net.runelite.api.Constants;
import net.runelite.api.GameState;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.ItemDespawned;
import net.runelite.api.events.ItemSpawned;
import net.runelite.api.mixins.FieldHook;
import net.runelite.api.mixins.Inject;
import net.runelite.api.mixins.Mixin;
import net.runelite.api.mixins.Shadow;
import net.runelite.rs.api.RSClient;
import net.runelite.rs.api.RSItemLayer;
import net.runelite.rs.api.RSNode;
import net.runelite.rs.api.RSNodeDeque;
import net.runelite.rs.api.RSTile;
import net.runelite.rs.api.RSTileItem;

@Mixin(RSTile.class)
public abstract class RSTileMixin implements RSTile
{
	@Shadow("client")
	public static RSClient client;

	@Inject
	private static RSNodeDeque[][][] lastGroundItems = new RSNodeDeque[Constants.MAX_Z][Constants.SCENE_SIZE][Constants.SCENE_SIZE];

	@FieldHook("itemLayer")
	@Inject
	public void itemLayerChanged(int idx)
	{
		int x = getX();
		int y = getY();
		int z = client.getPlane();
		RSNodeDeque[][][] groundItemDeque = client.getGroundItemDeque();

		RSNodeDeque oldQueue = lastGroundItems[z][x][y];
		RSNodeDeque newQueue = groundItemDeque[z][x][y];
		System.out.println("test1");
		if (client.getGameState() != GameState.LOGGED_IN)
		{
			lastGroundItems[z][x][y] = newQueue;
			client.setLastItemDespawn(null);
			return;
		}
		System.out.println("test2");
		if (oldQueue != newQueue)
		{
			if (oldQueue != null)
			{
				// despawn everything in old ..
				RSNode head = oldQueue.getSentinel();
				for (RSNode cur = head.getNext(); cur != head; cur = cur.getNext())
				{
					RSTileItem item = (RSTileItem) cur;
					ItemDespawned itemDespawned = new ItemDespawned(this, item);
					client.getEventBus().post(itemDespawned);
				}
			}
			lastGroundItems[z][x][y] = newQueue;
		}

		RSTileItem lastUnlink = client.getLastItemDespawn();
		if (lastUnlink != null)
		{
			client.setLastItemDespawn(null);
		}

		RSItemLayer itemLayer = (RSItemLayer) getItemLayer();
		if (itemLayer == null)
		{
			if (lastUnlink != null)
			{
				ItemDespawned itemDespawned = new ItemDespawned(this, lastUnlink);
				client.getEventBus().post(itemDespawned);
			}
			return;
		}

		if (newQueue == null)
		{
			if (lastUnlink != null)
			{
				ItemDespawned itemDespawned = new ItemDespawned(this, lastUnlink);
				client.getEventBus().post(itemDespawned);
			}
			return;
		}

		// The new item gets added to either the head, or the tail, depending on its price
		RSNode head = newQueue.getSentinel();
		RSTileItem current = null;
		RSNode next = head.getPrevious();
		//boolean forward = false;
		if (head != next)
		{
			RSTileItem prev = (RSTileItem) next;
			if (x != prev.getX() || y != prev.getY())
			{
				current = prev;
			}
		}

		RSNode previous = head.getNext();
		if (current == null && head != previous)
		{
			RSTileItem n = (RSTileItem) previous;
			if (x != n.getX() || y != n.getY())
			{
				current = n;
				//forward = true;
			}
		}

		if (lastUnlink != null && lastUnlink != next && lastUnlink != previous)
		{
			ItemDespawned itemDespawned = new ItemDespawned(this, lastUnlink);
			client.getEventBus().post(itemDespawned);
		}

		if (current != null)
		{
			current.setX(x);
			current.setY(y);
			ItemSpawned event = new ItemSpawned(this, current);
			client.getEventBus().post(event);
		}
	}

	@Inject
	@Override
	public WorldPoint getWorldLocation()
	{
		return new WorldPoint(getX(), getY(), client.getPlane());
	}
}


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
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.mixins.FieldHook;
import net.runelite.api.mixins.Inject;
import net.runelite.api.mixins.MethodHook;
import net.runelite.api.mixins.Mixin;
import net.runelite.api.mixins.Shadow;
import net.runelite.eventbus.EventBus;
import net.runelite.rs.api.RSClient;
import net.runelite.rs.api.RSObjectComposition;
import net.runelite.rs.api.RSPacketBuffer;
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

	@FieldHook("gameState")
	// TODO at org.objectweb.asm.Frame.merge: ArrayIndexOutOfBoundsException
	public static void onGameStateChanged()
	{
		GameStateChanged event = new GameStateChanged();
		event.setGameState(client.getGameState());
		eventBus.post(event);
	}
}


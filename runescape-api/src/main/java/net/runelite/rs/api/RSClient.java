/*
 * Copyright (c) 2016-2017, Adam <Adam@sigterm.info>
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
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.runelite.rs.api;

import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.TileObject;
import net.runelite.api.mixins.Inject;
import net.runelite.eventbus.EventBus;
import net.runelite.mapping.Import;

import org.slf4j.Logger;

public interface RSClient extends Client
{
	@Override
	Logger getLogger();

	@Inject
	@Override
	GameState getGameState();

	@Import("gameState")
	int getRSGameState();

	@Override
	@Import("localPlayer")
	RSPlayer getLocalPlayer();

	@Inject
	EventBus getEventBus();

	@Import("groundItemDeque")
    RSNodeDeque[][][] getGroundItemDeque();

	@Inject
	void setLastItemDespawn(RSTileItem o);

	@Inject
	RSTileItem getLastItemDespawn();

	@Override
	@Inject
	RSObjectComposition getObjectComposition(int objectId);

	@Import("getObjectComposition")
	RSObjectComposition getObjectComposition(int objectId, int garbage);

	@Override
	@Inject
	RSItemComposition getItemComposition(int itemId);

	@Import("ItemComposition_get")
	RSItemComposition getItemComposition(int itemId, int garbage);

	@Override
	@Import("client_plane")
	int getRSPlane();

	@Override
	int getPlane();

	@Override
	boolean isClientThread();

	@Override
	int getOverlayWidth();

	@Override
	int getOverlayHeight();

	@Override
	void setOverlayWidth(int width);

	@Override
	void setOverlayHeight(int height);

	@Override
	@Import("cameraZ")
	int getCameraZ();

	@Override
	@Import("cameraPitch")
	int getCameraPitch();

	@Override
	@Import("cameraYaw")
	int getCameraYaw();

	@Override
	@Import("cameraY")
	int getCameraY();

	@Override
	@Import("cameraX")
	int getCameraX();

	@Override
	@Import("viewportZoom")
	int getCameraZoom();

	@Override
	@Import("viewportWidth")
	int getViewportWidth();

	@Override
	@Import("viewportHeight")
	int getViewportHeight();
}

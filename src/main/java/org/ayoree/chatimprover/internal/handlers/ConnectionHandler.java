/*
 * ChatImprover, a Minecraft mod
 * Copyright (C) Ayoree <https://github.com/Ayoree>
 * Copyright (C) ChatImprover team and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package org.ayoree.chatimprover.internal.handlers;

import org.ayoree.chatimprover.internal.factories.ChatMessageFactory;
import org.ayoree.chatimprover.internal.factories.FilterFactory;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ServerInfo;

public class ConnectionHandler {
    public static void init() {
        ClientPlayConnectionEvents.JOIN.register(ConnectionHandler::onConnect);
        ClientPlayConnectionEvents.DISCONNECT.register(ConnectionHandler::onDisconnect);
    }

    private static void onConnect(final ClientPlayNetworkHandler networkHandler, final PacketSender sender, final MinecraftClient client) {
        final ServerInfo server = client.getCurrentServerEntry();
        ChatMessageFactory.setCurrentServer(server);
        FilterFactory.setCurrentServer(server);
    }

    private static void onDisconnect(final ClientPlayNetworkHandler networkHandler, final MinecraftClient client) {
        final ServerInfo server = client.getCurrentServerEntry();
        ChatMessageFactory.setCurrentServer(server);
        FilterFactory.setCurrentServer(server);
    }
}

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

import org.ayoree.chatimprover.internal.screens.LastMessagesScreen;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;

public class CommandToScreenHandler {
    private static boolean s_openLastMessagesScreen = false;

    public static void init() {
        ClientTickEvents.END_CLIENT_TICK.register(CommandToScreenHandler::checkScreens);
    }

    private static void checkScreens(MinecraftClient client) {
        if (s_openLastMessagesScreen) {
            s_openLastMessagesScreen = false;
            client.setScreen(LastMessagesScreen.generateScreen().create(client.currentScreen));
        }
    }

    public static void openLastMessagesScreen() { s_openLastMessagesScreen = true; }
}

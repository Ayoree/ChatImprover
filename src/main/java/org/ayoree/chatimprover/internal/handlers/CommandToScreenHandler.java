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

import java.util.function.Function;


import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;

public class CommandToScreenHandler {
    private static Function<Screen, Screen> OPENER = null;

    public static void init() {
        ClientTickEvents.END_CLIENT_TICK.register(CommandToScreenHandler::checkScreens);
    }

    private static void checkScreens(final MinecraftClient client) {
        if (OPENER != null) {
            client.setScreen(OPENER.apply(client.currentScreen));
            OPENER = null;
        }
    }

    public static void openScreen(Function<Screen, Screen> screen) { OPENER = screen; }
}

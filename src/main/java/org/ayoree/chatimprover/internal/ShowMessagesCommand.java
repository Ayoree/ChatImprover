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

package org.ayoree.chatimprover.internal;

import org.ayoree.chatimprover.internal.handlers.CommandToScreenHandler;

import com.mojang.brigadier.CommandDispatcher;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;

public class ShowMessagesCommand {
    public static void Init() {
        CommandToScreenHandler.init();
        ClientCommandRegistrationCallback.EVENT.register(ShowMessagesCommand::register);
    }

    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandRegistryAccess regAccess) {
        dispatcher.register(ClientCommandManager
            .literal("ayo")
                .executes(context -> {
                    // Unimplemented yet
                    return 1;
                }
            )
            .then(ClientCommandManager
                .literal("messages")
                .executes(context -> {
                    CommandToScreenHandler.openLastMessagesScreen();
                    return 1;
                })
            )
        );
    }
}

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

import java.util.concurrent.CompletableFuture;

import org.ayoree.chatimprover.internal.handlers.CommandToScreenHandler;
import org.ayoree.chatimprover.internal.screens.ChatimproverCustomScreen;
import org.ayoree.chatimprover.internal.screens.ChatimproverEditCustomScreen;
import org.ayoree.chatimprover.internal.screens.LastMessagesScreen;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;

public class CustomCommands {
    public static void Init() {
        CommandToScreenHandler.init();
        ClientCommandRegistrationCallback.EVENT.register(CustomCommands::register);
    }

    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandRegistryAccess regAccess) {
        dispatcher.register(ClientCommandManager
            .literal("debug_messages")
                .executes(context -> {
                    CommandToScreenHandler.openScreen(
                        parentScreen -> new LastMessagesScreen(parentScreen)
                    );
                    return Command.SINGLE_SUCCESS;
                }
            )
        );

        SuggestionProvider<FabricClientCommandSource> suggestionProvider = (context, builder) -> getSuggestions(context, builder);
        dispatcher.register(ClientCommandManager
            .literal("ayo")
                .then(ClientCommandManager
                    .literal("edit")
                    .executes(context -> {
                        CommandToScreenHandler.openScreen(
                            parentScreen -> new ChatimproverEditCustomScreen(parentScreen)
                        );
                        return Command.SINGLE_SUCCESS;
                    })
                )
                .then(ClientCommandManager
                    .literal("open")
                    .executes(context -> {
                        CommandToScreenHandler.openScreen(
                            parentScreen -> new ChatimproverCustomScreen(parentScreen)
                        );
                        return Command.SINGLE_SUCCESS;
                    })
                    .then(ClientCommandManager
                        .argument("data", StringArgumentType.greedyString())
                        .suggests(suggestionProvider)
                        .executes(context -> {

                            return Command.SINGLE_SUCCESS;
                        })
                    )
                )
                .executes(context -> {
                    
                    return Command.SINGLE_SUCCESS;
                })
        );
    }

    private static CompletableFuture<Suggestions> getSuggestions(CommandContext<FabricClientCommandSource> context, SuggestionsBuilder builder) {
        String remaining = builder.getRemaining().toLowerCase();
        context.getSource().getClient().getNetworkHandler().getPlayerList().forEach(player -> {
            final String nickname = player.getProfile().name();
            if (nickname.startsWith(remaining)) {
                builder.suggest(nickname);
            }
        });
        
        return builder.buildFuture();
    }
}

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

import java.util.HashMap;
import java.util.concurrent.CompletableFuture;

import org.ayoree.chatimprover.internal.configs.Config;
import org.ayoree.chatimprover.internal.configs.CustomScreenConfig;
import org.ayoree.chatimprover.internal.handlers.CommandToScreenHandler;
import org.ayoree.chatimprover.internal.screens.LastMessagesScreen;
import org.ayoree.chatimprover.internal.screens.customconfig.EditCustomConfigScreen;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.text.Text;

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
                        client -> LastMessagesScreen.createScreen(client.currentScreen)
                    );
                    return Command.SINGLE_SUCCESS;
                }
            )
        );

        SuggestionProvider<FabricClientCommandSource> suggestionProvider = (context, builder) -> getSuggestions(builder);

        dispatcher.register(ClientCommandManager
            .literal("ayo")
                .then(ClientCommandManager
                    .literal("create")
                    .then(ClientCommandManager
                        .argument("configName", StringArgumentType.word())
                        .executes(context -> {
                            String configName = context.getArgument("configName", String.class);
                            Config config = Config.getInst();
                            if (!config.screenConfigs.containsKey(configName))
                                Config.getInst().createScreenConfig(configName);
                            else {
                                context.getSource().sendFeedback(Text.literal("Конфиг с таким названием уже существует"));
                            }
                            return Command.SINGLE_SUCCESS;
                        })
                    )
                )
                .then(ClientCommandManager
                    .literal("edit")
                    .then(ClientCommandManager
                        .argument("configName", StringArgumentType.word())
                        .suggests(suggestionProvider)
                        .executes(context -> {
                            final String configName = context.getArgument("configName", String.class);
                            final HashMap<String, ConfigClassHandler<CustomScreenConfig>> screenConfigs = Config.getInst().screenConfigs;
                            if (!screenConfigs.containsKey(configName))
                                context.getSource().sendFeedback(Text.literal("Конфига с таким названием нет"));
                            else {
                                CommandToScreenHandler.openScreen(
                                    client -> EditCustomConfigScreen.createScreen(client.currentScreen, screenConfigs.get(configName))
                                );
                            }
                            return Command.SINGLE_SUCCESS;
                        })
                    )
                )
                .executes(context -> {
                    
                    return Command.SINGLE_SUCCESS;
                })
        );
    }

    private static CompletableFuture<Suggestions> getSuggestions(SuggestionsBuilder builder) {
        String remaining = builder.getRemaining().toLowerCase();
        
        for (String key : Config.getInst().screenConfigs.keySet()) {
            if (key.toLowerCase().startsWith(remaining)) {
                builder.suggest(key);
            }
        }
        
        return builder.buildFuture();
    }
}

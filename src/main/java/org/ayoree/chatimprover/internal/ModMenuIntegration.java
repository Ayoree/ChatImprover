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

import java.util.ArrayList;
import java.util.HashSet;

import org.ayoree.chatimprover.api.ChatMessage;
import org.ayoree.chatimprover.internal.factories.ChatMessageFactory;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.OptionGroup;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.api.controller.BooleanControllerBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.text.Text;

public class ModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        OptionGroup.Builder groupBuilder = OptionGroup.createBuilder()
            .name(Text.literal("Плагины"))
            .description(OptionDescription.of(Text.literal("Включить / Выключить специфичный плагин")));

        HashSet<String> addonIDs = new HashSet<>();
        ArrayList<ChatMessage.Provider> providers = ChatMessageFactory.getAllProviders();
        for (final ChatMessage.Provider provider : providers) {
            addonIDs.add(provider.addonID().get());
        }
        for (final String addonId : addonIDs) {

            ModContainer modContainer = FabricLoader.getInstance().getModContainer(addonId).get();
            String addonName = modContainer.getMetadata().getName();

            groupBuilder.option(Option.<Boolean>createBuilder()
                .name(Text.literal(addonName))
                .description(OptionDescription.of(Text.literal("Включить / Выключить")))
                .binding(
                    true,
                    () -> isAddonEnabled(addonId),
                    isEnabled -> setAddonEnabled(addonId, isEnabled)
                )
                .controller(BooleanControllerBuilder::create)
                .build());
        }

        return parentScreen -> YetAnotherConfigLib.createBuilder()
            .title(Text.empty())
            .category(ConfigCategory.createBuilder()
                .name(Text.literal("Настройки"))
                .tooltip(Text.literal("Ты нашёл печеньку! 🍪"))
                .group(OptionGroup.createBuilder()
                    .name(Text.literal("Настройки"))
                    .description(OptionDescription.of(Text.literal("Все настройки")))
                    .option(Option.<Boolean>createBuilder()
                        .name(Text.literal("Блокировать спам-сообщения"))
                        .description(OptionDescription.of(Text.literal("Включить / Выключить")))
                        .binding(true, () -> Config.getInst().isBlockTrash, newVal -> Config.getInst().isBlockTrash = newVal)
                        .controller(BooleanControllerBuilder::create)
                        .build())
                    .option(Option.<Boolean>createBuilder()
                        .name(Text.literal("Улучшать сообщения"))
                        .description(OptionDescription.of(Text.literal("Включить / Выключить")))
                        .binding(true, () -> Config.getInst().isImproveMessages, newVal -> Config.getInst().isImproveMessages = newVal)
                        .controller(BooleanControllerBuilder::create)
                        .build())
                    .build())
                .group(groupBuilder.build())
                .build())
            .save(Config::save)
            .build()
            .generateScreen(parentScreen);
    }

    private static boolean isAddonEnabled(String addonId) {
        return !Config.getInst().disabledAddons.contains(addonId);
    }

    private static void setAddonEnabled(String addonId, boolean enabled) {
        if (enabled) {
            Config.getInst().disabledAddons.remove(addonId);
        } else {
            Config.getInst().disabledAddons.add(addonId);
        }
    }
}
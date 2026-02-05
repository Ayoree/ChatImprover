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

package org.ayoree.chatimprover.internal.screens.customconfig;

import java.util.ArrayList;
import java.util.List;

import org.ayoree.chatimprover.internal.configs.CustomScreenConfig;
import org.ayoree.chatimprover.internal.configs.customconfig.helpers.CustomConfigEntry;
import org.jetbrains.annotations.NotNull;

import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.ListOption;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionGroup;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.api.controller.StringControllerBuilder;
import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class EditCustomConfigScreen {
    public static Screen createScreen(final Screen parentScreen, @NotNull final ConfigClassHandler<CustomScreenConfig> configHandler) {
        final CustomScreenConfig screenConfig = configHandler.instance();
        return YetAnotherConfigLib.createBuilder()
            .title(Text.empty())
            .category(createCategory(screenConfig))
            .save(() -> configHandler.save())
            .build()
            .generateScreen(parentScreen);
    }

    private static ConfigCategory createCategory(final CustomScreenConfig container) {
        final String defTitle = container.title;
        ConfigCategory.Builder categoryBuilder = ConfigCategory.createBuilder()
            .name(Text.literal("Редактирование конфига"))
            .tooltip(Text.literal("Изменить настройки конфига"))
            .group(OptionGroup.createBuilder()
                .name(Text.literal("Базовые настройки"))
                .option(Option.<String>createBuilder()
                    .name(Text.literal("Заголовок"))
                    .binding(defTitle, () -> container.title, newVal -> container.title = newVal)
                    .controller(StringControllerBuilder::create)
                    .build()
                )
                .build()
            );

        final List<CustomConfigEntry> entries = container.entries;
        if (entries.isEmpty()) {
            entries.add(new CustomConfigEntry("test", new ArrayList<>()));
        }
        for (final CustomConfigEntry entry : entries) {
            categoryBuilder.group(createOptionGroup(entry));
        }
        return categoryBuilder.build();
    }

    private static OptionGroup createOptionGroup(CustomConfigEntry entry) {
        return ListOption.<String>createBuilder()
            .name(Text.literal(entry.title))
            .binding(new ArrayList<String>(), () -> entry.commands, newVal -> entry.commands = newVal)
            .controller(StringControllerBuilder::create)
            .initial("/")
            .build();
    }
}
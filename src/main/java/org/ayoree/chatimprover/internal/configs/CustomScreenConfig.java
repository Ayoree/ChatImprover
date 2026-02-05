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

package org.ayoree.chatimprover.internal.configs;

import static org.ayoree.chatimprover.ChatImprover.MOD_ID;

import java.util.ArrayList;

import org.ayoree.chatimprover.internal.configs.customconfig.helpers.CustomConfigEntry;

import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Identifier;

public class CustomScreenConfig {
    @SerialEntry
    public String title = "Заголовок";
    @SerialEntry
    public ArrayList<CustomConfigEntry> entries = new ArrayList<>();

    public static ConfigClassHandler<CustomScreenConfig> createHandler(String configID) {
        return ConfigClassHandler.createBuilder(CustomScreenConfig.class)
            .id(Identifier.of(MOD_ID,  configID))
            .serializer(config -> GsonConfigSerializerBuilder.create(config)
                    .setPath(FabricLoader.getInstance().getConfigDir().resolve("%s/configs/%s.json5".formatted(MOD_ID, configID)))
                    .setJson5(true)
                    .build())
            .build();
    }
}

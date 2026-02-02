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

import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Identifier;

import static org.ayoree.chatimprover.ChatImprover.MOD_ID;

import java.util.HashSet;

import static org.ayoree.chatimprover.ChatImprover.LOGGER;

public class Config {
    @SerialEntry
    public boolean isBlockTrash = true;
    @SerialEntry
    public boolean isImproveMessages = true;
    @SerialEntry
    public HashSet<String> disabledAddons = new HashSet<>();

    private static Config instance = null;
    private static ConfigClassHandler<Config> HANDLER = ConfigClassHandler.createBuilder(Config.class)
            .id(Identifier.of(MOD_ID, "config"))
            .serializer(config -> GsonConfigSerializerBuilder.create(config)
                    .setPath(FabricLoader.getInstance().getConfigDir().resolve("chatimprover/%s.json5".formatted(MOD_ID)))
                    .setJson5(true)
                    .build())
            .build();

    public static void save() {
        HANDLER.save();
    }

    public static Config getInst() {
        if (instance == null) {
            if (HANDLER.load())
                instance = HANDLER.instance();
            else
                LOGGER.error("Failed to load config");
        }
        return instance;
    }
}

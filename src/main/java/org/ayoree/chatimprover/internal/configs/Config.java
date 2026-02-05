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

import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Identifier;

import static org.ayoree.chatimprover.ChatImprover.MOD_ID;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import org.apache.commons.io.FilenameUtils;
import org.jetbrains.annotations.NotNull;

import static org.ayoree.chatimprover.ChatImprover.LOGGER;

public class Config {
    @SerialEntry
    public boolean isBlockTrash = true;
    @SerialEntry
    public boolean isImproveMessages = true;
    @SerialEntry
    public HashSet<String> disabledAddons = new HashSet<>();

    public HashMap<String, ConfigClassHandler<CustomScreenConfig>> screenConfigs = readScreenConfigs();

    private static Config instance = null;
    private static ConfigClassHandler<Config> HANDLER = ConfigClassHandler.createBuilder(Config.class)
            .id(Identifier.of(MOD_ID, "config"))
            .serializer(config -> GsonConfigSerializerBuilder.create(config)
                    .setPath(FabricLoader.getInstance().getConfigDir().resolve("%s/%s.json5".formatted(MOD_ID, MOD_ID)))
                    .setJson5(true)
                    .build())
            .build();

    public static void save() {
        HANDLER.save();
    }

    public static Config getInst() {
        if (instance == null) {
            if (HANDLER.load()) {
                instance = HANDLER.instance();
            }
            else
                LOGGER.error("Failed to load Config");
        }
        return instance;
    }

    public void createScreenConfig(@NotNull String configID) {
        ConfigClassHandler<CustomScreenConfig> handler = CustomScreenConfig.createHandler(configID);
        screenConfigs.put(configID, handler);
        handler.save();
    }
    
    public @Nullable CustomScreenConfig getScreenConfig(@NotNull String configID) {
        if (screenConfigs.containsKey(configID)) {
            return screenConfigs.get(configID).instance();
        }
        return null;
    }

    public void saveScreenConfig(@NotNull String configID) {
        if (screenConfigs.containsKey(configID)) {
            screenConfigs.get(configID).save();
        }
    }

    private ArrayList<Path> getAllConfigFiles() {
        LOGGER.info("getAllConfigFiles");
        ArrayList<Path> result = new ArrayList<>();
        Path folder = FabricLoader.getInstance().getConfigDir().resolve("%s/configs/".formatted(MOD_ID));
        try (Stream<Path> paths = Files.list(folder)){
            paths.forEach(path -> {
                LOGGER.info("path: %s".formatted(path.toString()));
                if (!Files.isRegularFile(path))
                    return;
                if (!FilenameUtils.getExtension(path.toString()).equals("json5"))
                    return;
                result.add(path);
            });
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
        }
        return result;
    }

    private HashMap<String, ConfigClassHandler<CustomScreenConfig>> readScreenConfigs() {
        HashMap<String, ConfigClassHandler<CustomScreenConfig>> result = new HashMap<>();
        getAllConfigFiles().forEach(path -> {
            String configName = FilenameUtils.getBaseName(path.toString());
            result.put(configName, CustomScreenConfig.createHandler(configName));
        });
        return result;
    }
}

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

package org.ayoree.chatimprover.internal.factories;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.ServiceLoader;

import org.ayoree.chatimprover.api.Filter;
import org.ayoree.chatimprover.internal.configs.Config;

import net.minecraft.client.network.ServerInfo;
import net.minecraft.text.Text;

public class FilterFactory {
    private static ServerInfo SERVER_INFO = null;
    private static final HashMap<String, ArrayList<Filter.Provider>> REGISTRY = new HashMap<>();

    // Load all providers
    static {
        ServiceLoader.load(Filter.Provider.class)
            .stream()
            .map(ServiceLoader.Provider::get)
            .forEach(provider -> {
                provider.IPs().get().forEach(addr -> {
                    ArrayList<Filter.Provider> array = REGISTRY.computeIfAbsent(addr, k -> new ArrayList<>());
                    array.add(provider);
                });
            });
    }

    public static boolean testAllFilters(Text text) {
        if (SERVER_INFO != null) {
            final HashSet<String> disabledAddons = Config.getInst().disabledAddons;
            ArrayList<Filter.Provider> arr = REGISTRY.getOrDefault(SERVER_INFO.address, new ArrayList<>());
            for (final Filter.Provider provider : arr) {
                if (disabledAddons.contains(provider.addonID().get()))
                    continue;
                if (provider.validator().test(text)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static void setCurrentServer(ServerInfo serverInfo) {
        SERVER_INFO = serverInfo;
    }
}

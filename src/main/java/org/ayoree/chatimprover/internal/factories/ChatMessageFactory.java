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

import static org.ayoree.chatimprover.ChatImprover.CONFIG;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.ServiceLoader;
import java.util.Set;

import org.ayoree.chatimprover.api.ChatMessage;
import org.jetbrains.annotations.NotNull;

import net.minecraft.client.network.ServerInfo;
import net.minecraft.text.Text;

public class ChatMessageFactory {
    private static ServerInfo SERVER_INFO = null;
    private static final HashMap<String, ArrayList<ChatMessage.Provider>> REGISTRY = new HashMap<>();

    // Load all providers
    static {
        ServiceLoader.load(ChatMessage.Provider.class)
            .stream()
            .map(ServiceLoader.Provider::get)
            .forEach(provider -> {
                provider.IPs().get().forEach(addr -> {
                    ArrayList<ChatMessage.Provider> array = REGISTRY.computeIfAbsent(addr, k -> new ArrayList<>());
                    array.add(provider);
                });
            });
    }

    public static ChatMessage createChatMessage(Text text) {
        if (SERVER_INFO != null) {
            final Set<String> disabledAddons = CONFIG.disabledAddons();
            ArrayList<ChatMessage.Provider> arr = REGISTRY.getOrDefault(SERVER_INFO.address, new ArrayList<>());
            for (final ChatMessage.Provider provider : arr) {
                if (disabledAddons.contains(provider.addonID().get()))
                    continue;
                if (provider.validator().test(text))
                    return provider.creator().apply(text);
            }
        }
        return new ChatMessage(text);
    }

    public static void setCurrentServer(ServerInfo serverInfo) {
        SERVER_INFO = serverInfo;
    }
    
    public static final @NotNull ArrayList<ChatMessage.Provider> getAllProviders() {
        ArrayList<ChatMessage.Provider> providers = new ArrayList<>();
        REGISTRY.forEach((k, arr) -> {
            providers.addAll(arr);
        });
        return providers;
    }
}

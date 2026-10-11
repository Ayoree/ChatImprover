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

package org.ayoree.chatimprover.internal.handlers;

import static org.ayoree.chatimprover.ChatImprover.LOGGER;
import static org.ayoree.chatimprover.ChatImprover.MOD_ID;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

import org.ayoree.chatimprover.api.ChatImproverApi;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;

import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;

public class MessageDumpHandler {
    private static final String FLAG = "-debug-chatimprover";
    private static final String PROPERTY = "chatimprover.debug";
    private static final Identifier DUMP_PHASE = Identifier.fromNamespaceAndPath(MOD_ID, "dump");
    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().serializeNulls().create();
    private static BufferedWriter WRITER = null;
    private static int SEQ = 0;

    public static void init() {
        // Launchers only expose JVM arguments, so the flag also comes as -Dchatimprover.debug=true
        if (!Boolean.getBoolean(PROPERTY) && !Arrays.asList(FabricLoader.getInstance().getLaunchArguments(true)).contains(FLAG))
            return;

        final String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
        final Path file = FabricLoader.getInstance().getGameDir().resolve("logs").resolve(MOD_ID).resolve("messages-%s.jsonl".formatted(timestamp));
        try {
            Files.createDirectories(file.getParent());
            WRITER = Files.newBufferedWriter(file, StandardCharsets.UTF_8);
            write(header());
        } catch (final IOException e) {
            LOGGER.error("Failed to create message dump `{}`", file, e);
            return;
        }
        LOGGER.info("Dumping received chat messages to `{}`", file);

        // Before addons and ChatHandler, which may cancel the message
        ClientReceiveMessageEvents.ALLOW_GAME.addPhaseOrdering(DUMP_PHASE, ChatImproverApi.ADDON_PHASE);
        ClientReceiveMessageEvents.ALLOW_GAME.register(DUMP_PHASE, MessageDumpHandler::onGameMessage);
        ClientReceiveMessageEvents.ALLOW_CHAT.addPhaseOrdering(DUMP_PHASE, Event.DEFAULT_PHASE);
        ClientReceiveMessageEvents.ALLOW_CHAT.register(DUMP_PHASE, MessageDumpHandler::onChatMessage);
    }

    private static boolean onGameMessage(final Component message, final boolean isOverlay) {
        if (!isOverlay)
            dump("system", message, null);
        return true;
    }

    private static boolean onChatMessage(final Component message, final PlayerChatMessage signedMessage, final GameProfile sender, final ChatType.Bound params, final Instant receptionTimestamp) {
        dump("player_chat", message, sender);
        return true;
    }

    private static void dump(final String kind, final Component message, final GameProfile sender) {
        try {
            final DynamicOps<JsonElement> ops = jsonOps();
            final ServerData server = Minecraft.getInstance().getCurrentServer();
            final JsonObject record = new JsonObject();
            record.addProperty("seq", ++SEQ);
            record.addProperty("kind", kind);
            record.addProperty("time", LocalDateTime.now().toString());
            record.addProperty("server", server == null ? null : server.ip);
            record.addProperty("sender", sender == null ? null : sender.name());
            record.addProperty("plain", message.getString());
            record.add("tree", node(message, "root", ops));
            record.add("json", encode(ComponentSerialization.CODEC, message, ops));
            write(record);
        } catch (final Exception e) {
            LOGGER.warn("Failed to dump chat message", e);
        }
    }

    private static JsonObject node(final Component component, final String path, final DynamicOps<JsonElement> ops) {
        final JsonObject node = new JsonObject();
        node.addProperty("path", path);
        node.add("content", content(component, ops));
        node.add("style", encode(Style.Serializer.CODEC, component.getStyle(), ops));
        node.addProperty("string", component.getString());

        final JsonArray siblings = new JsonArray();
        final List<Component> children = component.getSiblings();
        for (int i = 0; i < children.size(); ++i)
            siblings.add(node(children.get(i), "%s.siblings[%d]".formatted(path, i), ops));
        node.add("siblings", siblings);
        return node;
    }

    private static JsonElement content(final Component component, final DynamicOps<JsonElement> ops) {
        final JsonElement content = encode(ComponentSerialization.CODEC, MutableComponent.create(component.getContents()), ops);
        // The codec collapses plain text to a bare string
        if (content.isJsonPrimitive()) {
            final JsonObject text = new JsonObject();
            text.add("text", content);
            return text;
        }
        return content;
    }

    private static <T> JsonElement encode(final Codec<T> codec, final T value, final DynamicOps<JsonElement> ops) {
        return codec.encodeStart(ops, value).mapOrElse(json -> json, error -> {
            final JsonObject failure = new JsonObject();
            failure.addProperty("encodeError", error.message());
            failure.addProperty("toString", String.valueOf(value));
            return failure;
        });
    }

    private static DynamicOps<JsonElement> jsonOps() {
        final ClientPacketListener connection = Minecraft.getInstance().getConnection();
        return connection == null ? JsonOps.INSTANCE : connection.registryAccess().createSerializationContext(JsonOps.INSTANCE);
    }

    private static JsonObject header() {
        final JsonObject schema = new JsonObject();
        schema.addProperty("file", "JSON Lines: this header, then one record per message received from the server, captured before ChatImprover or its addons filter or change it.");
        schema.addProperty("seq", "Record number within the file.");
        schema.addProperty("kind", "system: game/system message (ClientReceiveMessageEvents.ALLOW_GAME), the only kind ChatImprover addons can filter or improve. player_chat: signed player chat (ALLOW_CHAT), never processed by ChatImprover. Action-bar messages are not recorded.");
        schema.addProperty("time", "Local time the message was received.");
        schema.addProperty("server", "Server address as typed in the server list (ServerData.ip); AddonInformer.IPs() is matched against it. null in singleplayer.");
        schema.addProperty("sender", "Sender profile name for player_chat, otherwise null.");
        schema.addProperty("plain", "message.getString(): the flattened text that validators usually match. Legacy § formatting codes, if any, are part of it.");
        schema.addProperty("tree", "The Component tree, root first. Node fields: path, content, style, string, siblings.");
        schema.addProperty("tree.path", "Java access path: root.siblings[2].siblings[0] is message.getSiblings().get(2).getSiblings().get(0).");
        schema.addProperty("tree.content", "node.getContents() as vanilla text component JSON: {text}, {translate, with, fallback}, {keybind}, {score}, {selector}, {nbt} or {object}.");
        schema.addProperty("tree.style", "node.getStyle() as vanilla JSON: color, shadow_color, bold, italic, underlined, strikethrough, obfuscated, click_event, hover_event, insertion, font. Only fields set on this node: a missing field is inherited from the parent, an explicit false overrides it.");
        schema.addProperty("tree.string", "node.getString(): this node's text followed by the text of all its siblings.");
        schema.addProperty("tree.siblings", "node.getSiblings() in render order, each rendered after the node's own content.");
        schema.addProperty("json", "The whole message as vanilla text component JSON, as /tellraw accepts it. ComponentSerialization.CODEC decodes it, e.g. for test fixtures.");

        final JsonObject header = new JsonObject();
        header.addProperty("kind", "header");
        header.addProperty("format", "chatimprover-message-dump/1");
        header.addProperty("minecraft", version("minecraft"));
        header.addProperty(MOD_ID, version(MOD_ID));
        header.addProperty("started", LocalDateTime.now().toString());
        header.add("schema", schema);
        return header;
    }

    private static String version(final String modId) {
        return FabricLoader.getInstance().getModContainer(modId)
            .map(mod -> mod.getMetadata().getVersion().getFriendlyString())
            .orElse(null);
    }

    private static void write(final JsonObject json) throws IOException {
        WRITER.write(GSON.toJson(json));
        WRITER.write('\n');
        WRITER.flush();
    }
}

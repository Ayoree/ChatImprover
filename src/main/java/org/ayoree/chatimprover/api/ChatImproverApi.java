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

package org.ayoree.chatimprover.api;

import static org.ayoree.chatimprover.ChatImprover.CONFIG;
import static org.ayoree.chatimprover.ChatImprover.MOD_ID;

import org.ayoree.chatimprover.internal.factories.ChatMessageFactory;
import org.ayoree.chatimprover.mixin.ChatHudAccessor;
import org.jetbrains.annotations.NotNull;

import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

/**
 * Entry points an addon can use to push a {@link Component} through the improving
 * pipeline outside of the normal {@code ClientReceiveMessageEvents} flow.
 * <p>
 * This is what an addon needs when it cancels a message in
 * {@code ClientReceiveMessageEvents.ALLOW_GAME} in order to display it later,
 * for example while it waits for an asynchronous server response.
 */
public final class ChatImproverApi {
    /**
     * Phase an addon must register its {@link ClientReceiveMessageEvents#ALLOW_GAME} listener into
     * to be called before ChatImprover's own one.
     * <p>
     * An addon is loaded after ChatImprover, so by default its listener would be registered later
     * and run later. That is too late: ChatImprover cancels the event on the `fixChatOnFocus` path,
     * which would keep the addon from ever seeing a message while the chat is open.
     * <p>
     * Usage:
     * <pre>{@code
     * ClientReceiveMessageEvents.ALLOW_GAME.register(ChatImproverApi.ADDON_PHASE, MyHandler::onAllowMessage);
     * }</pre>
     */
    @NotNull
    public static final Identifier ADDON_PHASE = Identifier.fromNamespaceAndPath(MOD_ID, "addons");

    private ChatImproverApi() {}

    /**
     * Runs {@code message} through the improving pipeline without displaying it.
     * <p>
     * Honours {@code isImproveMessages} and {@code chatButtons} settings.
     *
     * @param message content to improve.
     * @return improved {@link Component}, or the original one when improving is disabled.
     */
    public static Component improve(final @NotNull Component message) {
        if (CONFIG.isImproveMessages())
            if (CONFIG.chatButtons())
                return ChatMessageFactory.createChatMessage(message).generateChangedMsg().addChatButtons().getChangedMessage();
            else
                return ChatMessageFactory.createChatMessage(message).generateChangedMsg().getChangedMessage();
        else
            if (CONFIG.chatButtons())
                return ChatMessageFactory.createChatMessage(message).addChatButtons().getChangedMessage();
            else
                return ChatMessageFactory.createChatMessage(message).getOrigMessage();
    }

    /**
     * Runs {@code message} through the improving pipeline and appends it to the client chat.
     * <p>
     * {@code ClientReceiveMessageEvents} are <b>not</b> fired again, so filters do not run
     * a second time and no recursion is possible.
     *
     * @param message content to improve and display.
     */
    public static void reemit(final @NotNull Component message) {
        final ChatComponent chatHud = Minecraft.getInstance().gui.hud.getChat();
        final boolean keepHistoryInPlace = CONFIG.fixChatOnFocus()
            && chatHud.isChatFocused()
            && ((ChatHudAccessor) chatHud).getChatScrollbarPos() == 0;

        chatHud.addClientSystemMessage(improve(message));
        if (keepHistoryInPlace)
            chatHud.scrollChat(1);
    }
}

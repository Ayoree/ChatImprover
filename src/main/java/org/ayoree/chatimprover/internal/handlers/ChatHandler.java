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

import static org.ayoree.chatimprover.ChatImprover.CONFIG;

import org.ayoree.chatimprover.internal.factories.ChatMessageFactory;
import org.ayoree.chatimprover.internal.factories.FilterFactory;
import org.ayoree.chatimprover.mixin.ChatHudAccessor;

import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.text.Text;

public class ChatHandler {
    private static MinecraftClient CLIENT;

    public static void init() {
        CLIENT = MinecraftClient.getInstance();
        ClientReceiveMessageEvents.ALLOW_GAME.register(ChatHandler::onAllowMessage);;
        ClientReceiveMessageEvents.MODIFY_GAME.register(ChatHandler::onModifyMessage);
    }

    private static boolean onAllowMessage(final Text message, final boolean isOverlay) {
        final boolean isTrash = CONFIG.isBlockTrash() && FilterFactory.testAllFilters(message);
        if (isTrash)
            return false;

        final ChatHud chatHud = CLIENT.inGameHud.getChatHud();
        if (CONFIG.fixChatOnFocus() && chatHud.isChatFocused() && ((ChatHudAccessor)chatHud).getScrolledLines() == 0) {
            // TODO: add ChatPlus compatibility
            chatHud.addMessage(onModifyMessage(message, isOverlay));
            chatHud.scroll(1);
            return false;
        }
        return true;
    }

    private static Text onModifyMessage(final Text origMessage, final boolean overlay) {
        if (CONFIG.isImproveMessages())
            if (CONFIG.chatButtons())
                return ChatMessageFactory.createChatMessage(origMessage).generateChangedMsg().addChatButtons().getChangedMessage();
            else
                return ChatMessageFactory.createChatMessage(origMessage).generateChangedMsg().getChangedMessage();
        else
            if (CONFIG.chatButtons())
                return ChatMessageFactory.createChatMessage(origMessage).addChatButtons().getChangedMessage();
            else
                return ChatMessageFactory.createChatMessage(origMessage).getOrigMessage();
    }
}

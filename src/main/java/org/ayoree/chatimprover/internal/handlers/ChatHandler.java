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

import org.ayoree.chatimprover.api.ChatMessage;
import org.ayoree.chatimprover.internal.Config;
import org.ayoree.chatimprover.internal.factories.ChatMessageFactory;
import org.ayoree.chatimprover.internal.factories.FilterFactory;

import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.text.Text;

public class ChatHandler {
    public static void init() {
        ClientReceiveMessageEvents.ALLOW_GAME.register(ChatHandler::onAllowMessage);;
        ClientReceiveMessageEvents.MODIFY_GAME.register(ChatHandler::onMessageReceive);
    }

    private static boolean onAllowMessage(Text message, boolean isOverlay) {
         if (!Config.getInst().isBlockTrash)
            return true;
        return !FilterFactory.testAllFilters(message);
    }

    private static Text onMessageReceive(Text origMessage, boolean overlay) {
        if (!Config.getInst().isImproveMessages)
            return origMessage;
        ChatMessage message = ChatMessageFactory.createChatMessage(origMessage);
        return message.getChangedMessage();
    }
}

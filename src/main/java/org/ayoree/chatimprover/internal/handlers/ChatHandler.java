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

import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.text.Text;

public class ChatHandler {
    public static void init() {
        ClientReceiveMessageEvents.ALLOW_GAME.register(ChatHandler::onAllowMessage);;
        ClientReceiveMessageEvents.MODIFY_GAME.register(ChatHandler::onMessageReceive);
    }

    private static boolean onAllowMessage(final Text message, final boolean isOverlay) {
        if (!CONFIG.isBlockTrash())
            return true;
        return !FilterFactory.testAllFilters(message);
    }

    private static Text onMessageReceive(final Text origMessage, final boolean overlay) {
        if (!CONFIG.isImproveMessages())
            return origMessage;
        return ChatMessageFactory.createChatMessage(origMessage).getChangedMessage();
    }
}

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

import org.jetbrains.annotations.NotNull;

import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;

public class ChatMessageWithReceiver extends ChatMessage implements IChatMessageWithReceiver {
    private String m_receiverNick;
    
    public ChatMessageWithReceiver(@NotNull final Text msg) {
        super(msg);
    }

    public final void setReceiverNick(final String nick) { m_receiverNick = nick; }
    public final String getReceiverNick() { return m_receiverNick; }

    @Override
    public ChatMessage addChatButtons() {
        m_changedMsg = Text.empty().append(getSymbol()).append(getChangedMessage());
        return this;
    }

    private MutableText getSymbol() {
        final MutableText symbol = Text.literal(
            CONFIG.receiverSymbol().replace('&', '§'))
            .setStyle(Style.EMPTY
                .withClickEvent(new ClickEvent.RunCommand("ayo open " + getReceiverNick()))
                .withHoverEvent(new HoverEvent.ShowText(Text.of("/ayo open " + getReceiverNick())))
            );
        return symbol;
    }
}

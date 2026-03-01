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

import java.util.function.Function;
import java.util.function.Predicate;

import org.jetbrains.annotations.NotNull;

import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.text.Text;

/**
 * This class represents every message,
 * that will be displayed in client's {@link ChatHud}.
 * <p>
 * You should create your own class, that extends {@link ChatMessage},
 * and overrides {@link ChatMessage#getChangedMessage()} method.
 * <p>
 * After that, you need to implement your custom {@link Provider} and mark it with {@code @AutoService} annotation.
 * 
 * @see <a href="https://github.com/Ayoree/ChatImprove-template">ChatImprove template mod example</a>.
 */
public class ChatMessage {
    /**
     * This field contains original content of the {@link ChatHud}'s message.
     */
    protected final Text m_message;
    
    /**
     * Constructs {@link ChatMessage} object.
     * @param msg original content of the message.
     */
    public ChatMessage(@NotNull final Text msg) {
        m_message = msg;
    }

    /**
     * This method is used for <b>`improving`</b> original message
     * <i>(e.g. adding some styles to it or somehow changing the content)</i>.
     * 
     * @return {@link Text} changed content of the original message.
     */
    public Text getChangedMessage() {
        return m_message;
    }

    protected Text addExtraStuff(Text msg) {
        return msg;
    }

    /**
     * Getter for {@link #m_message} field.
     * @return {@link Text}
     */
    public Text getMessage() {
        return m_message;
    }

    /**
     * Calls {@link Text#getString()} on {@link #m_message} field.
     * @return {@link String}
     */
    public String getMessageStr() {
        return m_message.getString();
    }
    
    /**
     * {@code Interface} for validating message content
     * and creating {@link ChatMessage} object.
     * <p>
     * You need to create your own class, that implements {@link Provider},
     * and mark it with {@code @AutoService} annotation.
     * 
     * @see <a href="https://github.com/Ayoree/ChatImprove-template">ChatImprove template mod example</a>.
     */
    public static interface Provider extends AddonInformer {
        /**
         * @see <a href="https://github.com/Ayoree/ChatImprove-template">ChatImprove template mod example</a>.
         * @return {@link Function} that accepts {@link Text} and returns a new {@link ChatMessage} instance.
         */
        public Function<Text, ChatMessage> creator();

        /**
         * Be sure that your implementation of {@link #validator()} allows <b>only suitable</b> {@link Text}'s
         * for constructing your inheritances of {@link ChatMessage}.
         * 
         * @see <a href="https://github.com/Ayoree/ChatImprove-template">ChatImprove template mod example</a>.
         * @return {@link Predicate} that accepts {@link Text} and returns {@code true} if {@link ChatMessage}
         * can be constructed from this {@link Text}. Otherwise returns {@code false}.
         */
        public Predicate<Text> validator();
    }
}

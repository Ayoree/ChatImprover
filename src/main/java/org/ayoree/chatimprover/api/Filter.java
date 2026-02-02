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


import java.util.function.Predicate;

import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.text.Text;

/**
 * Just inherit this class and create your own {@link Filter.Provider}'s inside it
 */
public class Filter {
    /**
     * {@code Interface} for approving chat messages to pass
     * {@link ClientReceiveMessageEvents#ALLOW_GAME} listener.
     * <p>
     * If {@link #validator()} returns {@code true}, then message will <b>not</b> appear in client's {@link ChatHud}.
     * <p>
     * You need to create your own class, that implements {@link Provider},
     * and mark it with {@code @AutoService} annotation.
     * 
     * @see <a href="https://github.com/Ayoree/ChatImprove-template">ChatImprove template mod example</a>.
     */
    public static interface Provider extends AddonInformer {
        /**
         * @see <a href="https://github.com/Ayoree/ChatImprove-template">ChatImprove template mod example</a>.
         * @return {@link Predicate} that accepts {@link Text} and returns {@code true} if chat message should be blocked.
         */
        public Predicate<Text> validator();
    }
}

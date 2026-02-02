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

import java.util.Set;
import java.util.function.Supplier;

import org.ayoree.chatimprover.ChatImprover;

/**
 * {@code Interface} for connecting addons to parent mod - {@link ChatImprover}.
 * <p>
 * To make your addon fully compatible with {@link ChatImprover} you need to implement this interface
 * by your custom {@code Provider} <i>(i.e. {@link Filter.Provider} and {@link ChatMessage.Provider})</i>
 * 
 * @see <a href="https://github.com/Ayoree/ChatImprove-template">ChatImprove template mod example</a>.
 */
public interface AddonInformer {
    /**
     * @return {@link Supplier} that returns current {@code MOD_ID}.
     */
    public Supplier<String> addonID();

    /**
     * @return {@link Supplier} that returns {@link Set} of {@link String}, containing {@code IP Addresses} of the server(s) addon was made for.
     * @see <a href="https://github.com/Ayoree/ChatImprove-template">ChatImprove template mod example</a>.
     */
    public Supplier<Set<String>> IPs();
}

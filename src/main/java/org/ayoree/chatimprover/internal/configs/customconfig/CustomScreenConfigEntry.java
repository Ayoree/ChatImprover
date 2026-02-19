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

package org.ayoree.chatimprover.internal.configs.customconfig;

import java.util.List;

public class CustomScreenConfigEntry{
    private String m_title;
    private List<String> m_commands;

    public CustomScreenConfigEntry(final String title, final List<String> commands) {
        m_title = title;
        m_commands = commands;
    }

    public String title() { return m_title; }
    public void title(final String title) { m_title = title; }

    public List<String> commands() { return m_commands; }
    public void commands(final List<String> commands) { m_commands = commands; }
}

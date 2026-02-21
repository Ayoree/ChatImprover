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

import java.util.ArrayList;
import java.util.List;

public class CustomScreenConfigCategory{
    private String m_name = "";
    private List<CustomScreenConfigCommand> m_commands = new ArrayList<>();

    public CustomScreenConfigCategory() {
        this("");
    }

    public CustomScreenConfigCategory(final String name) {
        this(name, new ArrayList<>());
    }

    public CustomScreenConfigCategory(final String name, final List<CustomScreenConfigCommand> commands) {
        m_name = name;
        m_commands = commands;
    }

    public String name() { return m_name; }
    public void name(final String name) { m_name = name; }

    public List<CustomScreenConfigCommand> commands() { return m_commands; }
    public void commands(final List<CustomScreenConfigCommand> commands) { m_commands = commands; }
}

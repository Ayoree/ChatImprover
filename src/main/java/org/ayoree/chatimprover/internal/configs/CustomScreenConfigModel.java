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

package org.ayoree.chatimprover.internal.configs;

import java.util.List;

import org.ayoree.chatimprover.internal.configs.customconfig.CustomScreenConfigEntry;

import io.wispforest.owo.config.annotation.Config;

import static org.ayoree.chatimprover.ChatImprover.MOD_ID;

@Config(name = MOD_ID + "/custom-screen-config", wrapperName = "CustomScreenConfig")
public class CustomScreenConfigModel {
    public String title;
    public List<CustomScreenConfigEntry> entries;
}

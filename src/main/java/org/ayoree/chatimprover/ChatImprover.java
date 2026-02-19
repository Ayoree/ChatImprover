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

package org.ayoree.chatimprover;

import net.fabricmc.api.ModInitializer;

import org.ayoree.chatimprover.internal.CustomCommands;
import org.ayoree.chatimprover.internal.configs.ChatimproverConfig;
import org.ayoree.chatimprover.internal.handlers.AttackHandler;
import org.ayoree.chatimprover.internal.handlers.ChatHandler;
import org.ayoree.chatimprover.internal.handlers.ConnectionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChatImprover implements ModInitializer {
	public static final String MOD_ID = "chatimprover";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static ChatimproverConfig CONFIG = ChatimproverConfig.createAndLoad();

	@Override
	public void onInitialize() {
		ChatHandler.init();
		ConnectionHandler.init();
		AttackHandler.init();
		CustomCommands.Init();
		LOGGER.info("`%s` mod initialized".formatted(MOD_ID));
	}
}
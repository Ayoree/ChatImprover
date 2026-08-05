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

import org.ayoree.chatimprover.internal.screens.ChatimproverCustomScreen;

import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.level.Level;

import static org.ayoree.chatimprover.ChatImprover.CONFIG;

public class RightClickHandler {
    public static void init() {
        UseEntityCallback.EVENT.register(RightClickHandler::onInteract);
    }

    private static InteractionResult onInteract(final Player player, final Level world, final InteractionHand hand, final Entity entity, final EntityHitResult hitResult) {
        if (!CONFIG.rightclickMenu())
            return InteractionResult.PASS;

        if (player.getMainHandItem().isEmpty()) {
            if (entity instanceof Player otherPlayer) {
                final Minecraft client = Minecraft.getInstance();
                client.gui.setScreen(new ChatimproverCustomScreen(client.gui.screen(), otherPlayer.getGameProfile().name()));
                player.swing(player.getUsedItemHand());
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
    }
}

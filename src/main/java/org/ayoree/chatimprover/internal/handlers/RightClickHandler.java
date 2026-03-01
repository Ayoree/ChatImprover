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
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.world.World;

public class RightClickHandler {
    public static void init() {
        UseEntityCallback.EVENT.register(RightClickHandler::onInteract);
    }

    private static ActionResult onInteract(final PlayerEntity player, final World world, final Hand hand, final Entity entity, final EntityHitResult hitResult) {

        if (player.getMainHandStack().isEmpty()) {
            if (entity instanceof PlayerEntity otherPlayer) {
                final MinecraftClient client = MinecraftClient.getInstance();
                client.setScreen(new ChatimproverCustomScreen(client.currentScreen, otherPlayer.getGameProfile().name()));
                player.swingHand(player.getActiveHand());
                return ActionResult.SUCCESS;
            }
        }
        return ActionResult.PASS;
    }
}

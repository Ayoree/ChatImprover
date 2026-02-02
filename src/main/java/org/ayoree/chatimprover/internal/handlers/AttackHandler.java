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

import org.jetbrains.annotations.Nullable;

import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.world.World;

public class AttackHandler {
    public static void init() {
        AttackEntityCallback.EVENT.register(AttackHandler::onAttack);
    }

    private static ActionResult onAttack(PlayerEntity player, World world, Hand hand, Entity entity, @Nullable EntityHitResult hitResult) {
        final ItemStack itemStack = player.getMainHandStack();
        final Item item = itemStack.getItem();
        if (Item.getRawId(item) != Item.getRawId(Items.MACE))
            return ActionResult.PASS;

        final Text itemName = itemStack.getCustomName();
        if (itemName == null || !itemName.getString().equals("Kick-Hammer"))
            return ActionResult.PASS;

        if (entity instanceof PlayerEntity targetPlayer) {
            String targetNick = targetPlayer.getGameProfile().name();
            ClientPlayNetworkHandler networkHandler = MinecraftClient.getInstance().getNetworkHandler();
            networkHandler.sendChatCommand("title \"" + targetNick + "\" title {\"entity\":\"" + MinecraftClient.getInstance().getSession().getUsername() + "\",\"nbt\":\"\"}");
        }
        return ActionResult.PASS;
    }
}

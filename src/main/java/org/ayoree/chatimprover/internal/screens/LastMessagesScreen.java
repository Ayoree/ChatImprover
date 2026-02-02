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

package org.ayoree.chatimprover.internal.screens;

import java.util.List;
import java.util.Stack;

import org.ayoree.chatimprover.mixin.ChatHudAccessor;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;

import dev.isxander.yacl3.api.ButtonOption;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.OptionGroup;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.ChatHudLine;
import net.minecraft.text.Text;

public class LastMessagesScreen {
    static public ConfigScreenFactory<?> generateScreen() {
        YetAnotherConfigLib.Builder builder = YetAnotherConfigLib.createBuilder().title(Text.empty());

        ConfigCategory.Builder categoryBuilder = ConfigCategory.createBuilder()
                .name(Text.literal("Последние сообщения"))
                .tooltip(Text.literal("Ты нашёл печеньку! 🍪"));

        ChatHud chatHud = MinecraftClient.getInstance().inGameHud.getChatHud();
        List<ChatHudLine> messages = ((ChatHudAccessor) chatHud).getMessages();

        int i = 0;
        for (ChatHudLine chatHudLine : messages) {
            OptionGroup.Builder optionGroupBuilder = OptionGroup.createBuilder()
                        .name(Text.literal(Integer.toString(i)))
                        .description(OptionDescription.of(chatHudLine.content()));

            Stack<Integer> depth = new Stack<Integer>();
            addRecursiveOptions(optionGroupBuilder, depth, chatHudLine.content());
            categoryBuilder.group(optionGroupBuilder.build());

            if (++i >= 10)
                break;
        }
        builder.category(categoryBuilder.build());

        return parentScreen -> builder
            .build()
            .generateScreen(parentScreen);
    }

    static private void addRecursiveOptions(OptionGroup.Builder builder, Stack<Integer> depth, Text text) {
        String curIndex = "";
        for (final int i : depth)
            curIndex += "[" + i + "]";
        if (curIndex.isEmpty()) curIndex = "Main";

        builder.option(ButtonOption.createBuilder()
                .name(Text.literal(curIndex))
                .text(Text.literal("`").append(text).append("`"))
                .description(OptionDescription.of(text))
                .action((screen, btn) -> {
                    Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();
                    StringSelection data = new StringSelection(text.getString());
                    cb.setContents(data, null);
                })
                .build());

        depth.push(0);
        for (final Text sibling : text.getSiblings()) {
            addRecursiveOptions(builder, depth, sibling);
            depth.push(depth.pop() + 1);
        }
        depth.pop();
    }
}

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

import io.wispforest.owo.ui.base.BaseUIModelScreen;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.DropdownComponent;
import io.wispforest.owo.ui.container.CollapsibleContainer;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.ScrollContainer;
import io.wispforest.owo.ui.core.Component;
import io.wispforest.owo.ui.core.HorizontalAlignment;
import io.wispforest.owo.ui.core.Insets;
import io.wispforest.owo.ui.core.Size;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.core.VerticalAlignment;

import static org.ayoree.chatimprover.ChatImprover.MOD_ID;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.ChatHudLine;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class LastMessagesScreen extends BaseUIModelScreen<FlowLayout> {
    private static final String ID_CONTAINER = "container";
    private final Screen m_parent;

    public LastMessagesScreen(Screen parent) {
        super(FlowLayout.class, DataSource.asset(Identifier.of(MOD_ID, "debug_messages_ui")));
        m_parent = parent;
    }

    @Override
    public void close() {
        client.setScreen(m_parent);
    }

    @Override
    protected void build(FlowLayout rootComponent) {
        final FlowLayout container = rootComponent.childById(FlowLayout.class, ID_CONTAINER);
        
        ChatHud chatHud = MinecraftClient.getInstance().inGameHud.getChatHud();
        List<ChatHudLine> messages = ((ChatHudAccessor) chatHud).getMessages();

        int i = 0;
        for (ChatHudLine chatHudLine : messages) {
            Stack<Integer> depth = new Stack<Integer>();
            addRecursiveOptions(container, depth, chatHudLine.content());

            if (++i >= 50)
                break;
        }
    }

    static private void addRecursiveOptions(final FlowLayout container, Stack<Integer> depth, final Text text) {
        final List<Text> siblings = text.getSiblings();
        String curIndex = "Main";
        if (!depth.empty()) {
            final int i = depth.lastElement();
            curIndex = "[" + i + "]";
        }

        Text textNoStyle = removeMouseStyles(text.copy());

        FlowLayout flow = Containers.horizontalFlow(Sizing.fill(100), Sizing.content());
        flow.alignment(HorizontalAlignment.LEFT, VerticalAlignment.CENTER);
        container.child(flow);
        ButtonComponent copyButton = Components.button(Text.of(curIndex), btn -> {
                Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();
                StringSelection data = new StringSelection(text.getString());
                cb.setContents(data, null);
            });
        copyButton.tooltip(Text.of("Копировать"));
        copyButton.margins(Insets.right(5));
        flow.child(copyButton);
        if (siblings.isEmpty())
            flow.child(Components.label(textNoStyle));
        else {
            CollapsibleContainer collapsible = Containers.collapsible(Sizing.fill(100), Sizing.content(), textNoStyle, false);
            flow.child(collapsible);
            depth.push(0);
            for (final Text sibling : text.getSiblings()) {
                addRecursiveOptions(collapsible, depth, sibling);
                depth.push(depth.pop() + 1);
            }
            depth.pop();
        }
    }

    static private Text removeMouseStyles(MutableText text) {

        Style textStyle = text.getStyle();
        MutableText textNoStyle;
        if (textStyle.getHoverEvent() != null || textStyle.getClickEvent() != null) {
            textStyle = textStyle.withHoverEvent(null);
            textStyle = textStyle.withClickEvent(null);
            textNoStyle = text.copy().setStyle(textStyle);
        }
        else
            textNoStyle = text;

        final List<Text> siblings = text.getSiblings();
        for (int i = 0; i < siblings.size(); ++i) {
            final MutableText sibling = siblings.get(i).copy();
            siblings.set(i, removeMouseStyles(sibling));
        }

        return textNoStyle;
    }
}

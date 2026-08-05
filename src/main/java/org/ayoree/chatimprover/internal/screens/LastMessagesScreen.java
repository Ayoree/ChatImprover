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
import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.component.UIComponents;
import io.wispforest.owo.ui.container.CollapsibleContainer;
import io.wispforest.owo.ui.container.UIContainers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.HorizontalAlignment;
import io.wispforest.owo.ui.core.Insets;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.core.VerticalAlignment;

import static org.ayoree.chatimprover.ChatImprover.MOD_ID;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.multiplayer.chat.GuiMessage;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public class LastMessagesScreen extends BaseUIModelScreen<FlowLayout> {
    private static final String ID_CONTAINER = "container";
    private final Screen m_parent;

    public LastMessagesScreen(Screen parent) {
        super(FlowLayout.class, DataSource.asset(Identifier.fromNamespaceAndPath(MOD_ID, "debug_messages_ui")));
        m_parent = parent;
    }

    @Override
    public void onClose() {
        this.minecraft.gui.setScreen(m_parent);
    }

    @Override
    protected void build(FlowLayout rootComponent) {
        final FlowLayout container = rootComponent.childById(FlowLayout.class, ID_CONTAINER);
        
        ChatComponent chatHud = Minecraft.getInstance().gui.hud.getChat();
        List<GuiMessage> messages = ((ChatHudAccessor) chatHud).getMessages();

        int i = 0;
        for (GuiMessage chatHudLine : messages) {
            Stack<Integer> depth = new Stack<Integer>();
            addRecursiveOptions(container, depth, chatHudLine.content());

            if (++i >= 50)
                break;
        }
    }

    static private void addRecursiveOptions(final FlowLayout container, Stack<Integer> depth, final Component text) {
        final List<Component> siblings = text.getSiblings();
        String curIndex = "Main";
        if (!depth.empty()) {
            final int i = depth.lastElement();
            curIndex = "[" + i + "]";
        }

        Component textNoStyle = removeMouseStyles(text.copy());

        FlowLayout flow = UIContainers.horizontalFlow(Sizing.fill(100), Sizing.content());
        flow.alignment(HorizontalAlignment.LEFT, VerticalAlignment.CENTER);
        flow.margins(Insets.bottom(4));
        container.child(flow);
        ButtonComponent copyButton = UIComponents.button(Component.nullToEmpty(curIndex), btn -> {
                Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();
                StringSelection data = new StringSelection(text.getString());
                cb.setContents(data, null);
            });
        copyButton.tooltip(Component.nullToEmpty("Копировать"));
        copyButton.margins(Insets.right(5));
        flow.child(copyButton);
        if (siblings.isEmpty())
            flow.child(UIComponents.label(Component.literal("`").append(textNoStyle).append("`")));
        else {
            CollapsibleContainer collapsible = UIContainers.collapsible(Sizing.fill(100), Sizing.content(), textNoStyle, false);
            flow.child(collapsible);
            
            // Content only
            ButtonComponent copyButton2 = UIComponents.button(Component.literal("Content"), btn -> {
                Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();
                StringSelection data = new StringSelection(textNoStyle.plainCopy().getString());
                cb.setContents(data, null);
            });
            copyButton2.tooltip(Component.nullToEmpty("Копировать"));
            copyButton2.margins(Insets.right(5));
            LabelComponent label = UIComponents.label(Component.literal("`").append(textNoStyle.plainCopy()).append(Component.literal("`")));
            FlowLayout flow2 = UIContainers.horizontalFlow(Sizing.fill(100), Sizing.content());
            flow2.alignment(HorizontalAlignment.LEFT, VerticalAlignment.CENTER);
            flow2.margins(Insets.bottom(4));
            flow2.child(copyButton2);
            flow2.child(label);
            collapsible.child(flow2);

            depth.push(0);
            for (final Component sibling : text.getSiblings()) {
                addRecursiveOptions(collapsible, depth, sibling);
                depth.push(depth.pop() + 1);
            }
            depth.pop();
        }
    }

    static private Component removeMouseStyles(MutableComponent text) {

        Style textStyle = text.getStyle();
        MutableComponent textNoStyle;
        if (textStyle.getHoverEvent() != null || textStyle.getClickEvent() != null) {
            textStyle = textStyle.withHoverEvent(null);
            textStyle = textStyle.withClickEvent(null);
            textNoStyle = text.copy().setStyle(textStyle);
        }
        else
            textNoStyle = text;

        final List<Component> siblings = text.getSiblings();
        for (int i = 0; i < siblings.size(); ++i) {
            final MutableComponent sibling = siblings.get(i).copy();
            siblings.set(i, removeMouseStyles(sibling));
        }

        return textNoStyle;
    }
}

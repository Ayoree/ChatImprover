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

import io.wispforest.owo.ui.base.BaseUIModelScreen;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.Component;
import io.wispforest.owo.ui.core.HorizontalAlignment;
import io.wispforest.owo.ui.core.Insets;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.core.VerticalAlignment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import static org.ayoree.chatimprover.ChatImprover.CONFIG_CUSTOM_SCREEN;
import static org.ayoree.chatimprover.ChatImprover.MOD_ID;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import org.ayoree.chatimprover.internal.configs.customconfig.CustomScreenConfigCategory;
import org.ayoree.chatimprover.internal.configs.customconfig.CustomScreenConfigCommand;

public class ChatimproverCustomScreen extends BaseUIModelScreen<FlowLayout> {
    private static final String ID_CONTAINER = "container";
    private static final int COLUMNS_MAX_COUNT = 4;
    
    private final Screen m_parent;

    public ChatimproverCustomScreen(Screen parent) {
        super(FlowLayout.class, DataSource.asset(Identifier.of(MOD_ID, "custom_screen_ui")));
        m_parent = parent;
    }

    @Override
    public void close() {
        client.setScreen(m_parent);
    }

    @Override
    protected void build(FlowLayout rootComponent) {
        final FlowLayout container = rootComponent.childById(FlowLayout.class, ID_CONTAINER);
        final List<CustomScreenConfigCategory> elems = CONFIG_CUSTOM_SCREEN.entries();
        final List<FlowLayout> columns = new ArrayList<>(COLUMNS_MAX_COUNT);
        final Deque<FlowLayout> elemsWidgets = new ArrayDeque<>();
        
        for (int i = 0; i < COLUMNS_MAX_COUNT; ++i) {
            final FlowLayout column = Containers.verticalFlow(Sizing.fill(100 / COLUMNS_MAX_COUNT), Sizing.content());
            column.alignment(HorizontalAlignment.CENTER, VerticalAlignment.TOP);
            columns.add(column);
        }

        for (final CustomScreenConfigCategory elem : elems) {
            addElem(elemsWidgets, elem);
        }

        final int elemsInCol = elemsWidgets.size() / COLUMNS_MAX_COUNT;
        int elemsOverflowCount = elemsWidgets.size() % COLUMNS_MAX_COUNT;

        for (final FlowLayout col : columns) {
            for (int i = 0; i < elemsInCol; ++i) {
                col.child(elemsWidgets.pollFirst());
            }
            if (elemsOverflowCount-- > 0) {
                col.child(elemsWidgets.pollFirst());
            }
            container.child(col);
        }
    }

    private void addElem(final Deque<FlowLayout> elemsDeque, final CustomScreenConfigCategory elem) {
        final FlowLayout vertical = Containers.verticalFlow(Sizing.content(), Sizing.content());
        final Deque<ButtonComponent> commandsWidgets = new ArrayDeque<>();
        final List<CustomScreenConfigCommand> commands = elem.commands();

        vertical.alignment(HorizontalAlignment.CENTER, VerticalAlignment.TOP);
        
        for (final CustomScreenConfigCommand command : commands) {
            ButtonComponent button = Components.button(Text.of(command.name()), btn -> {
                final ClientPlayNetworkHandler networkHandler = MinecraftClient.getInstance().getNetworkHandler();
                if (command.command().startsWith("/"))
                    networkHandler.sendChatCommand(command.command().substring(1));
                else
                    networkHandler.sendChatMessage(command.command());
            });
            button.tooltip(Text.of(command.command()));
            commandsWidgets.add(button);
        }
        
        final LabelComponent label = Components.label(Text.of(elem.name()));
        label.margins(Insets.of(16, 8, 0 ,0));
        vertical.child(label);

        while (!commandsWidgets.isEmpty()) {
            final FlowLayout horizontal = Containers.horizontalFlow(Sizing.fill(100), Sizing.content());
            horizontal.alignment(HorizontalAlignment.CENTER, VerticalAlignment.TOP);
            addButtonsToContainer(horizontal, commandsWidgets);
            vertical.child(horizontal);
        }

        elemsDeque.add(vertical);
    }

    private void addButtonsToContainer(final FlowLayout container, final Deque<ButtonComponent> buttons) {
        if (buttons.isEmpty())
            return;

        final List<Component> childs = container.children();
        if (childs.isEmpty()) {
            container.child(buttons.pollFirst());
        }
        
        final int containerWidth = width / COLUMNS_MAX_COUNT - 40; // bad AF
        System.out.println("Column width: %s".formatted(containerWidth));
        int childsWidth = 0;
        for (final Component child : childs) {
            ButtonComponent btn = (ButtonComponent)child;
            childsWidth += getButtonWidth(btn);
        }

        while (!buttons.isEmpty() && containerWidth >= childsWidth + getButtonWidth(buttons.getFirst())) {
            final ButtonComponent btn = buttons.pollFirst();
            container.child(btn);
            childsWidth += getButtonWidth(btn);
        }
    }

    // bad AF
    private int getButtonWidth(ButtonComponent btn) {
        var textRenderer = this.client.textRenderer;
        int w = textRenderer.getWidth(btn.getMessage()) + 8;
        System.out.println("btn '%s' size: %d".formatted(btn.getMessage().getString(), w));
        return w;
    }
}


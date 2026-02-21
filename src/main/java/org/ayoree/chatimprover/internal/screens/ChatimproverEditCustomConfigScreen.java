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
import io.wispforest.owo.ui.component.TextBoxComponent;
import io.wispforest.owo.ui.container.CollapsibleContainer;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.HorizontalAlignment;
import io.wispforest.owo.ui.core.Insets;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.core.VerticalAlignment;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import static org.ayoree.chatimprover.ChatImprover.CONFIG_CUSTOM_SCREEN;
import static org.ayoree.chatimprover.ChatImprover.MOD_ID;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ayoree.chatimprover.api.mixin.FlowLayoutOperations;
import org.ayoree.chatimprover.internal.configs.customconfig.CustomScreenConfigCategory;
import org.ayoree.chatimprover.internal.configs.customconfig.CustomScreenConfigCommand;

public class ChatimproverEditCustomConfigScreen extends BaseUIModelScreen<FlowLayout> {
    private static final String ID_BTN_CLOSE = "btn_close";
    private static final String ID_BTN_SAVE = "btn_save";
    private static final String ID_BTN_ADD = "btn_add_more";
    private static final String ID_CONTAINER = "container";
    
    private final Screen m_parent;
    private String m_title = CONFIG_CUSTOM_SCREEN.title();
    private List<CustomScreenConfigCategory> m_entires = new ArrayList<>(CONFIG_CUSTOM_SCREEN.entries());
    private ButtonComponent m_btnSave;

    private List<Elem<CustomScreenConfigCategory>> m_configCategories = new ArrayList<>();
    private Map<Elem<CustomScreenConfigCategory>, List<Elem<CustomScreenConfigCommand>>> m_categoryCommands = new HashMap<>();

    public ChatimproverEditCustomConfigScreen(Screen parent) {
        super(FlowLayout.class, DataSource.asset(Identifier.of(MOD_ID, "custom_config_screen_ui")));
        m_parent = parent;
    }

    @Override
    public void close() {
        client.setScreen(m_parent);
    }

    @Override
    protected void build(FlowLayout rootComponent) {
        final FlowLayout container = rootComponent.childById(FlowLayout.class, ID_CONTAINER);
        m_btnSave = rootComponent.childById(ButtonComponent.class, ID_BTN_SAVE);
        m_btnSave.active(false);
        m_btnSave.onPress(btn -> { save(btn); });

        final TextBoxComponent titleBox = Components.textBox(Sizing.fill(), m_title);
        titleBox.setPlaceholder(Text.literal("Заголовок"));
        titleBox.onChanged().subscribe(newTitle -> {
            m_title = newTitle;
            m_btnSave.active(wasConfigChanged());
        });
        container.child(titleBox);

        for (final CustomScreenConfigCategory entry : m_entires) {
            addCategory(container, entry);
        }

        rootComponent.childById(ButtonComponent.class, ID_BTN_CLOSE)
            .onPress(btn -> { close(); });

        rootComponent.childById(ButtonComponent.class, ID_BTN_ADD)
            .onPress(btn -> {
                final CustomScreenConfigCategory configCategory = new CustomScreenConfigCategory();
                m_entires.add(configCategory);
                addCategory(container, configCategory);
                m_btnSave.active(wasConfigChanged());
            });
    }

    private void save(ButtonComponent btn) {
        CONFIG_CUSTOM_SCREEN.title(m_title);
        CONFIG_CUSTOM_SCREEN.entries(m_entires);
        CONFIG_CUSTOM_SCREEN.save();
        btn.active(false);
    }

    private boolean wasConfigChanged() {
        return true; // why not?
    }

    private void addCategory(final FlowLayout container, final CustomScreenConfigCategory configCategory) {
        final FlowLayout vertFlow = Containers.verticalFlow(Sizing.fill(), Sizing.content());
        final FlowLayout horFlow = Containers.horizontalFlow(Sizing.fill(), Sizing.content());
        final ButtonComponent btnUp = Components.button(Text.literal("↑"), null);
        final ButtonComponent btnDown = Components.button(Text.literal("↓"), null);
        final TextBoxComponent textBox = Components.textBox(Sizing.expand());
        final ButtonComponent btnRemove = Components.button(Text.literal("✖"), null);
        final CollapsibleContainer collapsible = Containers.collapsible(Sizing.fill(), Sizing.content(), Text.literal("Команды"), false);
        final ButtonComponent btnAddCommand = Components.button(Text.literal("Добавить"), null);
        final FlowLayout commandsContainer = Containers.verticalFlow(Sizing.fill(), Sizing.content());
        final FlowLayout afterCommandsContainer = Containers.verticalFlow(Sizing.fill(), Sizing.content());
        vertFlow.margins(Insets.top(8));
        horFlow.alignment(HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
        textBox.setPlaceholder(Text.literal("Название категории"));
        textBox.text(configCategory.name());
        btnUp.sizing(Sizing.fixed(22));
        btnDown.sizing(Sizing.fixed(22));
        btnRemove.sizing(Sizing.fixed(22));
        btnRemove.tooltip(Text.literal("Удалить категорию"));
        collapsible.margins(Insets.left(44));
        afterCommandsContainer.alignment(HorizontalAlignment.LEFT, VerticalAlignment.TOP);
        
        final List<CustomScreenConfigCommand> commands = configCategory.commands();
        final Elem<CustomScreenConfigCategory> prevConfigCategory = m_configCategories.isEmpty() ? null : m_configCategories.getLast();
        final Elem<CustomScreenConfigCategory> thisConfigCategory = new Elem<>(configCategory, vertFlow, btnUp, btnDown, prevConfigCategory, null);
        m_configCategories.add(thisConfigCategory);
        m_categoryCommands.put(thisConfigCategory, new ArrayList<>());
        
        if (prevConfigCategory != null)
            prevConfigCategory.next(thisConfigCategory);

        btnUp.onPress(btn -> onPressUp(thisConfigCategory));
        btnDown.onPress(btn -> onPressDown(thisConfigCategory));
        btnRemove.onPress(btn -> onRemove(thisConfigCategory));
        textBox.onChanged().subscribe(newVal -> {
            configCategory.name(newVal);
            m_btnSave.active(wasConfigChanged());
        });
        btnAddCommand.onPress(btn -> {
            final CustomScreenConfigCommand cmd = new CustomScreenConfigCommand();
            thisConfigCategory.val().commands().add(cmd);
            addCommand(commandsContainer, thisConfigCategory, cmd);
            m_btnSave.active(wasConfigChanged());
        });
        btnAddCommand.tooltip(Text.literal("Добавить команду"));

        for (final CustomScreenConfigCommand command : commands) {
            addCommand(commandsContainer, thisConfigCategory, command);
        }

        afterCommandsContainer.child(btnAddCommand);

        horFlow.child(btnUp);
        horFlow.child(btnDown);
        horFlow.child(textBox);
        horFlow.child(btnRemove);
        vertFlow.child(horFlow);

        collapsible.child(commandsContainer);
        collapsible.child(afterCommandsContainer);
        vertFlow.child(collapsible);

        container.child(vertFlow);
        
        updateButtons(thisConfigCategory);
        updateButtons(prevConfigCategory);
    }

    private void addCommand(final FlowLayout container, final Elem<CustomScreenConfigCategory> thisConfigCategory, final CustomScreenConfigCommand command) {
        final List<Elem<CustomScreenConfigCommand>> commands = m_categoryCommands.get(thisConfigCategory);
        final FlowLayout horFlow = Containers.horizontalFlow(Sizing.fill(), Sizing.content());
        final ButtonComponent btnUp = Components.button(Text.literal("↑"), null);
        final ButtonComponent btnDown = Components.button(Text.literal("↓"), null);
        final TextBoxComponent nameTextBox = Components.textBox(Sizing.fixed(60));
        final TextBoxComponent commandTextBox = Components.textBox(Sizing.expand());
        final ButtonComponent btnRemove = Components.button(Text.literal("✖"), null);
        horFlow.alignment(HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
        nameTextBox.setPlaceholder(Text.literal("Название"));
        nameTextBox.text(command.name());
        commandTextBox.setPlaceholder(Text.literal("Команда"));
        commandTextBox.text(command.command());
        btnUp.sizing(Sizing.fixed(22));
        btnDown.sizing(Sizing.fixed(22));
        btnRemove.sizing(Sizing.fixed(22));
        btnRemove.tooltip(Text.literal("Удалить команду"));
        
        final Elem<CustomScreenConfigCommand> prevCommand = commands.isEmpty() ? null : commands.getLast();
        final Elem<CustomScreenConfigCommand> thisCommand = new Elem<>(command, horFlow, btnUp, btnDown, prevCommand, null);
        commands.add(thisCommand);

        if (prevCommand != null)
            prevCommand.next(thisCommand);

        btnUp.onPress(btn -> onPressUp(thisConfigCategory, thisCommand));
        btnDown.onPress(btn -> onPressDown(thisConfigCategory, thisCommand));
        btnRemove.onPress(btn -> onRemove(thisConfigCategory, thisCommand));
        nameTextBox.onChanged().subscribe(newVal -> {
            thisCommand.val().name(newVal);
            m_btnSave.active(wasConfigChanged());
        });
        commandTextBox.onChanged().subscribe(newVal -> {
            thisCommand.val().command(newVal);
            m_btnSave.active(wasConfigChanged());
        });

        horFlow.child(btnUp);
        horFlow.child(btnDown);
        horFlow.child(nameTextBox);
        horFlow.child(commandTextBox);
        horFlow.child(btnRemove);
        container.child(horFlow);
        
        updateButtons(thisCommand);
        updateButtons(prevCommand);
    }

    private void onPressUp(final Elem<CustomScreenConfigCategory> thisConfigCategory) {
        final Elem<CustomScreenConfigCategory> prevConfigCategory = thisConfigCategory.prev();
        Collections.swap(m_configCategories, m_configCategories.indexOf(thisConfigCategory), m_configCategories.indexOf(prevConfigCategory));
        Collections.swap(m_entires, m_entires.indexOf(thisConfigCategory.val()), m_entires.indexOf(prevConfigCategory.val()));

        swapElems(thisConfigCategory, prevConfigCategory);
    }

    private void onPressUp(final Elem<CustomScreenConfigCategory> thisConfigCategory, final Elem<CustomScreenConfigCommand> thisCommand) {
        final List<Elem<CustomScreenConfigCommand>> thisCommands = m_categoryCommands.get(thisConfigCategory);
        final List<CustomScreenConfigCommand> realCommands = thisConfigCategory.val().commands();
        
        final Elem<CustomScreenConfigCommand> prevCommand = thisCommand.prev();
        Collections.swap(thisCommands, thisCommands.indexOf(thisCommand), thisCommands.indexOf(prevCommand));
        Collections.swap(realCommands, realCommands.indexOf(thisCommand.val()), realCommands.indexOf(prevCommand.val()));

        swapElems(thisCommand, prevCommand);
    }
    
    private void onPressDown(final Elem<CustomScreenConfigCategory> thisConfigCategory) {
        final Elem<CustomScreenConfigCategory> nextConfigCategory = thisConfigCategory.next();
        Collections.swap(m_configCategories, m_configCategories.indexOf(thisConfigCategory), m_configCategories.indexOf(nextConfigCategory));
        Collections.swap(m_entires, m_entires.indexOf(thisConfigCategory.val()), m_entires.indexOf(nextConfigCategory.val()));
        
        swapElems(thisConfigCategory, nextConfigCategory);
    }
    
    private void onPressDown(final Elem<CustomScreenConfigCategory> thisConfigCategory, final Elem<CustomScreenConfigCommand> thisCommand) {
        final List<Elem<CustomScreenConfigCommand>> thisCommands = m_categoryCommands.get(thisConfigCategory);
        final List<CustomScreenConfigCommand> realCommands = thisConfigCategory.val().commands();

        final Elem<CustomScreenConfigCommand> nextCommand = thisCommand.next();
        Collections.swap(thisCommands, thisCommands.indexOf(thisCommand), thisCommands.indexOf(nextCommand));
        Collections.swap(realCommands, realCommands.indexOf(thisCommand.val()), realCommands.indexOf(nextCommand.val()));

        swapElems(thisCommand, nextCommand);
    }

    private void onRemove(final Elem<CustomScreenConfigCategory> thisConfigCategory) {
        m_configCategories.remove(thisConfigCategory);
        m_categoryCommands.remove(thisConfigCategory);
        m_entires.remove(thisConfigCategory.val());

        removeElem(thisConfigCategory);
    }

    private void onRemove(final Elem<CustomScreenConfigCategory> thisConfigCategory, Elem<CustomScreenConfigCommand> command) {
        m_categoryCommands.get(thisConfigCategory).remove(command);
        thisConfigCategory.val().commands().remove(command.val());

        removeElem(command);
    }

    private <T> void swapElems(final Elem<T> a, final Elem<T> b) {

        final Elem<T> aNext = a.next();
        final Elem<T> aPrev = a.prev();
        final Elem<T> bNext = b.next();
        final Elem<T> bPrev = b.prev();

        if (aNext != null)
            aNext.prev(b);
        if (aPrev != null)
            aPrev.next(b);

        if (bNext != null)
            bNext.prev(a);
        if (bPrev != null)
            bPrev.next(a);

        a.next(bNext);
        a.prev(bPrev);
        b.next(aNext);
        b.prev(aPrev);

        if (a.next() == a)
            a.next(b);
        if (a.prev() == a)
            a.prev(b);
        if (b.next() == b)
            b.next(a);
        if (b.prev() == b)
            b.prev(a);

        final FlowLayout container = (FlowLayout)a.container().parent();
        ((FlowLayoutOperations) container).swapChilds(a.container(), b.container());

        updateButtons(a);
        updateButtons(b);

        m_btnSave.active(wasConfigChanged());
    }

    private <T> void removeElem(final Elem<T> elem) {
        if (elem.next() != null) {
            elem.next().prev(elem.prev());
            updateButtons(elem.next());
        }
        if (elem.prev() != null) {
            elem.prev().next(elem.next());
            updateButtons(elem.prev());
        }
        elem.next(null);
        elem.prev(null);
        final FlowLayout mainContainer = (FlowLayout)elem.container().parent();
        mainContainer.removeChild(elem.container());

        m_btnSave.active(wasConfigChanged());
    }

    private void updateButtons(final Elem<?> elem) {
        if (elem == null)
            return;
        elem.btnUp().active(elem.prev() != null);
        elem.btnDown().active(elem.next() != null);
    }

    private static class Elem<T> {
        private final T m_val;
        private final FlowLayout m_container;
        private final ButtonComponent m_btnUp;
        private final ButtonComponent m_btnDown;
        private Elem<T> m_prev;
        private Elem<T> m_next;

        Elem(final T val, final FlowLayout container, final ButtonComponent btnUp, final ButtonComponent btnDown, final Elem<T> prev, final Elem<T> next) {
            m_val = val;
            m_container = container;
            m_btnUp = btnUp;
            m_btnDown = btnDown;
            m_prev = prev;
            m_next = next;
        }

        final T val() { return m_val; }
        final FlowLayout container() { return m_container; }
        final ButtonComponent btnUp() { return m_btnUp; }
        final ButtonComponent btnDown() { return m_btnDown; }
        final Elem<T> prev() { return m_prev; }
        final void prev(final Elem<T> configCategory) { m_prev = configCategory; }
        final Elem<T> next() { return m_next; }
        final void next(final Elem<T> configCategory) { m_next = configCategory; }
    }
}

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
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.Sizing;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import static org.ayoree.chatimprover.ChatImprover.CONFIG_CUSTOM_SCREEN;
import static org.ayoree.chatimprover.ChatImprover.MOD_ID;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.ayoree.chatimprover.internal.FlowLayoutOperations;
import org.ayoree.chatimprover.internal.configs.customconfig.CustomScreenConfigEntry;

public class ChatimproverEditCustomConfigScreen extends BaseUIModelScreen<FlowLayout> {
    private static final String ID_BTN_CLOSE = "btn_close";
    private static final String ID_BTN_SAVE = "btn_save";
    private static final String ID_BTN_ADD = "btn_add_more";
    private static final String ID_CONTAINER = "container";
    
    private final Screen m_parent;
    private String m_title = CONFIG_CUSTOM_SCREEN.title();
    private List<CustomScreenConfigEntry> m_entires = new ArrayList<CustomScreenConfigEntry>();
    private ButtonComponent m_btnSave;

    private List<Elem> m_elems = new ArrayList<>();

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
        if (CONFIG_CUSTOM_SCREEN.entries() != null)
            m_entires = new ArrayList<>(CONFIG_CUSTOM_SCREEN.entries());

        final FlowLayout container = rootComponent.childById(FlowLayout.class, ID_CONTAINER);
        m_btnSave = rootComponent.childById(ButtonComponent.class, ID_BTN_SAVE);
        m_btnSave.active(false);
        m_btnSave.onPress(btn -> { save(btn); });

        TextBoxComponent titleBox = Components.textBox(Sizing.fill(), m_title);
        titleBox.setPlaceholder(Text.literal("Заголовок"));
        titleBox.tooltip(Text.of("Показывается сверху экрана"));
        titleBox.onChanged().subscribe(newTitle -> m_title = newTitle);
        container.child(titleBox);

        for (final CustomScreenConfigEntry entry : m_entires) {
            addEntry(container, entry);
        }

        rootComponent.childById(ButtonComponent.class, ID_BTN_CLOSE)
            .onPress(btn -> { close(); });

        rootComponent.childById(ButtonComponent.class, ID_BTN_ADD)
            .onPress(btn -> {
                final CustomScreenConfigEntry configEntry = new CustomScreenConfigEntry("Новая категория", new ArrayList<>());
                m_entires.add(configEntry);
                addEntry(container, configEntry);
            });
    }

    private void save(ButtonComponent btn) {
        CONFIG_CUSTOM_SCREEN.title(m_title);
        CONFIG_CUSTOM_SCREEN.entries(m_entires);
        CONFIG_CUSTOM_SCREEN.save();
        btn.active(false);
    }

    private void addEntry(final FlowLayout container, final CustomScreenConfigEntry configEntry) {
        final FlowLayout flow = Containers.horizontalFlow(Sizing.fill(), Sizing.content());
        final ButtonComponent btnUp = Components.button(Text.literal("↑"), null);
        final ButtonComponent btnDown = Components.button(Text.literal("↓"), null);
        final TextBoxComponent textBox = Components.textBox(Sizing.expand());
        final ButtonComponent btnRemove = Components.button(Text.literal("✖"), null);
        btnUp.sizing(Sizing.fixed(20));
        btnDown.sizing(Sizing.fixed(20));
        btnRemove.sizing(Sizing.fixed(20));
        
        final Elem prevElem = m_elems.isEmpty() ? null : m_elems.getLast();
        final Elem thisElem = new Elem(configEntry, flow, btnUp, btnDown, textBox, btnRemove, prevElem, null);
        if (prevElem != null)
            prevElem.next(thisElem);
        System.out.println("thisElem: " + thisElem);
        System.out.println("prevElem: " + prevElem);

        btnUp.onPress(btn -> onPressUp(thisElem));
        btnDown.onPress(btn -> onPressDown(thisElem));
        btnRemove.onPress(btn -> onRemove(thisElem));

        flow.child(btnUp);
        flow.child(btnDown);
        flow.child(textBox);
        flow.child(btnRemove);
        container.child(flow);
        
        updateButtons(thisElem);
        updateButtons(prevElem);

        m_elems.add(thisElem);
    }

    private void onPressUp(final Elem thisElem) {
        final Elem prevElem = thisElem.prev();
        Collections.swap(m_elems, m_elems.indexOf(thisElem), m_elems.indexOf(prevElem));
        Collections.swap(m_entires, m_entires.indexOf(thisElem.configEntry()), m_entires.indexOf(prevElem.configEntry()));

        if (prevElem.prev() != null)
            prevElem.prev().next(thisElem);
        prevElem.next(thisElem.next());
        thisElem.prev(prevElem.prev());
        if (thisElem.next() != null)
            thisElem.next().prev(prevElem);
        thisElem.next(prevElem);
        prevElem.prev(thisElem);

        final FlowLayout globalContainer = (FlowLayout)thisElem.container().parent();
        ((FlowLayoutOperations) globalContainer).swapChilds(thisElem.container(), prevElem.container());

        updateButtons(thisElem);
        updateButtons(prevElem);
    }

    private void onPressDown(final Elem thisElem) {
        final Elem nextElem = thisElem.next();
        Collections.swap(m_elems, m_elems.indexOf(thisElem), m_elems.indexOf(nextElem));
        Collections.swap(m_entires, m_entires.indexOf(thisElem.configEntry()), m_entires.indexOf(nextElem.configEntry()));

        if (nextElem.next() != null)
            nextElem.next().prev(thisElem);
        nextElem.prev(thisElem.prev());
        thisElem.next(nextElem.next());
        if (thisElem.prev() != null)
            thisElem.prev().next(nextElem);
        thisElem.prev(nextElem);
        nextElem.next(thisElem);

        final FlowLayout globalContainer = (FlowLayout)thisElem.container().parent();
        ((FlowLayoutOperations) globalContainer).swapChilds(thisElem.container(), nextElem.container());

        updateButtons(thisElem);
        updateButtons(nextElem);
    }

    private void onRemove(final Elem elem) {
        m_elems.remove(elem);
        m_entires.remove(elem.configEntry());

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
        final FlowLayout globalContainer = (FlowLayout)elem.container().parent();
        globalContainer.removeChild(elem.container());
    }

    private void updateButtons(final Elem elem) {
        if (elem == null)
            return;
        elem.btnUp().active(elem.prev() != null);
        elem.btnDown().active(elem.next() != null);
    }

    private static class Elem {
        private final CustomScreenConfigEntry m_configEntry;
        private final FlowLayout m_container;
        private final ButtonComponent m_btnUp;
        private final ButtonComponent m_btnDown;
        private final TextBoxComponent m_textBox;
        private final ButtonComponent m_btnRemove;
        private Elem m_prev;
        private Elem m_next;

        public Elem(CustomScreenConfigEntry configEntry, FlowLayout container, ButtonComponent btnUp, ButtonComponent btnDown, TextBoxComponent textBox, ButtonComponent btnRemove, Elem prev, Elem next) {
            m_configEntry = configEntry;
            m_container = container;
            m_btnUp = btnUp;
            m_btnDown = btnDown;
            m_textBox = textBox;
            m_btnRemove = btnRemove;
            m_prev = prev;
            m_next = next;
        }

        CustomScreenConfigEntry configEntry() { return m_configEntry; }
        FlowLayout container() { return m_container; }
        ButtonComponent btnUp() { return m_btnUp; }
        ButtonComponent btnDown() { return m_btnDown; }
        TextBoxComponent textBox() { return m_textBox; }
        ButtonComponent btnRemove() { return m_btnRemove; }
        Elem prev() { return m_prev; }
        void prev(Elem elem) { m_prev = elem; }
        Elem next() { return m_next; }
        void next(Elem elem) { m_next = elem; }
    };
}

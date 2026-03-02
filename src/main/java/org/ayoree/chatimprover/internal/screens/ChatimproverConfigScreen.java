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

import io.wispforest.owo.config.Option;
import io.wispforest.owo.ui.base.BaseUIModelScreen;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.CheckboxComponent;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.TextBoxComponent;
import io.wispforest.owo.ui.container.FlowLayout;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import static org.ayoree.chatimprover.ChatImprover.CONFIG;
import static org.ayoree.chatimprover.ChatImprover.MOD_ID;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.ayoree.chatimprover.api.ChatMessage;
import org.ayoree.chatimprover.api.Filter;
import org.ayoree.chatimprover.internal.factories.ChatMessageFactory;
import org.ayoree.chatimprover.internal.factories.FilterFactory;

import com.google.common.collect.ImmutableList;

public class ChatimproverConfigScreen extends BaseUIModelScreen<FlowLayout> {
    private static final String ID_CONTAINER = "container";
    private static final String ID_BTN_SAVE = "btn_save";
    private static final String ID_BTN_CLOSE = "btn_close";
    @SuppressWarnings("null")
    private static final ImmutableList<Option.Key> CHECKBOXES_KEYS = ImmutableList.of(CONFIG.keys.isBlockTrash, CONFIG.keys.rightclickMenu, CONFIG.keys.isImproveMessages, CONFIG.keys.chatButtons);
    private static final ImmutableList<Option.Key> STRING_KEYS = ImmutableList.of(CONFIG.keys.senderSymbol, CONFIG.keys.receiverSymbol);
    
    private final Screen m_parent;
    private Map<Option.Key, Boolean> m_boolSettings = new HashMap<>();
    private Map<Option.Key, String> m_stringSettings = new HashMap<>();
    private Set<String> m_disabledAddons = new HashSet<>(CONFIG.disabledAddons());
    private ButtonComponent m_btnSave;

    public ChatimproverConfigScreen(Screen parent) {
        super(FlowLayout.class, DataSource.asset(Identifier.of(MOD_ID, "config_ui")));
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

        for (final Option.Key key : CHECKBOXES_KEYS) {
            final Option<Boolean> opt = CONFIG.optionForKey(key);
            final CheckboxComponent checkbox = rootComponent.childById(CheckboxComponent.class, key.name());
            final boolean val = opt.value();
            m_boolSettings.put(key, val);
            checkbox.checked(val);
            checkbox.onChanged(value -> onBooleanChanged(key, value));
        }

        for (final Option.Key key : STRING_KEYS) {
            final Option<String> opt = CONFIG.optionForKey(key);
            final TextBoxComponent textbox = rootComponent.childById(TextBoxComponent.class, key.name());
            final String val = opt.value();
            m_stringSettings.put(key, val);
            textbox.text(val);
            textbox.onChanged().subscribe(value -> onStringChanged(key, value));
        }

        rootComponent.childById(ButtonComponent.class, ID_BTN_CLOSE)
            .onPress(btn -> { close(); });

        // Creating addons list
        final Set<String> addons = new HashSet<>();
        {
            final ArrayList<ChatMessage.Provider> providers = ChatMessageFactory.getAllProviders();
            for(final ChatMessage.Provider provider : providers)
                addons.add(provider.addonID().get());
        }
        {
            final ArrayList<Filter.Provider> providers = FilterFactory.getAllProviders();
            for(final Filter.Provider provider : providers)
                addons.add(provider.addonID().get());
        }

        for (final String addonId : addons) {
            ModContainer modContainer = FabricLoader.getInstance().getModContainer(addonId).get();
            String addonName = modContainer.getMetadata().getName();
            CheckboxComponent checkbox = Components.checkbox(Text.of(addonName));
            if (!m_disabledAddons.contains(addonId))
                checkbox.checked(true);
            checkbox.onChanged(value -> onAddonChanged(addonId, value));
            container.child(checkbox);
        }
    }

    private void save(ButtonComponent btn) {
        for (final Entry<Option.Key, Boolean> entry : m_boolSettings.entrySet()) {
            Option<Boolean> boolOpt = CONFIG.optionForKey(entry.getKey());
            boolOpt.set(entry.getValue());
        }
        CONFIG.disabledAddons(m_disabledAddons);
        CONFIG.save();
        btn.active(false);
    }

    void onBooleanChanged(final Option.Key key, final boolean value) {
        m_boolSettings.put(key, value);
        m_btnSave.active(wasConfigChanged());
    };

    void onStringChanged(final Option.Key key, final String value) {
        m_stringSettings.put(key, value);
        m_btnSave.active(wasConfigChanged());
    };

    void onAddonChanged(final String addon, final boolean value) {
        if (value)
            m_disabledAddons.remove(addon);
        else
            m_disabledAddons.add(addon);
        m_btnSave.active(wasConfigChanged());
    };

    boolean wasConfigChanged() {
        return (
            CONFIG.isBlockTrash() != m_boolSettings.get(CONFIG.keys.isBlockTrash) ||
            CONFIG.isImproveMessages() != m_boolSettings.get(CONFIG.keys.isImproveMessages) ||
            CONFIG.chatButtons() != m_boolSettings.get(CONFIG.keys.chatButtons) ||
            CONFIG.rightclickMenu() != m_boolSettings.get(CONFIG.keys.rightclickMenu) ||
            CONFIG.senderSymbol() != m_stringSettings.get(CONFIG.keys.senderSymbol) ||
            CONFIG.receiverSymbol() != m_stringSettings.get(CONFIG.keys.receiverSymbol) ||
            !CONFIG.disabledAddons().equals(m_disabledAddons)
        );
    }
}

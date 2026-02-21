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

package org.ayoree.chatimprover.mixin;

import java.util.Collections;
import java.util.List;

import org.ayoree.chatimprover.api.mixin.FlowLayoutOperations;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.Component;

@Mixin(FlowLayout.class)
public abstract class FlowLayoutMixin implements FlowLayoutAccessor, FlowLayoutOperations {

    @Shadow(remap = false)
    protected List<Component> children;

    @Override
    public List<Component> getChildrens() {
        return this.children;
    }

    @Unique
    public void swapChilds(Component a, Component b) {
        if (a == null || b == null)
            return;

        final int index1 = this.children.indexOf(a);
        final int index2 = this.children.indexOf(b);

        if (index1 == -1 || index2 == -1) {
            return;
        }

        if (index1 != index2) {
            Collections.swap(this.children, index1, index2);
            ((BaseParentComponentInvoker) this).invokeUpdateLayout();
        }
    }
}

/*
 * This file is part of CraftBorders.
 *
 * Copyright © 2013-2014 Visual Illusions Entertainment
 *
 * CraftBorders is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * CraftBorders is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with CraftBorders.
 * If not, see http://www.gnu.org/licenses/gpl.html.
 */
package net.visualillusionsent.craftborders;

import net.canarymod.api.entity.living.humanoid.Player;
import net.visualillusionsent.minecraft.plugin.MessageTranslator;

/**
 * @author Jason (darkdiplomat)
 */
public final class Guardian extends MessageTranslator {
    protected Guardian(CraftBorders borders, String defaultLocale) {
        super(borders, defaultLocale, borders.updateLang());
    }

    public final void wisper(Player player, String key) {
        player.notice(localeTranslate(key, player.getLocale()));
    }
}

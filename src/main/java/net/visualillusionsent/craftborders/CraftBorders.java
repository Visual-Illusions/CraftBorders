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
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.
 * If not, see http://www.gnu.org/licenses/gpl.html.
 */
package net.visualillusionsent.craftborders;

import net.canarymod.Canary;
import net.canarymod.api.world.World;
import net.canarymod.api.world.position.Position;
import net.visualillusionsent.minecraft.plugin.canary.VisualIllusionsCanaryPlugin;
import net.visualillusionsent.utils.PropertiesFile;

public final class CraftBorders extends VisualIllusionsCanaryPlugin {

    private final PropertiesFile bordercfg;

    public CraftBorders() {
        bordercfg = new PropertiesFile("config/CraftBorders/borders.cfg");
        checkConfig();
    }

    @Override
    public final boolean enable() {
        try {
            new CraftBorderListener(this);
        }
        catch (Exception ex) {
            getLogman().error("CraftBorders failed to enable...", ex);
            return false;
        }
        return true;
    }

    final String serverLocale() {
        return bordercfg.getString("server.locale", "en_US");
    }

    final Position getWorldSpawn(String world_name) {
        World world = Canary.getServer().getWorld(world_name);
        return world.getSpawnLocation();
    }

    final double getWorldRadius(String world) {
        if (bordercfg.containsKey(world.concat(".radius"))) {
            return bordercfg.getInt(world.concat(".radius"));
        }
        return bordercfg.getInt("default.world.radius");
    }

    final int getWorldHeight(String world) {
        if (bordercfg.containsKey(world.concat(".height"))) {
            return bordercfg.getInt(world.concat(".height"));
        }
        return bordercfg.getInt("default.world.height");
    }

    final boolean pushBack() {
        return bordercfg.getBoolean("push.back");
    }

    final float outsideDamage() {
        return bordercfg.getFloat("outside.damage");
    }

    final boolean updateLang() {
        return bordercfg.getBoolean("update.lang");
    }

    final boolean reloadConfig() {
        try {
            bordercfg.reload();
            checkConfig();
            return true;
        }
        catch (Exception ex) {
            return false;
        }
    }

    private void checkConfig() {
        if (bordercfg.getHeaderLines().isEmpty()) {
            bordercfg.addHeaderLines("CraftBorders Configuration File", "For each world radius add [worldname].radius=[radius] Ex: default_NORMAL.radius=5000");
        }
        bordercfg.getString("server.locale", "en_US");
        bordercfg.getString("update.lang", "yes");
        bordercfg.getString("push.back", "yes");
        bordercfg.setComments("push.back", "Sets whether the push the player back or damage the player");
        bordercfg.getFloat("outside.damage", 1.0F);
        bordercfg.setComments("outside.damage", "The amount of damage to cause each step taken outside the border. Default: 1.0F");
        bordercfg.getInt("default.world.radius", 5000);
        bordercfg.getInt("default.world.height", 300);
        bordercfg.setComments("default.world.height", "The Y height limit");
        bordercfg.setComments("default.world.radius", "The default radius to use for worlds not listed in the config");
        /* Adding default world stuff for quick setup */
        bordercfg.getInt("default_NORMAL.radius", 5000);
        bordercfg.getInt("default_NORMAL.height", 300);
        bordercfg.getInt("default_NETHER.radius", 2500);
        bordercfg.getInt("default_NETHER.height", 300);
        bordercfg.getInt("default_END.radius", 1200);
        bordercfg.getInt("default_END.height", 300);
        bordercfg.save();
    }
}

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

import net.canarymod.api.entity.living.humanoid.Player;
import net.canarymod.api.entity.vehicle.Vehicle;
import net.canarymod.tasks.ServerTask;
import net.canarymod.tasks.TaskOwner;

/**
 * @author Jason (darkdiplomat)
 */
public final class DelayedRespawn extends ServerTask {
    private final Player player;
    private final Vehicle vehicle;

    public DelayedRespawn(TaskOwner owner, Player player, Vehicle vehicle) {
        super(owner, 1); // Execute Next Tick
        this.player = player;
        this.vehicle = vehicle;
    }

    @Override
    public final void run() {
        if (player != null) {
            player.teleportTo(((CraftBorders) this.getOwner()).getWorldSpawn(player.getWorld().getFqName()));
        }
        if (vehicle != null) {
            vehicle.teleportTo(((CraftBorders) this.getOwner()).getWorldSpawn(vehicle.getWorld().getFqName()));
            if (player != null) {
                player.mount(vehicle); // reconnect
            }
        }
    }
}

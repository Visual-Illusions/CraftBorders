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

import net.canarymod.Canary;
import net.canarymod.api.DamageType;
import net.canarymod.api.entity.living.humanoid.Player;
import net.canarymod.api.entity.vehicle.Vehicle;
import net.canarymod.api.world.position.Vector3D;
import net.canarymod.chat.MessageReceiver;
import net.canarymod.commandsys.Command;
import net.canarymod.commandsys.CommandDependencyException;
import net.canarymod.hook.HookHandler;
import net.canarymod.hook.entity.VehicleEnterHook;
import net.canarymod.hook.entity.VehicleMoveHook;
import net.canarymod.hook.player.BlockDestroyHook;
import net.canarymod.hook.player.BlockPlaceHook;
import net.canarymod.hook.player.PlayerMoveHook;
import net.canarymod.plugin.PluginListener;
import net.visualillusionsent.minecraft.plugin.canary.VisualIllusionsCanaryPluginInformationCommand;

import java.util.HashMap;

public class CraftBorderListener extends VisualIllusionsCanaryPluginInformationCommand implements PluginListener {

    private final CraftBorders borders;
    private final Guardian guardian;
    private final HashMap<String, Long> lastWarn = new HashMap<String, Long>(); // Reduce Warn Spam

    public CraftBorderListener(CraftBorders craftBorders) throws CommandDependencyException {
        super(craftBorders);
        craftBorders.registerListener(this);
        craftBorders.registerCommands(this, false);
        this.borders = craftBorders;
        this.guardian = new Guardian(craftBorders);
    }

    private void schedule(Player player, Vehicle vehicle) {
        Canary.getServer().addSynchronousTask(new DelayedRespawn(borders, player, vehicle));
    }

    @HookHandler
    public final void onPlayerHitTheGodDamnWall(PlayerMoveHook hook) {
        final Player player = hook.getPlayer();
        if (outside(player.getWorld().getFqName(), hook.getTo(), 0)) {
            if (borders.pushBack()) {
                if (outside(player.getWorld().getFqName(), hook.getTo(), 2)) { // Stuck
                    guardian.wisper(player, "outside.border.respawn");
                    schedule(player, null);
                }
                else {
                    warn(player, false);
                }
                hook.setCanceled();
            }
            else {
                if (outside(player.getWorld().getFqName(), hook.getTo(), 15)) {
                    guardian.wisper(player, "outside.border.killed");
                    player.kill();
                }
                else {
                    warn(player, true);
                }
                punch(player);
            }
        }
    }

    @HookHandler
    public final void onVehicleHitTheGodDamnWall(VehicleMoveHook hook) {
        Vehicle vehicle = hook.getVehicle();
        if (vehicle.isEmpty() || !vehicle.getPassenger().isPlayer()) {
            return; // Don't worry about empty or non-player passenger
        }
        Player player = (Player) vehicle.getPassenger();
        if (outside(vehicle.getWorld().getFqName(), hook.getTo(), 0)) {
            if (borders.pushBack()) {
                if (outside(vehicle.getWorld().getFqName(), hook.getTo(), 2)) { // Stuck
                    guardian.wisper(player, "outside.border.respawn");
                    schedule(player, vehicle);
                }
                else {
                    warn(player, false);
                }
                hook.setCanceled();
            }
            else {
                if (outside((hook.getVehicle().getPassenger()).getWorld().getFqName(), hook.getTo(), 15)) {
                    player.kill();
                    vehicle.destroy();
                }
                else {
                    warn(player, true);
                }
                punch(player);
            }
        }
    }

    @HookHandler
    public final void onPlayerTryToGetOutSideTheGodDamnWall(VehicleEnterHook hook) {
        if (outside(hook.getVehicle().getWorld().getFqName(), hook.getVehicle().getLocation(), 0)) {
            hook.setCanceled();
        }
    }

    @HookHandler
    public final void onPlayerDestroyOutsideTheGodDamnWall(BlockDestroyHook hook) {
        if (outside(hook.getBlock().getWorld().getFqName(), hook.getBlock().getLocation(), 0)) {
            hook.setCanceled();
        }
    }

    @HookHandler
    public final void onPlayerPlaceOutsideTheGodDamnWall(BlockPlaceHook hook) {
        if (outside(hook.getBlockPlaced().getWorld().getFqName(), hook.getBlockPlaced().getLocation(), 0)) {
            hook.setCanceled();
        }
    }

    private boolean outside(String world, Vector3D vec, int offset) { //offset is for stranded deep in the outside
        return vec.getDistance(borders.getWorldSpawn(world)) >= (borders.getWorldRadius(world) + offset) || vec.getBlockY() >= (borders.getWorldHeight(world) + offset);
    }

    private void warn(Player player, boolean deathWarn) {
        if (lastWarn.containsKey(player.getName())) {
            if ((lastWarn.get(player.getName()) + 5000) >= System.currentTimeMillis()) {
                return;
            }
        }
        if (deathWarn) {
            guardian.wisper(player, "outside.border.warn.death");
            lastWarn.put(player.getName(), System.currentTimeMillis());
        }
        guardian.wisper(player, "outside.border.warn");
        lastWarn.put(player.getName(), System.currentTimeMillis());
    }

    private void punch(Player player) {
        player.getCapabilities().setInvulnerable(false);
        player.dealDamage(DamageType.GENERIC, borders.outsideDamage());
    }

    @Command(aliases = { "craftborders" },
            description = "Displays plugin information",
            permissions = { "" },
            toolTip = "/craftborders [reload]")
    public final void infoCommand(MessageReceiver msgrec, String[] args) {
        if (args.length > 1 && args[1].equals("reload")) {
            if (borders.reloadConfig()) {
                msgrec.notice("CraftBorders Configuration reloaded.");
            }
            else {
                msgrec.notice("Reload failed...");
            }
        }
        else {
            super.sendInformation(msgrec);
        }
    }
}

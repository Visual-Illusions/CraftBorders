/*
 * This file is part of CraftBorders.
 *
 * Copyright © 2012 Visual Illusions Entertainment
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
import net.canarymod.api.world.position.Vector3D;
import net.canarymod.chat.Colors;
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
import net.visualillusionsent.utils.VersionChecker;

import java.util.HashMap;

public class CraftBorderListener extends VisualIllusionsCanaryPluginInformationCommand implements PluginListener {

    private final CraftBorders cborders;
    private final HashMap<Player, Long> lastWarn = new HashMap<Player, Long>(); // Reduce Spam

    public CraftBorderListener(CraftBorders craftBorders) {
        super(craftBorders);
        Canary.hooks().registerListener(this, craftBorders);
        this.cborders = craftBorders;

        try {
            Canary.commands().registerCommands(this, plugin, false);
        } catch (CommandDependencyException ex) {
        }
    }

    @HookHandler
    public final void onPlayerHitTheGodDamnWall(PlayerMoveHook hook) {
        if (outside(hook.getPlayer().getWorld().getFqName(), hook.getTo(), 0)) {
            if (cborders.pushBack()) {
                if (outside(hook.getPlayer().getWorld().getFqName(), hook.getTo(), 3)) {
                    hook.getPlayer().notice("You wake up at spawn in a daze, only remembering a faint voice having said \"You don't belong out here and need to go home...\"");
                    hook.getPlayer().teleportTo(cborders.getWorldSpawn(hook.getPlayer().getWorld().getFqName()));
                } else {
                    if(shouldWarn(hook.getPlayer())){
                        hook.getPlayer().notice("A faint voice whispers to you... \"You need to turn back, you don't belong out here\"");
                        lastWarn.put(hook.getPlayer(), System.currentTimeMillis());
                    }
                    hook.setCanceled();
                }
            } else {
                if (outside(hook.getPlayer().getWorld().getFqName(), hook.getTo(), 15)) {
                    hook.getPlayer().kill();
                } else {
                    if(shouldWarn(hook.getPlayer())){
                        hook.getPlayer().notice("A faint voice whispers to you... \"It's dangerous to be out here. Turn back before you die.\"");
                        lastWarn.put(hook.getPlayer(), System.currentTimeMillis());
                    }
                    hook.getPlayer().getCapabilities().setInvulnerable(false);
                    hook.getPlayer().dealDamage(DamageType.GENERIC, cborders.outsideDamage());
                }
            }
        }
    }

    @HookHandler
    public final void onVehicleHitTheGodDamnWall(VehicleMoveHook hook) {
        if (hook.getVehicle().isEmpty()) {
            return; // Don't worry about empty vehicles
        }
        if (outside(hook.getVehicle().getWorld().getFqName(), hook.getTo(), 0)) {
            if (cborders.pushBack()) {
                if (outside(hook.getVehicle().getWorld().getFqName(), hook.getTo(), 3)) {
                    hook.getVehicle().teleportTo(cborders.getWorldSpawn(hook.getVehicle().getWorld().getFqName()));
                    hook.getVehicle().getPassenger().teleportTo(cborders.getWorldSpawn(hook.getVehicle().getWorld().getFqName()));
                    if (hook.getVehicle().getPassenger().isPlayer()) {
                        if(shouldWarn((Player) hook.getVehicle().getPassenger())){
                           ((Player) hook.getVehicle().getPassenger()).notice("You wake up at spawn in a daze, only remembering a faint voice having said \"You don't belong out here and need to go home...\"");
                            lastWarn.put((Player) hook.getVehicle().getPassenger(), System.currentTimeMillis());
                        }
                    }
                } else {
                    if (hook.getVehicle().getPassenger().isPlayer()) {
                        ((Player) hook.getVehicle().getPassenger()).notice("A faint voice whispers to you... \"You need to turn back, you don't belong out here\"");
                    }
                    hook.setCanceled();
                }
            } else {
                if (hook.getVehicle().getPassenger().isPlayer()) {
                    if (outside((hook.getVehicle().getPassenger()).getWorld().getFqName(), hook.getTo(), 15)) {
                        ((Player) hook.getVehicle().getPassenger()).kill();
                    } else {
                        if(shouldWarn((Player) hook.getVehicle().getPassenger())){
                            ((Player) hook.getVehicle().getPassenger()).notice("A faint voice whispers to you... \"It's dangerous to be out here. Turn back before you die.\"");
                            lastWarn.put((Player) hook.getVehicle().getPassenger(), System.currentTimeMillis());
                        }
                        ((Player) hook.getVehicle().getPassenger()).getCapabilities().setInvulnerable(false);
                        ((Player) hook.getVehicle().getPassenger()).dealDamage(DamageType.GENERIC, cborders.outsideDamage());
                    }
                }
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
    public final void onPlayerDestoryOutsideTheGodDamnWall(BlockDestroyHook hook) {
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

    private final boolean outside(String world, Vector3D vec, int offset) { //offset is for stranded deep in the outside
        return vec.getDistance(cborders.getWorldSpawn(world)) >= (cborders.getWorldRadius(world) + offset) || vec.getBlockY() >= (cborders.getWorldHeight(world) + offset);
    }

    private final boolean shouldWarn(Player player){
        if(lastWarn.containsKey(player)){
           return (lastWarn.get(player) + 5000) < System.currentTimeMillis();
        }
        return true;
    }

    @Command(aliases = {"craftborders"},
            description = "Displays plugin information",
            permissions = {""},
            toolTip = "CraftBorders Information Command")
    public final void infoCommand(MessageReceiver msgrec, String[] args) {
        for (String msg : about) {
            if (msg.equals("$VERSION_CHECK$")) {
                VersionChecker vc = plugin.getVersionChecker();
                Boolean islatest = vc.isLatest();
                if (islatest == null) {
                    msgrec.message(center(Colors.GRAY + "VersionCheckerError: " + vc.getErrorMessage()));
                } else if (!islatest) {
                    msgrec.message(center(Colors.GRAY + vc.getUpdateAvailibleMessage()));
                } else {
                    msgrec.message(center(Colors.LIGHT_GREEN + "Latest Version Installed"));
                }
            } else {
                msgrec.message(msg);
            }
        }
    }
    //TWEAK
}

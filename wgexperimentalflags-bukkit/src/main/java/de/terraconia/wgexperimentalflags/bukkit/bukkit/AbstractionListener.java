/*
 * WorldGuardExperimentalFlags, a suite of tools for the Minecraft plugin WorldGuard
 * Copyright (C) WorldGuardExperimentalFlags team and contributors
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package de.terraconia.wgexperimentalflags.bukkit.bukkit;

import com.google.common.base.Predicate;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.bukkit.cause.Cause;
import com.sk89q.worldguard.bukkit.event.DelegateEvent;
import com.sk89q.worldguard.bukkit.event.block.BreakBlockEvent;
import com.sk89q.worldguard.bukkit.event.block.PlaceBlockEvent;
import com.sk89q.worldguard.bukkit.event.block.UseBlockEvent;
import com.sk89q.worldguard.bukkit.internal.WGMetadata;
import com.sk89q.worldguard.bukkit.util.Materials;
import com.sk89q.worldguard.commands.CommandUtils;
import com.sk89q.worldguard.protection.association.RegionAssociable;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.MapFlag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import de.terraconia.wgexperimentalflags.bukkit.ExperimentalFlags;
import de.terraconia.wgexperimentalflags.bukkit.ExperimentalFlagsPlugin;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AbstractionListener extends ExperimentalAbstractListener {
    private static final String DENY_MESSAGE_KEY = "worldguard.region.lastMessage";
    private static final String DISEMBARK_MESSAGE_KEY = "worldguard.region.disembarkMessage";
    private static final int LAST_MESSAGE_DELAY = 500;

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    public void onUseBlock(final UseBlockEvent event) {
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    public void onBreakBlock(final BreakBlockEvent event) {

        if (event.getResult() == Event.Result.ALLOW) return; // Don't care about events that have been pre-allowed
        if (!isRegionSupportEnabled(BukkitAdapter.adapt(event.getWorld()))) return; // Region support disabled

        final RegionQuery query = WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery();
        final RegionAssociable associable = createRegionAssociable(event.getCause());

        final BlockType worldEditType = BukkitAdapter.asBlockType(event.getEffectiveMaterial());
        event.filter((Predicate<Location>) target -> {
            boolean canBreak;
            String what;

            com.sk89q.worldedit.util.Location weLocation = BukkitAdapter.adapt(target);
            /* TNT */
            if (event.getCause().find(EntityType.PRIMED_TNT, EntityType.MINECART_TNT) != null) {
                canBreak = testMapBuild(query, BukkitAdapter.adapt(target), associable,
                        query.queryMapValue(BukkitAdapter.adapt(target), associable, ExperimentalFlags.BLOCK_BREAK_TYPE, worldEditType, Flags.BLOCK_PLACE),
                        combine(event, Flags.TNT));
                what = "use dynamite";

                /* Everything else */
            } else {
                canBreak = testMapBuild(query, BukkitAdapter.adapt(target), associable,
                        query.queryMapValue(BukkitAdapter.adapt(target), associable, ExperimentalFlags.BLOCK_BREAK_TYPE, worldEditType, Flags.BLOCK_PLACE),
                        combine(event));
                what = "break that block";
            }

            if (!canBreak) {
                tellErrorMessage(event, event.getCause(), target, what);
                return false;
            }

            return true;
        });
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    public void onPlaceBlock(final PlaceBlockEvent event) {
        if (event.getResult() == Event.Result.ALLOW) return; // Don't care about events that have been pre-allowed
        if (!isRegionSupportEnabled(BukkitAdapter.adapt(event.getWorld()))) return; // Region support disabled

        final Material type = event.getEffectiveMaterial();
        final RegionQuery query = WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery();
        final RegionAssociable associable = createRegionAssociable(event.getCause());

        // Don't check liquid flow unless it's enabled
        if (event.getCause().getRootCause() instanceof Block
                && Materials.isLiquid(type)
                && !WorldGuard.getInstance().getPlatform().getGlobalStateManager().get(BukkitAdapter.adapt(event.getWorld())).checkLiquidFlow) {
            return;
        }

        if (Materials.isFire(type)) {
            return;
        }

        BlockType effective = BukkitAdapter.asBlockType(type);
        if (effective == null) return;

        event.filter((Predicate<Location>) target -> {
            boolean canPlace = testMapBuild(query, BukkitAdapter.adapt(target), associable,
                    query.queryMapValue(BukkitAdapter.adapt(target), associable, ExperimentalFlags.BLOCK_PLACE_TYPE, effective, Flags.BLOCK_PLACE),
                    combine(event));

            if (!canPlace) {
                tellErrorMessage(event, event.getCause(), target, "place that block");
                return false;
            }

            return true;
        });
    }

    private boolean testMapBuild(RegionQuery query, com.sk89q.worldedit.util.Location location, RegionAssociable associable,
                                 StateFlag.State mapValue, StateFlag... flag) {
        if (flag.length == 0) {
            return StateFlag.test(StateFlag.combine(
                    StateFlag.denyToNone(query.queryState(location, associable, Flags.BUILD)), mapValue));
        }

        return StateFlag.test(StateFlag.combine(
                StateFlag.denyToNone(query.queryState(location, associable, Flags.BUILD)), mapValue,
                    query.queryState(location, associable, flag)));
    }

    /**
     * Combine the flags from a delegate event with an array of flags.
     *
     * <p>The delegate event's flags appear at the end.</p>
     *
     * @param event The event
     * @param flag An array of flags
     * @return An array of flags
     */
    private static StateFlag[] combine(DelegateEvent event, StateFlag... flag) {
        List<StateFlag> extra = event.getRelevantFlags();
        StateFlag[] flags = Arrays.copyOf(flag, flag.length + extra.size());
        for (int i = 0; i < extra.size(); i++) {
            flags[flag.length + i] = extra.get(i);
        }
        return flags;
    }

    /**
     * Tell a sender that s/he cannot do something 'here'.
     *
     * @param event the event
     * @param cause the cause
     * @param location the location
     * @param what what was done
     */
    private void tellErrorMessage(DelegateEvent event, Cause cause, Location location, String what) {
        if (event.isSilent() || cause.isIndirect()) {
            return;
        }

        Object rootCause = cause.getRootCause();

        if (rootCause instanceof Player) {
            Player player = (Player) rootCause;

            long now = System.currentTimeMillis();
            Long lastTime = WGMetadata.getIfPresent(player, DENY_MESSAGE_KEY, Long.class);
            if (lastTime == null || now - lastTime >= LAST_MESSAGE_DELAY) {
                RegionQuery query = WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery();
                LocalPlayer localPlayer = WorldGuardPlugin.inst().wrapPlayer(player);
                String message = query.queryValue(BukkitAdapter.adapt(location), localPlayer, Flags.DENY_MESSAGE);
                formatAndSendDenyMessage(what, localPlayer, message);
                WGMetadata.put(player, DENY_MESSAGE_KEY, now);
            }
        }
    }

    static void formatAndSendDenyMessage(String what, LocalPlayer localPlayer, String message) {
        if (message == null || message.isEmpty()) return;
        message = WorldGuard.getInstance().getPlatform().getMatcher().replaceMacros(localPlayer, message);
        message = CommandUtils.replaceColorMacros(message);
        localPlayer.printRaw(message.replace("%what%", what));
    }
}

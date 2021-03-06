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

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.BukkitWorldConfiguration;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.bukkit.cause.Cause;
import com.sk89q.worldguard.domains.Association;
import com.sk89q.worldguard.protection.association.Associables;
import com.sk89q.worldguard.protection.association.DelayedRegionOverlapAssociation;
import com.sk89q.worldguard.protection.association.RegionAssociable;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import de.terraconia.wgexperimentalflags.bukkit.ExperimentalFlagsPlugin;
import de.terraconia.wgexperimentalflags.bukkit.config.ConfigurationManager;
import de.terraconia.wgexperimentalflags.bukkit.config.WorldConfiguration;
import io.papermc.lib.PaperLib;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

class ExperimentalAbstractListener implements Listener {

    /**
     * Get the global configuration.
     *
     * @return the configuration
     */
    protected static ConfigurationManager getConfig() {
        return ExperimentalFlagsPlugin.getInstance().getConfiguration();
    }

    /**
     * Get the world configuration given a world.
     *
     * @param world The world to get the configuration for.
     * @return The configuration for {@code world}
     */
    protected static WorldConfiguration getWorldConfig(World world) {
        return getConfig().get(world);
    }

    /**
     * Get the world configuration given a player.
     *
     * @param player The player to get the wold from
     * @return The {@link WorldConfiguration} for the player's world
     */
    protected static WorldConfiguration getWorldConfig(LocalPlayer player) {
        return getWorldConfig((World) player.getExtent());
    }

    /**
     * Return whether region support is enabled.
     *
     * @param world the world
     * @return true if region support is enabled
     */
    protected static boolean isRegionSupportEnabled(World world) {
        return WorldGuard.getInstance().getPlatform().getGlobalStateManager().get(world).useRegions;
    }

    protected RegionAssociable createRegionAssociable(Cause cause) {
        Object rootCause = cause.getRootCause();

        if (!cause.isKnown()) {
            return Associables.constant(Association.NON_MEMBER);
        } else if (rootCause instanceof Player) {
            return WorldGuardPlugin.inst().wrapPlayer((Player) rootCause);
        } else if (rootCause instanceof OfflinePlayer) {
            return WorldGuardPlugin.inst().wrapOfflinePlayer((OfflinePlayer) rootCause);
        } else if (rootCause instanceof Entity) {
            RegionQuery query = WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery();
            final Entity entity = (Entity) rootCause;
            BukkitWorldConfiguration config =
                    (BukkitWorldConfiguration)WorldGuard.getInstance().getPlatform().getGlobalStateManager().get(BukkitAdapter.adapt(entity.getWorld()));
            Location loc;
            if (PaperLib.isPaper()  && config.usePaperEntityOrigin) {
                loc = entity.getOrigin();
                if (loc == null) {
                    loc = entity.getLocation();
                }
            } else {
                loc = entity.getLocation();
            }
            return new DelayedRegionOverlapAssociation(query, BukkitAdapter.adapt(loc),
                    config.useMaxPriorityAssociation);
        } else if (rootCause instanceof Block) {
            RegionQuery query = WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery();
            Location loc = ((Block) rootCause).getLocation();
            return new DelayedRegionOverlapAssociation(query, BukkitAdapter.adapt(loc),
                    WorldGuard.getInstance().getPlatform().getGlobalStateManager().get(BukkitAdapter.adapt(loc.getWorld())).useMaxPriorityAssociation);
        } else {
            return Associables.constant(Association.NON_MEMBER);
        }
    }
}

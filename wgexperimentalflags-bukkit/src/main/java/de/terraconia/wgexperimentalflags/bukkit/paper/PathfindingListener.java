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

package de.terraconia.wgexperimentalflags.bukkit.paper;

import com.destroystokyo.paper.event.entity.EntityPathfindEvent;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.world.entity.EntityType;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.association.RegionAssociable;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import de.terraconia.wgexperimentalflags.bukkit.ExperimentalFlags;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class PathfindingListener implements Listener {
    @EventHandler(ignoreCancelled = true)
    public void onPathfinding(EntityPathfindEvent event) {
        Location loc = event.getLoc();
        Entity entity = event.getEntity();
        EntityType type = BukkitAdapter.adapt(entity.getType());

        RegionQuery query = WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery();

        if (!StateFlag.test(query.queryMapValue(BukkitAdapter.adapt(loc), (RegionAssociable) null,
                ExperimentalFlags.PATHFINDING_MOB_TYPE, type, ExperimentalFlags.PATHFINDING_MOB))) {
            event.setCancelled(true);
        }
    }
}

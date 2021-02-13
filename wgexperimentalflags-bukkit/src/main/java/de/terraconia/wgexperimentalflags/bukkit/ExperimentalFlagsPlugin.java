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

package de.terraconia.wgexperimentalflags.bukkit;

import de.terraconia.wgexperimentalflags.bukkit.paper.PathfindingListener;
import io.papermc.lib.PaperLib;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class ExperimentalFlagsPlugin extends JavaPlugin {

    private static ExperimentalFlagsPlugin instance;
    public static ExperimentalFlagsPlugin getInstance() {
        return instance;
    }

    @Override
    public void onLoad() {
        instance = this;
        ExperimentalFlags.registerAll();
    }

    @Override
    public void onEnable() {
        if (PaperLib.isPaper()) {
            Bukkit.getPluginManager().registerEvents(new PathfindingListener(), this);
        }

    }
}

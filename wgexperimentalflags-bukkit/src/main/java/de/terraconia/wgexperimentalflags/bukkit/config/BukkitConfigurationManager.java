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

package de.terraconia.wgexperimentalflags.bukkit.config;

import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.extension.platform.Capability;
import com.sk89q.worldedit.util.report.Unreported;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import de.terraconia.wgexperimentalflags.bukkit.ExperimentalFlagsPlugin;

import java.io.File;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class BukkitConfigurationManager extends YamlConfigurationManager {

    @Unreported
    private ExperimentalFlagsPlugin plugin;
    @Unreported
    private ConcurrentMap<String, BukkitWorldConfiguration> worlds = new ConcurrentHashMap<>();


    /**
     * Construct the object.
     *
     * @param plugin The plugin instance
     */
    public BukkitConfigurationManager(ExperimentalFlagsPlugin plugin) {
        super();
        this.plugin = plugin;
    }

    public Collection<BukkitWorldConfiguration> getWorldConfigs() {
        return worlds.values();
    }

    @Override
    public void load() {
        super.load();
    }

    @Override
    public File getDataFolder() {
        return plugin.getDataFolder();
    }

    @Override
    public void copyDefaults() {
        // Create the default configuration file
        WorldGuardPlugin.inst().createDefaultConfiguration(new File(plugin.getDataFolder(), "config.yml"), "config.yml");
    }

    @Override
    public void unload() {
        worlds.clear();
    }

    @Override
    public void postLoad() {
        // Load configurations for each world
        for (World world : WorldEdit.getInstance().getPlatformManager().queryCapability(Capability.GAME_HOOKS).getWorlds()) {
            get(world);
        }
        getConfig().save();
    }

    /**
     * Get the configuration for a world.
     *
     * @param world The world to get the configuration for
     * @return {@code world}'s configuration
     */
    @Override
    public BukkitWorldConfiguration get(World world) {
        String worldName = world.getName();
        BukkitWorldConfiguration config = worlds.get(worldName);
        BukkitWorldConfiguration newConfig = null;

        while (config == null) {
            if (newConfig == null) {
                newConfig = new BukkitWorldConfiguration(plugin, worldName, this.getConfig());
            }
            worlds.putIfAbsent(world.getName(), newConfig);
            config = worlds.get(world.getName());
        }

        return config;
    }
}

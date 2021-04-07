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

import com.sk89q.util.yaml.YAMLFormat;
import com.sk89q.util.yaml.YAMLProcessor;
import com.sk89q.worldedit.util.report.Unreported;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.bukkit.internal.TargetMatcherSet;
import de.terraconia.wgexperimentalflags.bukkit.ExperimentalFlagsPlugin;
import org.bukkit.potion.PotionEffectType;
import org.yaml.snakeyaml.error.YAMLException;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.logging.Level;

/**
 * Holds the configuration for individual worlds.
 *
 * @author sk89q
 * @author Michael
 */
public class BukkitWorldConfiguration extends YamlWorldConfiguration {

    @Unreported
    private String worldName;

    /* Configuration data start */
    public boolean usePathfindingEvent;
    /* Configuration data end */

    /**
     * Construct the object.
     *
     * @param plugin The WorldGuardPlugin instance
     * @param worldName The world name that this BukkitWorldConfiguration is for.
     * @param parentConfig The parent configuration to read defaults from
     */
    public BukkitWorldConfiguration(ExperimentalFlagsPlugin plugin, String worldName, YAMLProcessor parentConfig) {
        File baseFolder = new File(plugin.getDataFolder(), "worlds/" + worldName);
        File configFile = new File(baseFolder, "config.yml");

        this.worldName = worldName;
        this.parentConfig = parentConfig;

        WorldGuardPlugin.inst().createDefaultConfiguration(configFile, "config_world.yml");

        config = new YAMLProcessor(configFile, true, YAMLFormat.EXTENDED);
        loadConfiguration();
    }

    /**
     * Load the configuration.
     */
    @Override
    public void loadConfiguration() {
        try {
            config.load();
        } catch (IOException e) {
            log.log(Level.SEVERE, "Error reading configuration for world " + worldName + ": ", e);
        } catch (YAMLException e) {
            log.severe("Error parsing configuration for world " + worldName + ". ");
            throw e;
        }

        config.setHeader(CONFIG_HEADER);
        usePathfindingEvent = getBoolean("event-handling.use-pathfinding", false);
        config.save();
    }

}

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

import java.io.File;
import java.io.IOException;

public abstract class YamlConfigurationManager extends ConfigurationManager {

    @Unreported
    private YAMLProcessor config;

    public abstract void copyDefaults();

    @Override
    public void load() {
        copyDefaults();

        config = new YAMLProcessor(new File(getDataFolder(), "config.yml"), true, YAMLFormat.EXTENDED);
        try {
            config.load();
        } catch (IOException e) {
            log.severe("Error reading configuration for global config: ");
            e.printStackTrace();
        }

        postLoad();

        config.setHeader(CONFIG_HEADER);
    }

    public void postLoad() {}

    public YAMLProcessor getConfig() {
        return config;
    }
}

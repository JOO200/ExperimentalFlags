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

import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.block.BlockTypes;
import com.sk89q.worldedit.world.item.ItemType;
import com.sk89q.worldedit.world.item.ItemTypes;
import com.sk89q.worldedit.world.registry.LegacyMapper;

import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Holds the configuration for individual worlds.
 *
 * @author sk89q
 * @author Michael
 */
public abstract class WorldConfiguration {

    public static final Logger log = Logger.getLogger(WorldConfiguration.class.getCanonicalName());

    public static final String CONFIG_HEADER = "#\r\n" +
            "# WorldGuardExperimentalFlags's world configuration file\r\n" +
            "#\r\n" +
            "# This is a world configuration file. Anything placed into here will only\r\n" +
            "# affect this world. If you don't put anything in this file, then the\r\n" +
            "# settings will be inherited from the main configuration file.\r\n" +
            "#\r\n" +
            "# If you see {} below, that means that there are NO entries in this file.\r\n" +
            "# Remove the {} and add your own entries.\r\n" +
            "#\r\n";

    public boolean usePathfindingEvent;

    /**
     * Load the configuration.
     */
    public abstract void loadConfiguration();

    public List<String> convertLegacyItems(List<String> legacyItems) {
        return legacyItems.stream().map(this::convertLegacyItem).filter(Objects::nonNull).collect(Collectors.toList());
    }

    public String convertLegacyItem(String legacy) {
        String[] splitter = legacy.split(":", 2);
        try {
            int id;
            byte data;
            if (splitter.length == 1) {
                id = Integer.parseInt(splitter[0]);
                data = 0;
            } else {
                id = Integer.parseInt(splitter[0]);
                data = Byte.parseByte(splitter[1]);
            }
            ItemType legacyItem = LegacyMapper.getInstance().getItemFromLegacy(id, data);
            if (legacyItem != null) {
                return legacyItem.getId();
            }
        } catch (NumberFormatException ignored) {
        }
        final ItemType itemType = ItemTypes.get(legacy);
        if (itemType != null) {
            return itemType.getId();
        }

        return null;
    }

    public List<String> convertLegacyBlocks(List<String> legacyBlocks) {
        return legacyBlocks.stream().map(this::convertLegacyBlock).filter(Objects::nonNull).collect(Collectors.toList());
    }

    public String convertLegacyBlock(String legacy) {
        String[] splitter = legacy.split(":", 2);
        try {
            int id;
            byte data;
            if (splitter.length == 1) {
                id = Integer.parseInt(splitter[0]);
                data = 0;
            } else {
                id = Integer.parseInt(splitter[0]);
                data = Byte.parseByte(splitter[1]);
            }
            BlockState legacyBlock = LegacyMapper.getInstance().getBlockFromLegacy(id, data);
            if (legacyBlock != null) {
                return legacyBlock.getBlockType().getId();
            }
        } catch (NumberFormatException ignored) {
        }
        final BlockType blockType = BlockTypes.get(legacy);
        if (blockType != null) {
            return blockType.getId();
        }

        return null;
    }
}

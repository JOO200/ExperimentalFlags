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

import com.sk89q.worldedit.world.entity.EntityType;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.MapFlag;
import com.sk89q.worldguard.protection.flags.RegistryFlag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.flags.registry.UnknownFlag;
import io.papermc.lib.PaperLib;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class ExperimentalFlags {
    private static final List<String> EXPERIMENTAL_FLAGS_LIST = new ArrayList<>();
    public static final List<String> EXPERIMENTAL_FLAGS = Collections.unmodifiableList(EXPERIMENTAL_FLAGS_LIST);


    // Specific flags for Paper
    // This flag does prevent the pathfinding from specific mob types or all mob types.
    public static final MapFlag<EntityType, StateFlag.State> PATHFINDING_MOB_TYPE =
            registerPaper(new MapFlag<>("pathfinding-mob-type",
                    new RegistryFlag<EntityType>(null, EntityType.REGISTRY),
                    new StateFlag(null, true)));
    public static final StateFlag PATHFINDING_MOB = registerPaper(new StateFlag("pathfinding", true));

    private ExperimentalFlags() {

    }

    private static <T extends Flag<?>> T registerPaper(final T flag) throws FlagConflictException {
        if (PaperLib.isPaper()) {
            return register(flag);
        } else {
            PaperLib.suggestPaper(ExperimentalFlagsPlugin.getInstance());
            register(new UnknownFlag(flag.getName()));
            return null;
        }
    }

    private static <T extends Flag<?>> T register(final T flag) throws FlagConflictException {
        WorldGuard.getInstance().getFlagRegistry().register(flag);
        EXPERIMENTAL_FLAGS_LIST.add(flag.getName());
        return flag;
    }

    private static <T extends Flag<?>> T register(final T flag, Consumer<T> cfg) throws FlagConflictException {
        T f = register(flag);
        cfg.accept(f);
        return f;
    }

    /**
     * Dummy method to call that initialises the class.
     */
    public static void registerAll() {
    }
}

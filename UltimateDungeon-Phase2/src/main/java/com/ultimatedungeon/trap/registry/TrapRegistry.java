package com.ultimatedungeon.trap.registry;

import com.ultimatedungeon.api.trap.ITrap;
import com.ultimatedungeon.api.trap.ITrapRegistry;
import com.ultimatedungeon.core.PluginLogger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.*;

/** Holds all registered trap type definitions. */
public final class TrapRegistry implements ITrapRegistry {

    private final PluginLogger logger;
    private final Map<String, ITrap> traps = new LinkedHashMap<>();

    public TrapRegistry(@NotNull final PluginLogger logger) {
        this.logger = logger;
    }

    @Override public void register(@NotNull final ITrap trap) {
        traps.put(trap.getTrapId(), trap);
        logger.debug("Registered trap: " + trap.getTrapId());
    }

    @Override @Nullable public ITrap getTrap(@NotNull final String trapId) { return traps.get(trapId); }
    @Override @NotNull public Collection<ITrap> getAllTraps() { return Collections.unmodifiableCollection(traps.values()); }
}

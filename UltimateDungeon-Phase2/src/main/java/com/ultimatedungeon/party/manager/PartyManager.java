package com.ultimatedungeon.party.manager;

import com.ultimatedungeon.core.PluginLogger;
import org.jetbrains.annotations.NotNull;

/** PartyManager — party subsystem manager. Implemented in Milestone 5. */
public final class PartyManager {

    private final PluginLogger logger;

    public PartyManager(@NotNull final PluginLogger logger) {
        this.logger = logger;
    }
}

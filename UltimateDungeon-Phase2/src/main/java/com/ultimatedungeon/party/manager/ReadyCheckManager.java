package com.ultimatedungeon.party.manager;

import com.ultimatedungeon.core.PluginLogger;
import org.jetbrains.annotations.NotNull;

/** ReadyCheckManager — party subsystem manager. Implemented in Milestone 5. */
public final class ReadyCheckManager {

    private final PluginLogger logger;

    public ReadyCheckManager(@NotNull final PluginLogger logger) {
        this.logger = logger;
    }
}

package com.ultimatedungeon.party.service;

import com.ultimatedungeon.core.PluginLogger;
import org.jetbrains.annotations.NotNull;

/** PartyService — stateless party business logic. Implemented in Milestone 5. */
public final class PartyService {

    private final PluginLogger logger;

    public PartyService(@NotNull final PluginLogger logger) {
        this.logger = logger;
    }
}

package com.ultimatedungeon.party.service;

import com.ultimatedungeon.core.PluginLogger;
import org.jetbrains.annotations.NotNull;

/** PartyValidationService — stateless party business logic. Implemented in Milestone 5. */
public final class PartyValidationService {

    private final PluginLogger logger;

    public PartyValidationService(@NotNull final PluginLogger logger) {
        this.logger = logger;
    }
}

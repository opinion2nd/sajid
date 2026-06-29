package com.ultimatedungeon.party.manager;

import com.ultimatedungeon.core.PluginLogger;
import org.jetbrains.annotations.NotNull;

/** InvitationManager — party subsystem manager. Implemented in Milestone 5. */
public final class InvitationManager {

    private final PluginLogger logger;

    public InvitationManager(@NotNull final PluginLogger logger) {
        this.logger = logger;
    }
}

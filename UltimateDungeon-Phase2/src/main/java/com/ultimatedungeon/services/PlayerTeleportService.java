package com.ultimatedungeon.services;

import com.ultimatedungeon.core.PluginLogger;
import org.jetbrains.annotations.NotNull;

/** PlayerTeleportService — stateless service. Implemented in the relevant milestone. */
public final class PlayerTeleportService {
    private final PluginLogger logger;
    public PlayerTeleportService(@NotNull final PluginLogger logger) { this.logger = logger; }
}

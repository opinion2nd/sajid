package com.ultimatedungeon.services;

import com.ultimatedungeon.core.PluginLogger;
import org.jetbrains.annotations.NotNull;

/** DifficultyService — stateless service. Implemented in the relevant milestone. */
public final class DifficultyService {
    private final PluginLogger logger;
    public DifficultyService(@NotNull final PluginLogger logger) { this.logger = logger; }
}

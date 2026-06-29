package com.ultimatedungeon.services;

import com.ultimatedungeon.core.PluginLogger;
import org.jetbrains.annotations.NotNull;

/** StatisticsService — stateless service. Implemented in the relevant milestone. */
public final class StatisticsService {
    private final PluginLogger logger;
    public StatisticsService(@NotNull final PluginLogger logger) { this.logger = logger; }
}

package com.ultimatedungeon.services;

import com.ultimatedungeon.core.PluginLogger;
import org.jetbrains.annotations.NotNull;

/** NotificationService — stateless service. Implemented in the relevant milestone. */
public final class NotificationService {
    private final PluginLogger logger;
    public NotificationService(@NotNull final PluginLogger logger) { this.logger = logger; }
}

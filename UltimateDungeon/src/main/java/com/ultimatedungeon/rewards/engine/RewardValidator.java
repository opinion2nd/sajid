package com.ultimatedungeon.rewards.engine;

import com.ultimatedungeon.core.PluginLogger;
import com.ultimatedungeon.rewards.model.RewardEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Prevents reward duplication by rejecting a repeat delivery of the same event
 * to the same player within a short window — guarding against double-fired
 * events or rapid re-triggers.
 */
public final class RewardValidator {

    private static final long WINDOW_MS = 1500L;

    private final PluginLogger logger;
    private final Map<String, Long> recent = new ConcurrentHashMap<>();

    public RewardValidator(@NotNull final PluginLogger logger) {
        this.logger = logger;
    }

    public boolean canDeliver(@NotNull final UUID playerId, @NotNull final RewardEvent event) {
        final String key = playerId + ":" + event.name();
        final long now = System.currentTimeMillis();
        final Long last = recent.get(key);
        if (last != null && now - last < WINDOW_MS) {
            logger.debug("Rejected duplicate reward " + event + " for " + playerId);
            return false;
        }
        recent.put(key, now);
        return true;
    }

    public void clearPlayer(@NotNull final UUID playerId) {
        recent.keySet().removeIf(k -> k.startsWith(playerId.toString()));
    }
}

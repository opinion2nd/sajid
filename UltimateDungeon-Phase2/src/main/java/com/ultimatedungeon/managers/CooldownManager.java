package com.ultimatedungeon.managers;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Per-player cooldown tracking for dungeon entry, retry attempts, and
 * other time-gated actions. All times stored as epoch milliseconds.
 */
public final class CooldownManager {

    private final Map<UUID, Map<String, Long>> cooldowns = new ConcurrentHashMap<>();

    public void setCooldown(
            @NotNull final Player player,
            @NotNull final String key,
            final long durationMs
    ) {
        cooldowns
                .computeIfAbsent(player.getUniqueId(), id -> new ConcurrentHashMap<>())
                .put(key, System.currentTimeMillis() + durationMs);
    }

    public boolean isOnCooldown(@NotNull final Player player, @NotNull final String key) {
        final Map<String, Long> playerCooldowns = cooldowns.get(player.getUniqueId());
        if (playerCooldowns == null) return false;
        final Long expiresAt = playerCooldowns.get(key);
        if (expiresAt == null) return false;
        if (System.currentTimeMillis() >= expiresAt) {
            playerCooldowns.remove(key);
            return false;
        }
        return true;
    }

    public long getRemainingMs(@NotNull final Player player, @NotNull final String key) {
        final Map<String, Long> playerCooldowns = cooldowns.get(player.getUniqueId());
        if (playerCooldowns == null) return 0L;
        final Long expiresAt = playerCooldowns.get(key);
        if (expiresAt == null) return 0L;
        return Math.max(0L, expiresAt - System.currentTimeMillis());
    }

    public void clearPlayer(@NotNull final Player player) {
        cooldowns.remove(player.getUniqueId());
    }

    public void clearAll() {
        cooldowns.clear();
    }
}

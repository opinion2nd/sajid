package com.ultimatedungeon.managers;

import com.ultimatedungeon.core.PluginLogger;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks the dungeon session state for every player currently in a dungeon.
 *
 * <p>Used to restore player state on reconnect and validate dungeon-scoped
 * operations (e.g. blocking teleports, processing deaths).</p>
 */
public final class PlayerSessionManager {

    private final PluginLogger logger;
    private final Map<UUID, PlayerDungeonSession> sessions = new ConcurrentHashMap<>();

    public PlayerSessionManager(@NotNull final PluginLogger logger) {
        this.logger = logger;
    }

    public void createSession(@NotNull final Player player, @NotNull final UUID instanceId) {
        sessions.put(player.getUniqueId(), new PlayerDungeonSession(player.getUniqueId(), instanceId));
        logger.debug("Created session for player: " + player.getName());
    }

    public void removeSession(@NotNull final Player player) {
        sessions.remove(player.getUniqueId());
        logger.debug("Removed session for player: " + player.getName());
    }

    @Nullable
    public PlayerDungeonSession getSession(@NotNull final Player player) {
        return sessions.get(player.getUniqueId());
    }

    public boolean hasSession(@NotNull final Player player) {
        return sessions.containsKey(player.getUniqueId());
    }

    public void clearAll() {
        sessions.clear();
    }

    // ── Inner session record ──────────────────────────────────────────────────

    public static final class PlayerDungeonSession {

        private final UUID playerUuid;
        private final UUID instanceId;
        private final long joinedAt;
        private volatile boolean reconnecting;

        public PlayerDungeonSession(
                @NotNull final UUID playerUuid,
                @NotNull final UUID instanceId
        ) {
            this.playerUuid = playerUuid;
            this.instanceId = instanceId;
            this.joinedAt = System.currentTimeMillis();
            this.reconnecting = false;
        }

        @NotNull public UUID getPlayerUuid() { return playerUuid; }
        @NotNull public UUID getInstanceId() { return instanceId; }
        public long getJoinedAt() { return joinedAt; }
        public boolean isReconnecting() { return reconnecting; }
        public void setReconnecting(final boolean reconnecting) { this.reconnecting = reconnecting; }
    }
}

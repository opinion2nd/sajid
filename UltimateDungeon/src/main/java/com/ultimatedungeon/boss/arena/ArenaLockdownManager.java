package com.ultimatedungeon.boss.arena;

import com.ultimatedungeon.core.PluginLogger;
import com.ultimatedungeon.room.model.RoomData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks which dungeon instances have a sealed boss arena. While locked, the
 * {@link com.ultimatedungeon.boss.arena.ArenaEscapeBlocker} keeps players inside
 * the boss room and portal/teleport listeners block escapes.
 */
public final class ArenaLockdownManager {

    private final PluginLogger logger;
    private final Map<UUID, RoomData> lockedArenas = new ConcurrentHashMap<>();

    public ArenaLockdownManager(@NotNull final PluginLogger logger) {
        this.logger = logger;
    }

    public void lock(@NotNull final UUID instanceId, @NotNull final RoomData bossRoom) {
        lockedArenas.put(instanceId, bossRoom);
        logger.debug("Arena locked for instance " + instanceId);
    }

    public void unlock(@NotNull final UUID instanceId) {
        lockedArenas.remove(instanceId);
        logger.debug("Arena unlocked for instance " + instanceId);
    }

    public boolean isLocked(@NotNull final UUID instanceId) {
        return lockedArenas.containsKey(instanceId);
    }

    @Nullable
    public RoomData getArena(@NotNull final UUID instanceId) {
        return lockedArenas.get(instanceId);
    }
}

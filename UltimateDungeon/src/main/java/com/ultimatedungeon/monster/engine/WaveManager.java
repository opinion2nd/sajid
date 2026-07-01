package com.ultimatedungeon.monster.engine;

import com.ultimatedungeon.core.PluginLogger;
import com.ultimatedungeon.room.model.RoomData;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Drives wave-based combat encounters for a room.
 *
 * <p>A session spawns one wave at a time; when {@link #poll} detects the current
 * wave is cleared it spawns the next, and runs the completion callback once all
 * waves are done. The owning tick task calls {@link #poll} periodically.</p>
 */
public final class WaveManager {

    /** Mutable state for one active wave encounter. */
    private static final class WaveSession {
        final RoomData room;
        final List<String> pool;
        final int totalWaves;
        final int perWave;
        final String difficultyId;
        final Runnable onComplete;
        int currentWave;

        WaveSession(final RoomData room, final List<String> pool, final int totalWaves,
                    final int perWave, final String difficultyId, final Runnable onComplete) {
            this.room = room;
            this.pool = pool;
            this.totalWaves = totalWaves;
            this.perWave = perWave;
            this.difficultyId = difficultyId;
            this.onComplete = onComplete;
        }
    }

    private final MonsterEngine engine;
    private final PluginLogger logger;
    private final Map<UUID, WaveSession> sessions = new ConcurrentHashMap<>();

    public WaveManager(@NotNull final MonsterEngine engine, @NotNull final PluginLogger logger) {
        this.engine = engine;
        this.logger = logger;
    }

    /** Starts a wave encounter for an instance and spawns the first wave. */
    public void start(@NotNull final UUID instanceId, @NotNull final RoomData room,
                      @NotNull final List<String> pool, final int waves, final int perWave,
                      @NotNull final String difficultyId, @NotNull final Runnable onComplete) {
        if (pool.isEmpty() || waves <= 0) {
            onComplete.run();
            return;
        }
        final WaveSession session = new WaveSession(room, pool, waves, perWave, difficultyId, onComplete);
        sessions.put(instanceId, session);
        spawnWave(instanceId, session);
    }

    /** Checks whether the current wave is cleared and advances or completes. */
    public void poll(@NotNull final UUID instanceId) {
        final WaveSession session = sessions.get(instanceId);
        if (session == null) return;
        if (engine.aliveCount(instanceId) > 0) return;

        if (session.currentWave >= session.totalWaves) {
            sessions.remove(instanceId);
            session.room.setCleared();
            session.onComplete.run();
        } else {
            spawnWave(instanceId, session);
        }
    }

    public boolean hasActiveEncounter(@NotNull final UUID instanceId) {
        return sessions.containsKey(instanceId);
    }

    public void cancel(@NotNull final UUID instanceId) {
        sessions.remove(instanceId);
    }

    private void spawnWave(@NotNull final UUID instanceId, @NotNull final WaveSession session) {
        session.currentWave++;
        final java.util.List<String> ids = new java.util.ArrayList<>();
        for (int i = 0; i < session.perWave; i++) {
            ids.add(session.pool.get(i % session.pool.size()));
        }
        engine.spawnGroup(instanceId, ids, session.room.getCentre(), session.difficultyId);
        logger.debug("Spawned wave " + session.currentWave + "/" + session.totalWaves
                + " for instance " + instanceId);
    }
}

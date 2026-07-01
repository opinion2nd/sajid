package com.ultimatedungeon.monster.engine;

import com.ultimatedungeon.core.PluginLogger;
import com.ultimatedungeon.room.model.RoomData;
import org.bukkit.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Drives wave-based combat encounters for a room, strictly one wave at a time.
 *
 * <p>Each session tracks the exact monsters it spawned for the current wave. The
 * next wave is only spawned once <em>those</em> monsters are all dead — never
 * based on an instance-wide count — and only after a short inter-wave delay. This
 * prevents every wave from pouring out at once.</p>
 */
public final class WaveManager {

    /** Delay between a wave being cleared and the next wave spawning (ms). */
    private static final long INTER_WAVE_DELAY_MS = 2_000L;

    /** Mutable state for one active wave encounter. */
    private static final class WaveSession {
        final RoomData room;
        final List<String> pool;
        final int totalWaves;
        final int perWave;
        final String difficultyId;
        final Runnable onComplete;
        int currentWave;
        /** The living entities spawned for the current wave. */
        final List<LivingEntity> currentMonsters = new ArrayList<>();
        /** When set, the earliest time (ms) the next wave may spawn. */
        long nextWaveAtMs;
        boolean awaitingNext;

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

    /** Advances the encounter: spawns the next wave only once the current one is dead. */
    public void poll(@NotNull final UUID instanceId) {
        final WaveSession session = sessions.get(instanceId);
        if (session == null) return;

        // Only this wave's monsters gate progression — not the whole instance.
        session.currentMonsters.removeIf(e -> e == null || e.isDead() || !e.isValid());
        if (!session.currentMonsters.isEmpty()) return; // current wave still fighting

        if (session.currentWave >= session.totalWaves) {
            sessions.remove(instanceId);
            session.room.setCleared();
            session.onComplete.run();
            return;
        }

        // Brief gap between waves so they never appear all at once.
        final long now = System.currentTimeMillis();
        if (!session.awaitingNext) {
            session.awaitingNext = true;
            session.nextWaveAtMs = now + INTER_WAVE_DELAY_MS;
            return;
        }
        if (now < session.nextWaveAtMs) return;
        session.awaitingNext = false;
        spawnWave(instanceId, session);
    }

    public boolean hasActiveEncounter(@NotNull final UUID instanceId) {
        return sessions.containsKey(instanceId);
    }

    public void cancel(@NotNull final UUID instanceId) {
        sessions.remove(instanceId);
    }

    private void spawnWave(@NotNull final UUID instanceId, @NotNull final WaveSession session) {
        session.currentWave++;
        final List<String> ids = new ArrayList<>();
        // Offset the pool by the wave number so each wave fields a different mix
        // of monsters rather than repeating the same set every time.
        for (int i = 0; i < session.perWave; i++) {
            ids.add(session.pool.get((i + session.currentWave) % session.pool.size()));
        }
        session.currentMonsters.clear();
        session.currentMonsters.addAll(
                engine.spawnGroup(instanceId, ids, session.room.getCentre(), session.difficultyId));
        logger.debug("Spawned wave " + session.currentWave + "/" + session.totalWaves
                + " (" + session.currentMonsters.size() + " monsters) for instance " + instanceId);
    }
}

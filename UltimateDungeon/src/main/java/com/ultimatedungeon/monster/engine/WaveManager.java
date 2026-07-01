package com.ultimatedungeon.monster.engine;

import com.ultimatedungeon.config.files.WavesConfig;
import com.ultimatedungeon.core.PluginLogger;
import com.ultimatedungeon.room.model.RoomData;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Drives wave-based combat, one wave at a time, using {@code waves.yml}.
 *
 * <p>Each combat room runs {@code waves-per-room} waves of mixed vanilla mobs
 * drawn from the dungeon level's roster, scaled by the level. The next wave only
 * spawns once the current wave's monsters are all dead, after a short gap.</p>
 */
public final class WaveManager {

    private static final long INTER_WAVE_DELAY_MS = 2_000L;

    private static final class WaveSession {
        final RoomData room;
        final int level;
        final int totalWaves;
        final Runnable onComplete;
        int currentWave;
        final List<LivingEntity> currentMonsters = new ArrayList<>();
        long nextWaveAtMs;
        boolean awaitingNext;

        WaveSession(final RoomData room, final int level, final int totalWaves, final Runnable onComplete) {
            this.room = room;
            this.level = level;
            this.totalWaves = totalWaves;
            this.onComplete = onComplete;
        }
    }

    private final MonsterEngine engine;
    private final WavesConfig waves;
    private final PluginLogger logger;
    private final Map<UUID, WaveSession> sessions = new ConcurrentHashMap<>();

    public WaveManager(@NotNull final MonsterEngine engine, @NotNull final WavesConfig waves,
                       @NotNull final PluginLogger logger) {
        this.engine = engine;
        this.waves = waves;
        this.logger = logger;
    }

    /** Starts the wave encounter for a room at the given dungeon level. */
    public void start(@NotNull final UUID instanceId, @NotNull final RoomData room,
                      final int level, @NotNull final Runnable onComplete) {
        final WaveSession session = new WaveSession(room, level, waves.getWavesPerRoom(), onComplete);
        sessions.put(instanceId, session);
        spawnWave(instanceId, session);
    }

    /** Advances the encounter: next wave only once the current one is dead. */
    public void poll(@NotNull final UUID instanceId) {
        final WaveSession session = sessions.get(instanceId);
        if (session == null) return;

        session.currentMonsters.removeIf(e -> e == null || e.isDead() || !e.isValid());
        if (!session.currentMonsters.isEmpty()) return;

        if (session.currentWave >= session.totalWaves) {
            sessions.remove(instanceId);
            session.room.setCleared();
            session.onComplete.run();
            return;
        }
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
        final WavesConfig.LevelWaves roster = waves.forLevel(session.level);
        final int count = waves.getBasePerWave() + waves.getPerWaveGrowth() * (session.currentWave - 1);
        final Location centre = session.room.getCentre();

        session.currentMonsters.clear();
        for (int i = 0; i < count; i++) {
            final EntityType type = roster.mobs().get(ThreadLocalRandom.current().nextInt(roster.mobs().size()));
            final Location loc = centre.clone().add(
                    ThreadLocalRandom.current().nextInt(-3, 4), 0,
                    ThreadLocalRandom.current().nextInt(-3, 4));
            final LivingEntity e = engine.spawnWaveMob(instanceId, type, roster.healthMultiplier(), loc);
            if (e != null) session.currentMonsters.add(e);
        }
        logger.debug("Spawned wave " + session.currentWave + "/" + session.totalWaves
                + " (" + session.currentMonsters.size() + " mobs, level " + session.level
                + ") for instance " + instanceId);
    }
}

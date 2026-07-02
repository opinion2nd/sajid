package com.ultimatedungeon.monster.engine;

import com.ultimatedungeon.config.files.WavesConfig;
import com.ultimatedungeon.core.PluginLogger;
import com.ultimatedungeon.room.model.RoomData;
import com.ultimatedungeon.util.RandomUtil;
import com.ultimatedungeon.util.WeightedRandomSelector;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Drives wave-based combat encounters for a room, configured entirely by
 * {@code waves.yml} (per level: wave count, mobs per wave, mob weights).
 *
 * <p>A session spawns one wave at a time; when {@link #poll} detects the
 * current wave is cleared it spawns the next, and runs the completion callback
 * once all waves are done — only then does the room count as cleared/unlocked.
 * Mobs spawn on validated floor positions inside the room, never inside walls.
 * Exposes live wave/mob counters for the dungeon scoreboard.</p>
 */
public final class WaveManager {

    /** Mutable state for one active wave encounter. */
    private static final class WaveSession {
        final RoomData room;
        final WavesConfig.LevelWaves rules;
        final int totalWaves;
        final Runnable onComplete;
        final List<UUID> aliveMobs = new ArrayList<>();
        int currentWave;
        int completedWaves;

        WaveSession(final RoomData room, final WavesConfig.LevelWaves rules,
                    final int totalWaves, final Runnable onComplete) {
            this.room = room;
            this.rules = rules;
            this.totalWaves = totalWaves;
            this.onComplete = onComplete;
        }
    }

    private final WavesConfig wavesConfig;
    private final PluginLogger logger;
    private final Map<UUID, WaveSession> sessions = new ConcurrentHashMap<>();
    /** Every wave mob spawned per instance, for full despawn on cleanup. */
    private final Map<UUID, List<UUID>> spawnedByInstance = new ConcurrentHashMap<>();

    public WaveManager(@NotNull final WavesConfig wavesConfig, @NotNull final PluginLogger logger) {
        this.wavesConfig = wavesConfig;
        this.logger = logger;
    }

    /** Rolls whether a room of this level should host a wave encounter at all. */
    public boolean shouldRoomHaveWaves(@NotNull final String levelId) {
        return RandomUtil.rollPercent(wavesConfig.forLevel(levelId).roomsWithWavesPercent());
    }

    /** True when boss rooms of this level are allowed to host normal waves. */
    public boolean bossRoomWavesEnabled(@NotNull final String levelId) {
        return wavesConfig.forLevel(levelId).bossRoomWaves();
    }

    /** Pre-fight countdown (seconds) before a wave room seals and starts. */
    public int waveCountdownSeconds(@NotNull final String levelId) {
        return wavesConfig.getCountdownSeconds();
    }

    /** Starts a wave encounter for an instance using the level's rules. */
    public void startForLevel(@NotNull final UUID instanceId, @NotNull final RoomData room,
                              @NotNull final String levelId, @NotNull final Runnable onComplete) {
        final WavesConfig.LevelWaves rules = wavesConfig.forLevel(levelId);
        final int waves = RandomUtil.safeRange(
                rules.minWavesPerRoom(), rules.maxWavesPerRoom(), 1, 20);
        final WaveSession session = new WaveSession(room, rules, waves, onComplete);
        sessions.put(instanceId, session);
        spawnWave(instanceId, session);
    }

    /** Checks whether the current wave is cleared and advances or completes. */
    public void poll(@NotNull final UUID instanceId) {
        final WaveSession session = sessions.get(instanceId);
        if (session == null) return;

        pruneDead(session);
        if (!session.aliveMobs.isEmpty()) return;

        if (session.currentWave > 0) session.completedWaves = session.currentWave;
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

    /** Removes every wave mob this instance ever spawned (dungeon cleanup). */
    public void despawnAll(@NotNull final UUID instanceId) {
        sessions.remove(instanceId);
        final List<UUID> ids = spawnedByInstance.remove(instanceId);
        if (ids == null) return;
        for (final UUID id : ids) {
            final Entity e = org.bukkit.Bukkit.getEntity(id);
            if (e != null && !e.isDead()) e.remove();
        }
    }

    // ── Scoreboard counters ───────────────────────────────────────────────────

    public int getCurrentWave(@NotNull final UUID instanceId) {
        final WaveSession s = sessions.get(instanceId);
        return s != null ? s.currentWave : 0;
    }

    public int getTotalWaves(@NotNull final UUID instanceId) {
        final WaveSession s = sessions.get(instanceId);
        return s != null ? s.totalWaves : 0;
    }

    public int getCompletedWaves(@NotNull final UUID instanceId) {
        final WaveSession s = sessions.get(instanceId);
        return s != null ? s.completedWaves : 0;
    }

    public int getMobsLeft(@NotNull final UUID instanceId) {
        final WaveSession s = sessions.get(instanceId);
        if (s == null) return 0;
        pruneDead(s);
        return s.aliveMobs.size();
    }

    // ── Internal ──────────────────────────────────────────────────────────────

    private void pruneDead(@NotNull final WaveSession session) {
        final Iterator<UUID> it = session.aliveMobs.iterator();
        while (it.hasNext()) {
            final Entity e = org.bukkit.Bukkit.getEntity(it.next());
            if (e == null || e.isDead()) it.remove();
        }
    }

    private void spawnWave(@NotNull final UUID instanceId, @NotNull final WaveSession session) {
        session.currentWave++;
        final int count = RandomUtil.safeRange(
                session.rules.mobsPerWaveMin(), session.rules.mobsPerWaveMax(), 1, 64);

        final WeightedRandomSelector<EntityType> selector = new WeightedRandomSelector<>();
        session.rules.mobWeights().forEach(selector::add);

        int spawned = 0;
        for (int i = 0; i < count; i++) {
            final Location at = findFloorSpawn(session.room);
            if (at == null) continue;
            final Entity e = at.getWorld().spawnEntity(at, selector.select());
            if (e instanceof final LivingEntity mob) {
                mob.setRemoveWhenFarAway(false);
                session.aliveMobs.add(mob.getUniqueId());
                spawnedByInstance.computeIfAbsent(instanceId, k -> new ArrayList<>())
                        .add(mob.getUniqueId());
                spawned++;
            } else {
                e.remove();
            }
        }
        logger.debug("Spawned wave " + session.currentWave + "/" + session.totalWaves
                + " (" + spawned + " mobs) for instance " + instanceId);
    }

    /**
     * Finds a valid floor position inside the room: solid block below, two air
     * blocks above, at least one block away from every wall — so mobs never
     * spawn inside walls or get stuck.
     */
    private Location findFloorSpawn(@NotNull final RoomData room) {
        final Location origin = room.getOrigin();
        final World world = origin.getWorld();
        if (world == null) return null;
        for (int attempt = 0; attempt < 20; attempt++) {
            final int x = origin.getBlockX() + RandomUtil.randomInt(2, Math.max(2, room.getWidth() - 3));
            final int z = origin.getBlockZ() + RandomUtil.randomInt(2, Math.max(2, room.getDepth() - 3));
            final int floorY = origin.getBlockY(); // room floor layer
            final Location at = new Location(world, x + 0.5, floorY + 1.0, z + 0.5);
            if (world.getBlockAt(x, floorY, z).getType().isSolid()
                    && world.getBlockAt(x, floorY + 1, z).getType().isAir()
                    && world.getBlockAt(x, floorY + 2, z).getType().isAir()) {
                return at;
            }
        }
        // Fallback: the room centre is always inside the carved interior.
        return room.getCentre();
    }
}

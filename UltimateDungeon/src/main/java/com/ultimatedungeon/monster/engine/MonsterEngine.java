package com.ultimatedungeon.monster.engine;

import com.ultimatedungeon.config.files.DifficultyConfig.DifficultyPreset;
import com.ultimatedungeon.config.files.MonstersConfig;
import com.ultimatedungeon.core.PluginLogger;
import com.ultimatedungeon.monster.ai.MonsterAI;
import com.ultimatedungeon.monster.model.MonsterDefinition;
import com.ultimatedungeon.services.DifficultyService;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Spawns, tracks and ticks dungeon monsters per instance.
 *
 * <p>Monster definitions are parsed once from {@code monsters.yml}. Spawned
 * entities are tracked per instance so they can be AI-ticked together and fully
 * removed on cleanup, preventing orphaned mobs.</p>
 */
public final class MonsterEngine {

    private final MonsterSpawner spawner;
    private final MonsterScaler scaler;
    private final DifficultyService difficulty;
    private final MonsterAI ai;
    private final PluginLogger logger;

    private final Map<String, MonsterDefinition> definitions = new LinkedHashMap<>();
    private final Map<UUID, List<LivingEntity>> active = new ConcurrentHashMap<>();
    /** Per-spawned-entity ability list, keyed by entity UUID. */
    private final Map<UUID, List<com.ultimatedungeon.monster.abilities.ConfiguredMonsterAbility>> entityAbilities
            = new ConcurrentHashMap<>();

    public MonsterEngine(@NotNull final MonstersConfig config,
                         @NotNull final MonsterSpawner spawner,
                         @NotNull final MonsterScaler scaler,
                         @NotNull final DifficultyService difficulty,
                         @NotNull final PluginLogger logger) {
        this.spawner = spawner;
        this.scaler = scaler;
        this.difficulty = difficulty;
        this.ai = new MonsterAI();
        this.logger = logger;
        loadDefinitions(config);
    }

    private void loadDefinitions(@NotNull final MonstersConfig config) {
        final ConfigurationSection section = config.raw().getConfigurationSection("monsters");
        if (section == null) {
            logger.warning("monsters.yml has no 'monsters' section.");
            return;
        }
        for (final String id : section.getKeys(false)) {
            final ConfigurationSection s = section.getConfigurationSection(id);
            if (s != null) definitions.put(id, MonsterDefinition.fromSection(id, s));
        }
        logger.info("Loaded " + definitions.size() + " monster definition(s).");
    }

    @Nullable
    public MonsterDefinition getDefinition(@NotNull final String id) {
        return definitions.get(id);
    }

    /** Spawns one monster for an instance and tracks it. */
    @Nullable
    public LivingEntity spawnOne(@NotNull final UUID instanceId, @NotNull final String monsterId,
                                 @NotNull final Location location, @NotNull final String difficultyId) {
        final MonsterDefinition def = definitions.get(monsterId);
        if (def == null) {
            logger.debug("Unknown monster id requested: " + monsterId);
            return null;
        }
        final DifficultyPreset preset = difficulty.resolve(difficultyId);
        final LivingEntity entity = spawner.spawn(def, scaler.scale(def, preset), location);
        if (entity != null) {
            active.computeIfAbsent(instanceId, k -> new CopyOnWriteArrayList<>()).add(entity);
            if (!def.getAbilities().isEmpty()) {
                final List<com.ultimatedungeon.monster.abilities.ConfiguredMonsterAbility> abs = new ArrayList<>();
                for (final MonsterDefinition.AbilitySpec spec : def.getAbilities()) {
                    abs.add(new com.ultimatedungeon.monster.abilities.ConfiguredMonsterAbility(spec));
                }
                entityAbilities.put(entity.getUniqueId(), abs);
            }
        }
        return entity;
    }

    /**
     * Spawns one plain (config-driven) wave monster of a vanilla type, scaled by
     * the level's health multiplier, and tracks it for wave/alive counting.
     */
    @Nullable
    public LivingEntity spawnWaveMob(@NotNull final UUID instanceId,
                                     @NotNull final org.bukkit.entity.EntityType type,
                                     final double healthMultiplier, @NotNull final Location location) {
        if (location.getWorld() == null) return null;
        final Class<? extends org.bukkit.entity.Entity> cls = type.getEntityClass();
        if (cls == null || !LivingEntity.class.isAssignableFrom(cls)) return null;
        final org.bukkit.entity.Entity raw = location.getWorld().spawnEntity(location, type);
        if (!(raw instanceof final LivingEntity living)) {
            raw.remove();
            return null;
        }
        final double base = living.getMaxHealth();
        final double scaled = Math.max(1.0, base * Math.max(0.1, healthMultiplier));
        living.setMaxHealth(scaled);
        living.setHealth(scaled);
        living.setRemoveWhenFarAway(false);
        active.computeIfAbsent(instanceId, k -> new CopyOnWriteArrayList<>()).add(living);
        return living;
    }

    /** Spawns a group of monsters scattered around a centre point. */
    @NotNull
    public List<LivingEntity> spawnGroup(@NotNull final UUID instanceId, @NotNull final List<String> monsterIds,
                                         @NotNull final Location centre, @NotNull final String difficultyId) {
        final List<LivingEntity> spawned = new ArrayList<>();
        for (final String id : monsterIds) {
            final Location loc = centre.clone().add(
                    ThreadLocalRandom.current().nextInt(-4, 5), 0,
                    ThreadLocalRandom.current().nextInt(-4, 5));
            final LivingEntity e = spawnOne(instanceId, id, loc, difficultyId);
            if (e != null) spawned.add(e);
        }
        return spawned;
    }

    /** Runs an AI tick for every monster in the instance, fires abilities, prunes dead ones. */
    public void tick(@NotNull final UUID instanceId) {
        final List<LivingEntity> list = active.get(instanceId);
        if (list == null || list.isEmpty()) return;

        final List<LivingEntity> dead = new ArrayList<>();
        for (final LivingEntity e : list) {
            if (e == null || e.isDead() || !e.isValid()) dead.add(e);
        }
        if (!dead.isEmpty()) {
            list.removeAll(dead);
            for (final LivingEntity e : dead) if (e != null) entityAbilities.remove(e.getUniqueId());
        }

        ai.tick(list);

        // Fire one ready ability per monster while a player is engaged nearby.
        for (final LivingEntity e : list) {
            final List<com.ultimatedungeon.monster.abilities.ConfiguredMonsterAbility> abs =
                    entityAbilities.get(e.getUniqueId());
            if (abs == null || abs.isEmpty() || !playerNear(e, 20.0)) continue;
            for (final com.ultimatedungeon.monster.abilities.ConfiguredMonsterAbility a : abs) {
                if (a.isReady()) { a.activate(e); break; }
            }
        }
    }

    private boolean playerNear(@NotNull final LivingEntity monster, final double range) {
        if (monster.getWorld() == null) return false;
        final double rSq = range * range;
        for (final org.bukkit.entity.Player p : monster.getWorld().getPlayers()) {
            if (p.getGameMode() == org.bukkit.GameMode.SPECTATOR) continue;
            if (p.getLocation().distanceSquared(monster.getLocation()) <= rSq) return true;
        }
        return false;
    }

    public int aliveCount(@NotNull final UUID instanceId) {
        final List<LivingEntity> list = active.get(instanceId);
        if (list == null) return 0;
        return (int) list.stream().filter(e -> e != null && !e.isDead() && e.isValid()).count();
    }

    @NotNull
    public Collection<LivingEntity> getActive(@NotNull final UUID instanceId) {
        return active.getOrDefault(instanceId, List.of());
    }

    /** Removes every tracked monster for an instance. */
    public void despawnAll(@NotNull final UUID instanceId) {
        final List<LivingEntity> list = active.remove(instanceId);
        if (list == null) return;
        for (final LivingEntity e : list) {
            if (e != null && !e.isDead()) e.remove();
            if (e != null) entityAbilities.remove(e.getUniqueId());
        }
    }
}

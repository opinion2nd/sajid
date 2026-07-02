package com.ultimatedungeon.boss.engine;

import com.ultimatedungeon.UltimateDungeon;
import com.ultimatedungeon.api.boss.IBossAbility;
import com.ultimatedungeon.boss.abilities.*;
import com.ultimatedungeon.boss.model.BossDefinition;
import com.ultimatedungeon.boss.model.BossPhaseData;
import com.ultimatedungeon.config.files.BossesConfig;
import com.ultimatedungeon.core.PluginLogger;
import com.ultimatedungeon.services.DifficultyService;
import com.ultimatedungeon.util.MiniMessageUtil;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

/**
 * Spawns and drives boss encounters: builds the boss entities from their
 * definitions, scales health by level, wires up BossBars, phase state machines
 * and ability rotations.
 *
 * <p>An instance may host several bosses across separate boss rooms — the
 * selected level decides how many (level 1 → 1 boss ... level 5 → 5 bosses).
 * The death hook fires per boss; run completion is decided by the caller
 * counting kills against the level's boss total.</p>
 */
public final class BossEngine {

    private static final class ActiveBoss {
        final UUID instanceId;
        final BossDefinition def;
        final LivingEntity entity;
        final BossHealthTracker health;
        final BossBarManager bossBar;
        final BossStateMachine stateMachine;
        final BossAI ai;
        boolean dead;

        ActiveBoss(final UUID instanceId, final BossDefinition def, final LivingEntity entity,
                   final BossHealthTracker health, final BossBarManager bossBar,
                   final BossStateMachine stateMachine, final BossAI ai) {
            this.instanceId = instanceId;
            this.def = def;
            this.entity = entity;
            this.health = health;
            this.bossBar = bossBar;
            this.stateMachine = stateMachine;
            this.ai = ai;
        }
    }

    private final DifficultyService difficulty;
    private final PluginLogger logger;
    private final NamespacedKey bossKey;

    private final Map<String, BossDefinition> definitions = new LinkedHashMap<>();
    private final Map<UUID, List<ActiveBoss>> active = new ConcurrentHashMap<>();

    /** Fired every time a boss dies: (instanceId, bossId). */
    private BiConsumer<UUID, String> onBossDeath = (i, b) -> {};

    public BossEngine(@NotNull final UltimateDungeon plugin,
                      @NotNull final BossesConfig config,
                      @NotNull final DifficultyService difficulty,
                      @NotNull final PluginLogger logger) {
        this.difficulty = difficulty;
        this.logger = logger;
        this.bossKey = new NamespacedKey(plugin, "ud_boss_id");
        loadDefinitions(config);
    }

    public void setDeathHook(@NotNull final BiConsumer<UUID, String> hook) {
        this.onBossDeath = hook;
    }

    private void loadDefinitions(@NotNull final BossesConfig config) {
        final ConfigurationSection section = config.raw().getConfigurationSection("bosses");
        if (section == null) {
            logger.warning("bosses.yml has no 'bosses' section.");
            return;
        }
        for (final String id : section.getKeys(false)) {
            final ConfigurationSection s = section.getConfigurationSection(id);
            if (s != null) definitions.put(id, BossDefinition.fromSection(id, s));
        }
        logger.info("Loaded " + definitions.size() + " boss definition(s).");
    }

    @Nullable
    public BossDefinition getDefinition(@NotNull final String id) {
        return definitions.get(id);
    }

    /**
     * Spawns one boss for a boss room, picking a random type from bosses.yml.
     * The theme's pool is preferred, and a boss already fighting in this
     * instance is never picked twice while others remain — so a level-5 run
     * meets five DIFFERENT bosses in five different rooms.
     */
    @Nullable
    public LivingEntity spawnRandomBoss(@NotNull final UUID instanceId,
                                        @NotNull final List<String> themePool,
                                        @NotNull final Location centre,
                                        @NotNull final String difficultyId,
                                        @NotNull final Collection<? extends Player> arenaPlayers) {
        final java.util.Set<String> used = new java.util.HashSet<>();
        final List<ActiveBoss> group = active.get(instanceId);
        if (group != null) for (final ActiveBoss b : group) used.add(b.def.getId());

        final List<String> candidates = new ArrayList<>();
        for (final String id : themePool) {
            if (definitions.containsKey(id) && !used.contains(id)) candidates.add(id);
        }
        if (candidates.isEmpty()) {
            for (final String id : definitions.keySet()) {
                if (!used.contains(id)) candidates.add(id);
            }
        }
        if (candidates.isEmpty()) candidates.addAll(definitions.keySet());
        if (candidates.isEmpty()) return null;

        java.util.Collections.shuffle(candidates);
        return spawnBoss(instanceId, candidates.get(0), centre, difficultyId, arenaPlayers);
    }

    /** Spawns one boss for an instance and shows its BossBar to the arena players. */
    @Nullable
    public LivingEntity spawnBoss(@NotNull final UUID instanceId, @NotNull final String bossId,
                                  @NotNull final Location location, @NotNull final String difficultyId,
                                  @NotNull final Collection<? extends Player> arenaPlayers) {
        final BossDefinition def = definitions.get(bossId);
        if (def == null || location.getWorld() == null) {
            logger.warning("Cannot spawn unknown boss: " + bossId);
            return null;
        }
        final Entity raw = location.getWorld().spawnEntity(location, def.getEntityType());
        if (!(raw instanceof final LivingEntity boss)) {
            raw.remove();
            return null;
        }
        final double maxHealth = def.getMaxHealth() * difficulty.healthMultiplier(difficultyId);
        applyHealth(boss, maxHealth);
        boss.setCustomName(MiniMessageUtil.legacy(def.getDisplayName()));
        boss.setCustomNameVisible(true);
        boss.setRemoveWhenFarAway(false);
        boss.setGlowing(true);
        boss.getPersistentDataContainer().set(bossKey, PersistentDataType.STRING, bossId);

        final List<IBossAbility> abilities = buildAbilities(def);
        final BossBarManager bar = new BossBarManager(
                MiniMessageUtil.legacy(def.getDisplayName()), def.getBarColor(), def.getBarStyle());
        bar.show(arenaPlayers);

        final ActiveBoss activeBoss = new ActiveBoss(instanceId, def, boss,
                new BossHealthTracker(boss, maxHealth), bar,
                new BossStateMachine(def.getPhases()), new BossAI(abilities));
        active.computeIfAbsent(instanceId, k -> new ArrayList<>()).add(activeBoss);

        announce(arenaPlayers, def, "spawn");
        logger.info("Boss spawned: " + bossId + " for instance " + instanceId);
        return boss;
    }

    /** Per-tick update: BossBars, phase transitions, ability rotations and deaths. */
    public void tick(@NotNull final UUID instanceId) {
        final List<ActiveBoss> group = active.get(instanceId);
        if (group == null || group.isEmpty()) return;

        for (final ActiveBoss boss : new ArrayList<>(group)) {
            if (boss.dead) continue;

            if (boss.health.isDead()) {
                handleDeath(boss, group);
                continue;
            }
            final double ratio = boss.health.getHealthRatio();
            boss.bossBar.setProgress(ratio);

            final BossPhaseData newPhase = boss.stateMachine.update(ratio);
            if (newPhase != null && boss.stateMachine.getCurrentPhaseIndex() > 0) {
                // Announce the line matching the phase we just entered.
                final String key = boss.stateMachine.getCurrentPhaseIndex() == 1
                        ? "phase-two" : "phase-three";
                announce(arenaPlayers(boss), boss.def, key);
            }
            boss.ai.tick(boss.entity);
        }
    }

    public boolean hasActiveBoss(@NotNull final UUID instanceId) {
        final List<ActiveBoss> group = active.get(instanceId);
        if (group == null) return false;
        for (final ActiveBoss boss : group) {
            if (!boss.dead) return true;
        }
        return false;
    }

    public void cleanup(@NotNull final UUID instanceId) {
        final List<ActiveBoss> group = active.remove(instanceId);
        if (group == null) return;
        for (final ActiveBoss boss : group) {
            boss.bossBar.remove();
            if (!boss.entity.isDead()) boss.entity.remove();
        }
    }

    // ── Internal ────────────────────────────────────────────────────────────

    private void handleDeath(@NotNull final ActiveBoss boss, @NotNull final List<ActiveBoss> group) {
        boss.dead = true;
        boss.bossBar.remove();
        announce(arenaPlayers(boss), boss.def, "death");
        spawnVictoryFireworks(boss.entity.getLocation());
        group.remove(boss);
        if (group.isEmpty()) active.remove(boss.instanceId);
        logger.info("Boss defeated: " + boss.def.getId() + " in instance " + boss.instanceId);

        // Fires for EVERY boss death; the completion hook decides whether the
        // run is over by counting kills against the level's boss total.
        onBossDeath.accept(boss.instanceId, boss.def.getId());
    }

    private void spawnVictoryFireworks(@NotNull final org.bukkit.Location loc) {
        if (loc.getWorld() == null) return;
        for (int i = 0; i < 4; i++) {
            final org.bukkit.entity.Entity e = loc.getWorld().spawnEntity(
                    loc.clone().add(Math.random() * 4 - 2, 1, Math.random() * 4 - 2),
                    EntityType.FIREWORK_ROCKET);
            if (!(e instanceof final org.bukkit.entity.Firework fw)) continue;
            final org.bukkit.inventory.meta.FireworkMeta meta = fw.getFireworkMeta();
            meta.addEffect(org.bukkit.FireworkEffect.builder()
                    .withColor(org.bukkit.Color.YELLOW, org.bukkit.Color.RED)
                    .withFade(org.bukkit.Color.WHITE)
                    .with(org.bukkit.FireworkEffect.Type.BALL_LARGE)
                    .flicker(true).trail(true).build());
            meta.setPower(1);
            fw.setFireworkMeta(meta);
        }
    }

    @NotNull
    private List<IBossAbility> buildAbilities(@NotNull final BossDefinition def) {
        final List<IBossAbility> abilities = new ArrayList<>();
        int index = 0;
        for (final BossDefinition.AbilitySpec spec : def.getAbilities()) {
            abilities.add(create(index++, spec));
        }
        return abilities;
    }

    @NotNull
    private IBossAbility create(final int index, @NotNull final BossDefinition.AbilitySpec spec) {
        // Every boss power has its own unique behaviour keyed by ability id.
        final IBossAbility unique = BossAbilityFactory.create(spec);
        if (unique != null) return unique;
        // Unknown ids (custom configs) fall back to a generic slot behaviour.
        final double range = spec.range() > 0 ? spec.range() : 8.0;
        return switch (index % 5) {
            case 0 -> new AreaDenialAbility(spec.id(), spec.damage(), spec.cooldownTicks(), range);
            case 1 -> new ProjectileAbility(spec.id(), spec.damage(), spec.cooldownTicks(), range);
            case 2 -> new SummonAbility(spec.id(), spec.damage(), spec.cooldownTicks(), range);
            case 3 -> new MobilityAbility(spec.id(), spec.damage(), spec.cooldownTicks(), range);
            default -> new EnvironmentAbility(spec.id(), spec.damage(), spec.cooldownTicks(), range);
        };
    }

    private void announce(@NotNull final Collection<? extends Player> players,
                          @NotNull final BossDefinition def, @NotNull final String key) {
        if (!def.isDialogueEnabled() || !def.hasDialogue(key)) return;
        final String line = def.getDialogue(key);
        players.forEach(p -> MiniMessageUtil.send(p, line));
    }

    /** Radius around the boss considered part of its arena. */
    private static final double ARENA_RADIUS_SQ = 64.0 * 64.0;

    @NotNull
    private Collection<Player> arenaPlayers(@NotNull final ActiveBoss boss) {
        if (boss.entity.getWorld() == null) return List.of();
        // Instances share one dungeon world — only address players near THIS boss.
        return boss.entity.getWorld().getPlayers().stream()
                .filter(p -> p.getLocation().distanceSquared(boss.entity.getLocation())
                        <= ARENA_RADIUS_SQ)
                .toList();
    }

    @SuppressWarnings("deprecation")
    private void applyHealth(@NotNull final LivingEntity boss, final double health) {
        try {
            boss.setMaxHealth(health);
            boss.setHealth(health);
        } catch (final IllegalArgumentException ex) {
            logger.debug("Could not set boss health: " + ex.getMessage());
        }
    }
}

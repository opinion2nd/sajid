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
 * Spawns and drives boss encounters: builds the boss entity from its definition,
 * scales health by difficulty, wires up the BossBar, phase state machine and
 * ability rotation, and fires a death hook when the boss falls.
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
    private final Map<UUID, ActiveBoss> active = new ConcurrentHashMap<>();

    /** Fired when a boss dies: (instanceId, bossId). */
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

    /** Spawns a boss for an instance and shows its BossBar to the arena players. */
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
        boss.customName(MiniMessageUtil.parse(def.getDisplayName()));
        boss.setCustomNameVisible(true);
        boss.setRemoveWhenFarAway(false);
        boss.setGlowing(true);
        boss.getPersistentDataContainer().set(bossKey, PersistentDataType.STRING, bossId);

        final List<IBossAbility> abilities = buildAbilities(def);
        final BossBarManager bar = new BossBarManager(
                MiniMessageUtil.stripTags(def.getDisplayName()), def.getBarColor(), def.getBarStyle());
        bar.show(arenaPlayers);

        final ActiveBoss activeBoss = new ActiveBoss(instanceId, def, boss,
                new BossHealthTracker(boss, maxHealth), bar,
                new BossStateMachine(def.getPhases()), new BossAI(abilities));
        active.put(instanceId, activeBoss);

        announce(arenaPlayers, def, "spawn");
        logger.info("Boss spawned: " + bossId + " for instance " + instanceId);
        return boss;
    }

    /** Per-tick update: BossBar, phase transitions, ability rotation and death. */
    public void tick(@NotNull final UUID instanceId) {
        final ActiveBoss boss = active.get(instanceId);
        if (boss == null || boss.dead) return;

        if (boss.health.isDead()) {
            handleDeath(boss);
            return;
        }
        final double ratio = boss.health.getHealthRatio();
        boss.bossBar.setProgress(ratio);

        final BossPhaseData newPhase = boss.stateMachine.update(ratio);
        if (newPhase != null && boss.def.hasDialogue("phase-two")
                && boss.stateMachine.getCurrentPhaseIndex() > 0) {
            announce(arenaPlayers(boss), boss.def, "phase-two");
        }
        boss.ai.tick(boss.entity);
    }

    public boolean hasActiveBoss(@NotNull final UUID instanceId) {
        return active.containsKey(instanceId) && !active.get(instanceId).dead;
    }

    public void cleanup(@NotNull final UUID instanceId) {
        final ActiveBoss boss = active.remove(instanceId);
        if (boss != null) {
            boss.bossBar.remove();
            if (!boss.entity.isDead()) boss.entity.remove();
        }
    }

    // ── Internal ────────────────────────────────────────────────────────────

    private void handleDeath(@NotNull final ActiveBoss boss) {
        boss.dead = true;
        boss.bossBar.remove();
        announce(arenaPlayers(boss), boss.def, "death");
        spawnVictoryFireworks(boss.entity.getLocation());
        active.remove(boss.instanceId);
        onBossDeath.accept(boss.instanceId, boss.def.getId());
        logger.info("Boss defeated: " + boss.def.getId() + " in instance " + boss.instanceId);
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

    @NotNull
    private Collection<Player> arenaPlayers(@NotNull final ActiveBoss boss) {
        return boss.entity.getWorld() != null ? boss.entity.getWorld().getPlayers() : List.of();
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

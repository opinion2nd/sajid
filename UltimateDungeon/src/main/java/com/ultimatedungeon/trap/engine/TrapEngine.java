package com.ultimatedungeon.trap.engine;

import com.ultimatedungeon.config.files.DifficultyConfig.DifficultyPreset;
import com.ultimatedungeon.config.files.TrapsConfig;
import com.ultimatedungeon.core.PluginLogger;
import com.ultimatedungeon.room.model.RoomData;
import com.ultimatedungeon.services.DifficultyService;
import com.ultimatedungeon.trap.model.TrapDefinition;
import com.ultimatedungeon.trap.model.TrapTriggerType;
import com.ultimatedungeon.trap.traps.*;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;

/**
 * Places, tracks, triggers and ticks traps for each dungeon instance.
 *
 * <p>Trap definitions come from {@code traps.yml}; a factory maps each trap id
 * to its concrete implementation. Movement traps fire from player-move checks;
 * timed and random traps fire from the trap tick task. All placed traps are
 * tracked per instance and dropped on cleanup.</p>
 */
public final class TrapEngine {

    private final DifficultyService difficulty;
    private final PluginLogger logger;
    private final TrapScaler scaler = new TrapScaler();
    private final TrapPlacer placer = new TrapPlacer();
    private final TrapTriggerDetector detector = new TrapTriggerDetector();

    private final Map<String, TrapDefinition> definitions = new LinkedHashMap<>();
    private final Map<String, Function<TrapDefinition, AbstractTrap>> factory = new LinkedHashMap<>();
    private final Map<UUID, List<AbstractTrap>> placed = new ConcurrentHashMap<>();

    public TrapEngine(@NotNull final TrapsConfig config,
                      @NotNull final DifficultyService difficulty,
                      @NotNull final PluginLogger logger) {
        this.difficulty = difficulty;
        this.logger = logger;
        registerFactory();
        loadDefinitions(config);
    }

    private void registerFactory() {
        factory.put("SpikeTrap", SpikeTrap::new);
        factory.put("ArrowLauncherTrap", ArrowLauncherTrap::new);
        factory.put("FallingBlockTrap", FallingBlockTrap::new);
        factory.put("FireHazardTrap", FireHazardTrap::new);
        factory.put("PoisonAreaTrap", PoisonAreaTrap::new);
        factory.put("ExplosiveTrap", ExplosiveTrap::new);
        factory.put("MovingObstacleTrap", MovingObstacleTrap::new);
        factory.put("HiddenTrap", HiddenTrap::new);
        // TNT family — one configurable implementation, seven tuned flavours.
        factory.put("PressurePlateTnt", TntTrap::new);
        factory.put("TripwireTnt", TntTrap::new);
        factory.put("DelayedTnt", TntTrap::new);
        factory.put("FallingTnt", TntTrap::new);
        factory.put("ChainTnt", TntTrap::new);
        factory.put("FakeChestTnt", TntTrap::new);
        factory.put("CorridorAmbushTnt", TntTrap::new);
    }

    private void loadDefinitions(@NotNull final TrapsConfig config) {
        final ConfigurationSection section = config.raw().getConfigurationSection("traps");
        if (section == null) {
            logger.warning("traps.yml has no 'traps' section.");
            return;
        }
        for (final String id : section.getKeys(false)) {
            final ConfigurationSection s = section.getConfigurationSection(id);
            if (s != null) definitions.put(id, TrapDefinition.fromSection(id, s));
        }
        logger.info("Loaded " + definitions.size() + " trap definition(s).");
    }

    /** Places up to {@code count} random traps in a room for an instance. */
    public void placeInRoom(@NotNull final UUID instanceId, @NotNull final RoomData room,
                            final int count, @NotNull final String difficultyId) {
        if (definitions.isEmpty()) return;
        final DifficultyPreset preset = difficulty.resolve(difficultyId);
        final List<String> ids = new ArrayList<>(definitions.keySet());
        final List<Location> spots = placer.pickSpots(room, count);
        final List<AbstractTrap> list = placed.computeIfAbsent(instanceId, k -> new ArrayList<>());
        for (final Location spot : spots) {
            final String trapId = ids.get(ThreadLocalRandom.current().nextInt(ids.size()));
            final TrapDefinition def = definitions.get(trapId);
            final Function<TrapDefinition, AbstractTrap> creator = factory.get(trapId);
            if (def == null || creator == null) continue;
            final AbstractTrap trap = creator.apply(def);
            trap.setScaledDamage(scaler.scaleDamage(def, preset));
            trap.place(spot);
            list.add(trap);
        }
    }

    /** Movement-trigger check for a single player. */
    public void onPlayerMove(@NotNull final UUID instanceId, @NotNull final Player player) {
        final List<AbstractTrap> list = placed.get(instanceId);
        if (list != null && !list.isEmpty()) detector.checkMovement(player, list);
    }

    /** Fires timed/random traps; called on the trap tick. */
    public void tick(@NotNull final UUID instanceId) {
        final List<AbstractTrap> list = placed.get(instanceId);
        if (list == null) return;
        for (final AbstractTrap trap : list) {
            final TrapTriggerType type = trap.getDefinition().getTriggerType();
            if (type == TrapTriggerType.TIMED) {
                trap.activate();
            } else if (type == TrapTriggerType.RANDOM && ThreadLocalRandom.current().nextDouble() < 0.15) {
                trap.activate();
            }
        }
    }

    public void cleanup(@NotNull final UUID instanceId) {
        placed.remove(instanceId);
    }
}

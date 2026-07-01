package com.ultimatedungeon.dungeon.instance;

import com.ultimatedungeon.boss.arena.ArenaCleanupService;
import com.ultimatedungeon.core.PluginLogger;
import com.ultimatedungeon.monster.engine.MonsterEngine;
import com.ultimatedungeon.monster.engine.WaveManager;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/** Tears down a completed or failed dungeon instance fully and safely. */
public final class DungeonCleanupService {

    private final ArenaCleanupService arenaCleanup;
    private final MonsterEngine monsterEngine;
    private final WaveManager waveManager;
    private final RoomSealer roomSealer;
    private final EncounterCountdownManager encounterCountdown;
    private final PluginLogger logger;

    public DungeonCleanupService(@NotNull final ArenaCleanupService arenaCleanup,
                                 @NotNull final MonsterEngine monsterEngine,
                                 @NotNull final WaveManager waveManager,
                                 @NotNull final RoomSealer roomSealer,
                                 @NotNull final EncounterCountdownManager encounterCountdown,
                                 @NotNull final PluginLogger logger) {
        this.arenaCleanup = arenaCleanup;
        this.monsterEngine = monsterEngine;
        this.waveManager = waveManager;
        this.roomSealer = roomSealer;
        this.encounterCountdown = encounterCountdown;
        this.logger = logger;
    }

    public void cleanup(@NotNull final DungeonInstance instance) {
        final UUID id = instance.getInstanceId();
        // Remove the boss bar + boss entity and unlock the arena. The boss bar is
        // a UI element, not a world entity, so deleting the world does NOT remove
        // it — this must run explicitly or the bar lingers on players' screens.
        arenaCleanup.cleanup(id);
        monsterEngine.despawnAll(id);
        waveManager.cancel(id);
        encounterCountdown.cancelInstance(id);
        roomSealer.clearInstance(id);
        instance.cleanup();
        logger.debug("Dungeon instance cleaned up: " + id);
    }
}

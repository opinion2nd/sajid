package com.ultimatedungeon.boss.arena;

import com.ultimatedungeon.boss.engine.BossEngine;
import com.ultimatedungeon.core.PluginLogger;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Tears down a boss arena once the encounter ends: unlocks the arena and removes
 * the boss entity and its BossBar via the {@link BossEngine}.
 */
public final class ArenaCleanupService {

    private final ArenaLockdownManager lockdown;
    private final BossEngine bossEngine;
    private final PluginLogger logger;

    public ArenaCleanupService(@NotNull final ArenaLockdownManager lockdown,
                               @NotNull final BossEngine bossEngine,
                               @NotNull final PluginLogger logger) {
        this.lockdown = lockdown;
        this.bossEngine = bossEngine;
        this.logger = logger;
    }

    public void cleanup(@NotNull final UUID instanceId) {
        lockdown.unlock(instanceId);
        bossEngine.cleanup(instanceId);
        logger.debug("Arena cleaned up for instance " + instanceId);
    }
}

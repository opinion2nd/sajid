package com.ultimatedungeon.tasks;

import com.ultimatedungeon.boss.engine.BossEngine;
import com.ultimatedungeon.dungeon.instance.DungeonInstanceManager;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

/** Ticks boss encounters for every active dungeon instance. */
public final class BossAITickTask extends BukkitRunnable {
    private final BossEngine bossEngine;
    private final DungeonInstanceManager instances;
    public BossAITickTask(@NotNull final BossEngine bossEngine,
                          @NotNull final DungeonInstanceManager instances) {
        this.bossEngine = bossEngine;
        this.instances = instances;
    }
    @Override public void run() {
        instances.getActiveInstances().forEach(i -> bossEngine.tick(i.getInstanceId()));
    }
}

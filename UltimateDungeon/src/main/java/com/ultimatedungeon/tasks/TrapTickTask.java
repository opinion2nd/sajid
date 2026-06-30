package com.ultimatedungeon.tasks;

import com.ultimatedungeon.dungeon.instance.DungeonInstanceManager;
import com.ultimatedungeon.trap.engine.TrapEngine;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

/** Fires timed and random traps for every active dungeon instance. */
public final class TrapTickTask extends BukkitRunnable {
    private final TrapEngine trapEngine;
    private final DungeonInstanceManager instances;
    public TrapTickTask(@NotNull final TrapEngine trapEngine,
                        @NotNull final DungeonInstanceManager instances) {
        this.trapEngine = trapEngine;
        this.instances = instances;
    }
    @Override public void run() {
        instances.getActiveInstances().forEach(i -> trapEngine.tick(i.getInstanceId()));
    }
}

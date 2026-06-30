package com.ultimatedungeon.tasks;

import com.ultimatedungeon.dungeon.instance.DungeonInstanceManager;
import com.ultimatedungeon.monster.engine.WaveManager;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

/** Per-instance heartbeat: advances wave encounters when cleared. */
public final class DungeonTickTask extends BukkitRunnable {
    private final WaveManager waveManager;
    private final DungeonInstanceManager instances;
    public DungeonTickTask(@NotNull final WaveManager waveManager,
                           @NotNull final DungeonInstanceManager instances) {
        this.waveManager = waveManager;
        this.instances = instances;
    }
    @Override public void run() {
        instances.getActiveInstances().forEach(i -> waveManager.poll(i.getInstanceId()));
    }
}

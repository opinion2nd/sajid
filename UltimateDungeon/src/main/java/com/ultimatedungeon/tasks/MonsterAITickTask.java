package com.ultimatedungeon.tasks;

import com.ultimatedungeon.dungeon.instance.DungeonInstanceManager;
import com.ultimatedungeon.monster.engine.MonsterEngine;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

/** Ticks monster AI for every active dungeon instance. */
public final class MonsterAITickTask extends BukkitRunnable {
    private final MonsterEngine monsterEngine;
    private final DungeonInstanceManager instances;
    public MonsterAITickTask(@NotNull final MonsterEngine monsterEngine,
                             @NotNull final DungeonInstanceManager instances) {
        this.monsterEngine = monsterEngine;
        this.instances = instances;
    }
    @Override public void run() {
        instances.getActiveInstances().forEach(i -> monsterEngine.tick(i.getInstanceId()));
    }
}

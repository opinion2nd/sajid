package com.ultimatedungeon.tasks;

import com.ultimatedungeon.dungeon.hazard.HazardEngine;
import com.ultimatedungeon.dungeon.instance.DungeonInstanceManager;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

/** Applies ambient room hazards to players in every active dungeon instance. */
public final class HazardTickTask extends BukkitRunnable {
    private final HazardEngine hazardEngine;
    private final DungeonInstanceManager instances;
    public HazardTickTask(@NotNull final HazardEngine hazardEngine,
                          @NotNull final DungeonInstanceManager instances) {
        this.hazardEngine = hazardEngine;
        this.instances = instances;
    }
    @Override public void run() {
        instances.getActiveInstances().forEach(hazardEngine::tick);
    }
}

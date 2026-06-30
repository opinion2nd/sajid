package com.ultimatedungeon.dungeon.lifecycle;

import com.ultimatedungeon.dungeon.instance.DungeonInstance;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Handles successful dungeon completion by delegating to {@link DungeonLauncher},
 * which owns the player set, rewards hook, statistics and cleanup pipeline.
 */
public final class DungeonEndHandler {

    private final DungeonLauncher launcher;

    public DungeonEndHandler(@NotNull final DungeonLauncher launcher) {
        this.launcher = launcher;
    }

    public void onComplete(@NotNull final DungeonInstance instance, @Nullable final String bossKilled) {
        launcher.complete(instance, bossKilled);
    }
}

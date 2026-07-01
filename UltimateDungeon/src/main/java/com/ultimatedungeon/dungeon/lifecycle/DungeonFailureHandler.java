package com.ultimatedungeon.dungeon.lifecycle;

import com.ultimatedungeon.dungeon.instance.DungeonInstance;
import org.jetbrains.annotations.NotNull;

/**
 * Handles dungeon failure (party wipe, abandonment) by delegating to
 * {@link DungeonLauncher} for notification, player return and cleanup.
 */
public final class DungeonFailureHandler {

    private final DungeonLauncher launcher;

    public DungeonFailureHandler(@NotNull final DungeonLauncher launcher) {
        this.launcher = launcher;
    }

    public void onFailure(@NotNull final DungeonInstance instance) {
        launcher.fail(instance);
    }
}

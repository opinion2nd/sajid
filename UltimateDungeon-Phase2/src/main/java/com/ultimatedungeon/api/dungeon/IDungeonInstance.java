package com.ultimatedungeon.api.dungeon;

import org.jetbrains.annotations.NotNull;
import java.util.UUID;

/** Contract for an active dungeon instance. */
public interface IDungeonInstance {
    @NotNull UUID getInstanceId();
    boolean isActive();
    void end();
    void fail();
    void cleanup();
}

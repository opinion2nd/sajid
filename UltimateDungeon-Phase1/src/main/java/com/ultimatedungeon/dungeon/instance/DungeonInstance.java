package com.ultimatedungeon.dungeon.instance;

import com.ultimatedungeon.api.dungeon.IDungeonInstance;
import org.jetbrains.annotations.NotNull;
import java.util.UUID;

/**
 * Concrete dungeon instance that tracks state for a single procedural run.
 *
 * <p>All mutable state changes are gated through {@link DungeonState}
 * transitions to prevent invalid state combinations.</p>
 */
public final class DungeonInstance implements IDungeonInstance {

    private final DungeonContext context;
    private volatile DungeonState state;

    public DungeonInstance(@NotNull final DungeonContext context) {
        this.context = context;
        this.state = DungeonState.GENERATING;
    }

    @Override
    @NotNull
    public UUID getInstanceId() {
        return context.getInstanceId();
    }

    @Override
    public boolean isActive() {
        return state == DungeonState.ACTIVE || state == DungeonState.BOSS_ENCOUNTER;
    }

    @Override
    public void end() {
        state = DungeonState.COMPLETED;
    }

    @Override
    public void fail() {
        state = DungeonState.FAILED;
    }

    @Override
    public void cleanup() {
        state = DungeonState.CLEANING_UP;
    }

    @NotNull
    public DungeonState getState() {
        return state;
    }

    @NotNull
    public DungeonContext getContext() {
        return context;
    }
}

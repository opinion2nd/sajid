package com.ultimatedungeon.dungeon.instance;

import com.ultimatedungeon.api.dungeon.IDungeonInstance;
import com.ultimatedungeon.room.model.RoomGraph;
import com.ultimatedungeon.theme.model.ThemeDefinition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

    // Set after generation completes
    private volatile RoomGraph       roomGraph;
    private volatile ThemeDefinition theme;

    public DungeonInstance(@NotNull final DungeonContext context) {
        this.context = context;
        this.state   = DungeonState.GENERATING;
    }

    // ── IDungeonInstance ──────────────────────────────────────────────────────

    @Override
    @NotNull
    public UUID getInstanceId() { return context.getInstanceId(); }

    @Override
    public boolean isActive() {
        return state == DungeonState.ACTIVE || state == DungeonState.BOSS_ENCOUNTER;
    }

    @Override public void end()     { state = DungeonState.COMPLETED;    }
    @Override public void fail()    { state = DungeonState.FAILED;       }
    @Override public void cleanup() { state = DungeonState.CLEANING_UP;  }

    // ── State ─────────────────────────────────────────────────────────────────

    @NotNull  public DungeonState getState()    { return state;   }
    @NotNull  public DungeonContext getContext() { return context; }

    public void setReady() { state = DungeonState.READY; }
    public void setActive(){ state = DungeonState.ACTIVE;}

    // ── Generation results ────────────────────────────────────────────────────

    public void setRoomGraph(@NotNull final RoomGraph graph) {
        this.roomGraph = graph;
        this.state     = DungeonState.READY;
    }

    public void setTheme(@NotNull final ThemeDefinition theme) {
        this.theme = theme;
    }

    @Nullable public RoomGraph       getRoomGraph() { return roomGraph; }
    @Nullable public ThemeDefinition getTheme()     { return theme;     }
}

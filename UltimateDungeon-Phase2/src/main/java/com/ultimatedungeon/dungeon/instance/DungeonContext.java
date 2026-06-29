package com.ultimatedungeon.dungeon.instance;

import com.ultimatedungeon.api.dungeon.DungeonGenerationRequest;
import org.jetbrains.annotations.NotNull;
import java.util.UUID;

/**
 * Immutable context object carrying all scoped data for a single dungeon run.
 *
 * <p>Passed through every system that operates on this dungeon instance,
 * replacing scattered static state and making cleanup trivial.</p>
 */
public final class DungeonContext {

    private final UUID instanceId;
    private final DungeonGenerationRequest request;
    private final long createdAt;

    public DungeonContext(
            @NotNull final UUID instanceId,
            @NotNull final DungeonGenerationRequest request
    ) {
        this.instanceId = instanceId;
        this.request = request;
        this.createdAt = System.currentTimeMillis();
    }

    @NotNull public UUID getInstanceId() { return instanceId; }
    @NotNull public DungeonGenerationRequest getRequest() { return request; }
    public long getCreatedAt() { return createdAt; }
    public long getElapsedMs() { return System.currentTimeMillis() - createdAt; }
}

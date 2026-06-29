package com.ultimatedungeon.api.dungeon;

import org.jetbrains.annotations.NotNull;
import java.util.concurrent.CompletableFuture;

/** Contract for the procedural dungeon generation engine. */
public interface IDungeonGenerator {
    @NotNull CompletableFuture<IDungeonInstance> generate(@NotNull DungeonGenerationRequest request);
}

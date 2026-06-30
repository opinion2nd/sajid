package com.ultimatedungeon.dungeon.generation;

import com.ultimatedungeon.room.model.RoomGraph;
import com.ultimatedungeon.theme.model.ThemeDefinition;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Immutable result of a successful dungeon generation.
 *
 * <p>Passed from {@link GenerationPipeline} to the caller (DungeonLauncher)
 * so players can be teleported to the spawn point and the dungeon can start.</p>
 */
public final class GenerationResult {

    private final UUID            instanceId;
    private final RoomGraph       roomGraph;
    private final ThemeDefinition theme;
    private final long            generationTimeMs;

    public GenerationResult(
            @NotNull final UUID            instanceId,
            @NotNull final RoomGraph       roomGraph,
            @NotNull final ThemeDefinition theme,
            final long                     generationTimeMs
    ) {
        this.instanceId       = instanceId;
        this.roomGraph        = roomGraph;
        this.theme            = theme;
        this.generationTimeMs = generationTimeMs;
    }

    @NotNull public UUID            getInstanceId()       { return instanceId;       }
    @NotNull public RoomGraph       getRoomGraph()        { return roomGraph;        }
    @NotNull public ThemeDefinition getTheme()            { return theme;            }
    public long                     getGenerationTimeMs() { return generationTimeMs; }
}

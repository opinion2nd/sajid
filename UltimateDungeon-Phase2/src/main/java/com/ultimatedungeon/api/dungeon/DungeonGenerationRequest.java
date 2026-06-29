package com.ultimatedungeon.api.dungeon;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.UUID;

/** Immutable value object describing a dungeon generation request. */
public final class DungeonGenerationRequest {
    private final UUID requesterId;
    private final String themeId;
    private final String difficultyId;
    private final boolean partyMode;
    private final @Nullable UUID partyId;

    public DungeonGenerationRequest(
            @NotNull final UUID requesterId,
            @NotNull final String themeId,
            @NotNull final String difficultyId,
            final boolean partyMode,
            @Nullable final UUID partyId
    ) {
        this.requesterId = requesterId;
        this.themeId = themeId;
        this.difficultyId = difficultyId;
        this.partyMode = partyMode;
        this.partyId = partyId;
    }

    @NotNull public UUID getRequesterId() { return requesterId; }
    @NotNull public String getThemeId() { return themeId; }
    @NotNull public String getDifficultyId() { return difficultyId; }
    public boolean isPartyMode() { return partyMode; }
    @Nullable public UUID getPartyId() { return partyId; }
}

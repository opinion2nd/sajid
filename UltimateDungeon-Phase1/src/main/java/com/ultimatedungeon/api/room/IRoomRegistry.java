package com.ultimatedungeon.api.room;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.Collection;

/** Contract for room template registration and weighted selection. */
public interface IRoomRegistry {
    void register(@NotNull IRoomTemplate template);
    @Nullable IRoomTemplate getTemplate(@NotNull String templateId);
    @NotNull IRoomTemplate selectWeighted();
    @NotNull Collection<IRoomTemplate> getAllTemplates();
}

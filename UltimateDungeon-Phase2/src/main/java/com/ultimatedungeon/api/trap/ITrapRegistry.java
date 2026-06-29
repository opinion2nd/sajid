package com.ultimatedungeon.api.trap;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.Collection;

/** Contract for trap type registration and lookup. */
public interface ITrapRegistry {
    void register(@NotNull ITrap trap);
    @Nullable ITrap getTrap(@NotNull String trapId);
    @NotNull Collection<ITrap> getAllTraps();
}

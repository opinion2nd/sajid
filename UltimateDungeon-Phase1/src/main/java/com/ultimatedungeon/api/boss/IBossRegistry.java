package com.ultimatedungeon.api.boss;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.Collection;

/** Contract for boss type registration and lookup. */
public interface IBossRegistry {
    void register(@NotNull IBoss boss);
    @Nullable IBoss getBoss(@NotNull String bossId);
    @NotNull Collection<IBoss> getAllBosses();
}

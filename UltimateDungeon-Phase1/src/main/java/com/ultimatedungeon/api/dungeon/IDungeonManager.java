package com.ultimatedungeon.api.dungeon;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.Collection;
import java.util.UUID;

/** Contract for dungeon instance lifecycle management. */
public interface IDungeonManager {
    void registerInstance(@NotNull IDungeonInstance instance);
    void removeInstance(@NotNull UUID instanceId);
    @Nullable IDungeonInstance getInstance(@NotNull UUID instanceId);
    @Nullable IDungeonInstance getInstanceForPlayer(@NotNull Player player);
    @NotNull Collection<IDungeonInstance> getActiveInstances();
    boolean isPlayerInDungeon(@NotNull Player player);
}

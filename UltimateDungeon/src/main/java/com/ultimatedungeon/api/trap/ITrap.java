package com.ultimatedungeon.api.trap;

import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

/** Contract for a dungeon trap. */
public interface ITrap {
    @NotNull String getTrapId();
    void place(@NotNull Location location);
    void activate();
    void reset();
    boolean isActive();
}

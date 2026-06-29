package com.ultimatedungeon.api.room;

import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

/** Contract for a placed dungeon room. */
public interface IRoom {
    @NotNull String getRoomId();
    @NotNull Location getOrigin();
    boolean isCleared();
    void onEnter();
    void onClear();
}

package com.ultimatedungeon.api.room;

import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

/** Contract for a procedural room blueprint. */
public interface IRoomTemplate {
    @NotNull String getTemplateId();
    int getWeight();
    @NotNull IRoom place(@NotNull Location origin);
}

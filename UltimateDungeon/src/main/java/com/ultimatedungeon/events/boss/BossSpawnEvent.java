package com.ultimatedungeon.events.boss;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/** Custom event fired when BossSpawn occurs. Populated in Milestone 4. */
public final class BossSpawnEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();
    public BossSpawnEvent() {}
    @Override @NotNull public HandlerList getHandlers() { return HANDLERS; }
    @NotNull public static HandlerList getHandlerList() { return HANDLERS; }
}

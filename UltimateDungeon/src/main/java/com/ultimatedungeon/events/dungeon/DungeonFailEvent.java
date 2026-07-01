package com.ultimatedungeon.events.dungeon;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/** Custom event fired when DungeonFail occurs. Populated with context in Milestone 3. */
public final class DungeonFailEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();
    public DungeonFailEvent() {}
    @Override @NotNull public HandlerList getHandlers() { return HANDLERS; }
    @NotNull public static HandlerList getHandlerList() { return HANDLERS; }
}

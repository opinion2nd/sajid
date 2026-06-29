package com.ultimatedungeon.events.room;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public final class RoomClearEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();
    public RoomClearEvent() {}
    @Override @NotNull public HandlerList getHandlers() { return HANDLERS; }
    @NotNull public static HandlerList getHandlerList() { return HANDLERS; }
}

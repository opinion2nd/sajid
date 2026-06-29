package com.ultimatedungeon.events.monster;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public final class WaveClearEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();
    public WaveClearEvent() {}
    @Override @NotNull public HandlerList getHandlers() { return HANDLERS; }
    @NotNull public static HandlerList getHandlerList() { return HANDLERS; }
}

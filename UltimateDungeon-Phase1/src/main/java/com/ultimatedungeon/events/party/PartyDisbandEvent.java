package com.ultimatedungeon.events.party;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public final class PartyDisbandEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();
    public PartyDisbandEvent() {}
    @Override @NotNull public HandlerList getHandlers() { return HANDLERS; }
    @NotNull public static HandlerList getHandlerList() { return HANDLERS; }
}

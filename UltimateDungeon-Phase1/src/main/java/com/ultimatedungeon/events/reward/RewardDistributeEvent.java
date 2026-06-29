package com.ultimatedungeon.events.reward;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public final class RewardDistributeEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();
    public RewardDistributeEvent() {}
    @Override @NotNull public HandlerList getHandlers() { return HANDLERS; }
    @NotNull public static HandlerList getHandlerList() { return HANDLERS; }
}

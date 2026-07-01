package com.ultimatedungeon.events.party;

import com.ultimatedungeon.party.model.Party;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Fired when a player creates a new party.
 * Cancelling this event prevents the party from being created.
 */
public final class PartyCreateEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Player leader;
    private boolean cancelled;

    public PartyCreateEvent(@NotNull final Player leader) {
        this.leader    = leader;
        this.cancelled = false;
    }

    @NotNull public Player getLeader() { return leader; }

    @Override public boolean isCancelled() { return cancelled; }
    @Override public void setCancelled(final boolean cancel) { this.cancelled = cancel; }
    @Override @NotNull public HandlerList getHandlers() { return HANDLERS; }
    @NotNull public static HandlerList getHandlerList() { return HANDLERS; }
}

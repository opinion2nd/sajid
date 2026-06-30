package com.ultimatedungeon.events.party;

import com.ultimatedungeon.party.model.Party;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/** Fired when a player is about to join a party. Cancellable. */
public final class PartyMemberJoinEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Party  party;
    private final Player player;
    private boolean cancelled;

    public PartyMemberJoinEvent(@NotNull final Party party, @NotNull final Player player) {
        this.party  = party;
        this.player = player;
    }

    @NotNull public Party  getParty()  { return party;  }
    @NotNull public Player getPlayer() { return player; }

    @Override public boolean isCancelled() { return cancelled; }
    @Override public void setCancelled(final boolean cancel) { this.cancelled = cancel; }
    @Override @NotNull public HandlerList getHandlers() { return HANDLERS; }
    @NotNull public static HandlerList getHandlerList() { return HANDLERS; }
}

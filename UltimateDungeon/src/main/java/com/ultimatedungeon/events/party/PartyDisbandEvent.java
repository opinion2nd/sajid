package com.ultimatedungeon.events.party;

import com.ultimatedungeon.party.model.Party;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/** Fired after a party is disbanded. Not cancellable — teardown is already complete. */
public final class PartyDisbandEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Party  party;
    private final String reason;

    public PartyDisbandEvent(@NotNull final Party party, @NotNull final String reason) {
        this.party  = party;
        this.reason = reason;
    }

    @NotNull public Party  getParty()  { return party;  }
    @NotNull public String getReason() { return reason; }

    @Override @NotNull public HandlerList getHandlers() { return HANDLERS; }
    @NotNull public static HandlerList getHandlerList() { return HANDLERS; }
}

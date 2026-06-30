package com.ultimatedungeon.events.party;

import com.ultimatedungeon.party.model.Party;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/** Fired after a player leaves a party (voluntarily, kicked, or disconnected). */
public final class PartyMemberLeaveEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    public enum Reason { VOLUNTARY, KICKED, DISCONNECT, DISBAND }

    private final Party  party;
    private final Player player;
    private final Reason reason;

    public PartyMemberLeaveEvent(
            @NotNull final Party  party,
            @NotNull final Player player,
            @NotNull final Reason reason
    ) {
        this.party  = party;
        this.player = player;
        this.reason = reason;
    }

    @NotNull public Party  getParty()  { return party;  }
    @NotNull public Player getPlayer() { return player; }
    @NotNull public Reason getReason() { return reason; }

    @Override @NotNull public HandlerList getHandlers() { return HANDLERS; }
    @NotNull public static HandlerList getHandlerList() { return HANDLERS; }
}

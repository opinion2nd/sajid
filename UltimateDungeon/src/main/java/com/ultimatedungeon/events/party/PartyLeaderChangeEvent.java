package com.ultimatedungeon.events.party;

import com.ultimatedungeon.party.model.Party;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/** Fired when party leadership is transferred to a new player. */
public final class PartyLeaderChangeEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Party  party;
    private final Player oldLeader;
    private final Player newLeader;

    public PartyLeaderChangeEvent(
            @NotNull final Party  party,
            @NotNull final Player oldLeader,
            @NotNull final Player newLeader
    ) {
        this.party     = party;
        this.oldLeader = oldLeader;
        this.newLeader = newLeader;
    }

    @NotNull public Party  getParty()     { return party;     }
    @NotNull public Player getOldLeader() { return oldLeader; }
    @NotNull public Player getNewLeader() { return newLeader; }

    @Override @NotNull public HandlerList getHandlers() { return HANDLERS; }
    @NotNull public static HandlerList getHandlerList() { return HANDLERS; }
}

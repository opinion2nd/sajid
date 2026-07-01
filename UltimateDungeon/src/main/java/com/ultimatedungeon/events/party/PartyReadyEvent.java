package com.ultimatedungeon.events.party;

import com.ultimatedungeon.party.model.Party;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/** Fired when all party members pass the ready check and the dungeon can launch. */
public final class PartyReadyEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Party party;

    public PartyReadyEvent(@NotNull final Party party) {
        this.party = party;
    }

    @NotNull public Party getParty() { return party; }

    @Override @NotNull public HandlerList getHandlers() { return HANDLERS; }
    @NotNull public static HandlerList getHandlerList() { return HANDLERS; }
}

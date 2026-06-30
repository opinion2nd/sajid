package com.ultimatedungeon.party.model;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/** Lightweight value object wrapping a party member with their join timestamp. */
public final class PartyMember {

    private final Player player;
    private final long joinedAt;
    private volatile boolean ready;

    public PartyMember(@NotNull final Player player) {
        this.player = player;
        this.joinedAt = System.currentTimeMillis();
        this.ready = false;
    }

    @NotNull public Player getPlayer() { return player; }
    public long getJoinedAt() { return joinedAt; }
    public boolean isReady() { return ready; }
    public void setReady(final boolean ready) { this.ready = ready; }
}

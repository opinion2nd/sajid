package com.ultimatedungeon.party.model;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import java.util.UUID;

/** An open party invitation with its expiry timestamp. */
public final class PartyInvitation {

    private final UUID partyId;
    private final Player inviter;
    private final Player invitee;
    private final long expiresAt;

    public PartyInvitation(
            @NotNull final UUID partyId,
            @NotNull final Player inviter,
            @NotNull final Player invitee,
            final long expiresAt
    ) {
        this.partyId = partyId;
        this.inviter = inviter;
        this.invitee = invitee;
        this.expiresAt = expiresAt;
    }

    @NotNull public UUID getPartyId() { return partyId; }
    @NotNull public Player getInviter() { return inviter; }
    @NotNull public Player getInvitee() { return invitee; }
    public long getExpiresAt() { return expiresAt; }
    public boolean isExpired() { return System.currentTimeMillis() > expiresAt; }
}

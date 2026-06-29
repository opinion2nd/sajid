package com.ultimatedungeon.party.model;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** Tracks the ready state of every party member during a ready check. */
public final class ReadyCheckSession {

    private final UUID partyId;
    private final long expiresAt;
    private final Map<UUID, Boolean> responses = new ConcurrentHashMap<>();

    public ReadyCheckSession(
            @NotNull final UUID partyId,
            final long expiresAt
    ) {
        this.partyId = partyId;
        this.expiresAt = expiresAt;
    }

    public void recordResponse(@NotNull final Player player, final boolean ready) {
        responses.put(player.getUniqueId(), ready);
    }

    public boolean allReady() {
        return !responses.isEmpty() && responses.values().stream().allMatch(Boolean::booleanValue);
    }

    public boolean isExpired() { return System.currentTimeMillis() > expiresAt; }
    @NotNull public UUID getPartyId() { return partyId; }
    @NotNull public Map<UUID, Boolean> getResponses() { return responses; }
}

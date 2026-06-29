package com.ultimatedungeon.party.model;

import com.ultimatedungeon.api.party.IParty;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Mutable party model tracking all members and the current dungeon assignment.
 */
public final class Party implements IParty {

    private final UUID partyId;
    private final List<Player> members = new CopyOnWriteArrayList<>();
    private volatile UUID leaderId;
    private volatile UUID dungeonInstanceId;
    private volatile PartyState state = PartyState.FORMING;
    private final int maxSize;

    public Party(
            @NotNull final UUID partyId,
            @NotNull final Player leader,
            final int maxSize
    ) {
        this.partyId = partyId;
        this.leaderId = leader.getUniqueId();
        this.maxSize = maxSize;
        members.add(leader);
    }

    @Override @NotNull public UUID getPartyId() { return partyId; }

    @Override
    @NotNull
    public Player getLeader() {
        return members.stream()
                .filter(p -> p.getUniqueId().equals(leaderId))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Party leader not found in member list."));
    }

    @Override @NotNull public Collection<Player> getMembers() { return Collections.unmodifiableList(members); }
    @Override public boolean isMember(@NotNull final Player p) { return members.contains(p); }
    @Override public boolean isLeader(@NotNull final Player p) { return p.getUniqueId().equals(leaderId); }
    @Override public boolean isFull() { return members.size() >= maxSize; }
    @Override public int getSize() { return members.size(); }
    @Override @Nullable public UUID getDungeonInstanceId() { return dungeonInstanceId; }

    public void addMember(@NotNull final Player player) { members.add(player); }
    public void removeMember(@NotNull final Player player) { members.remove(player); }
    public void transferLeadership(@NotNull final UUID newLeaderId) { this.leaderId = newLeaderId; }
    public void setDungeonInstanceId(@Nullable final UUID id) { this.dungeonInstanceId = id; }
    @NotNull public PartyState getState() { return state; }
    public void setState(@NotNull final PartyState state) { this.state = state; }
}

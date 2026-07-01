package com.ultimatedungeon.api.party;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.Collection;
import java.util.UUID;

/** Contract for a player party. */
public interface IParty {
    @NotNull UUID getPartyId();
    @NotNull Player getLeader();
    @NotNull Collection<Player> getMembers();
    boolean isMember(@NotNull Player player);
    boolean isLeader(@NotNull Player player);
    boolean isFull();
    int getSize();
    @Nullable UUID getDungeonInstanceId();
}

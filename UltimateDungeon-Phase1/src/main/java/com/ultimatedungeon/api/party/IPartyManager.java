package com.ultimatedungeon.api.party;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.Collection;
import java.util.UUID;

/** Contract for party lifecycle management. */
public interface IPartyManager {
    @NotNull IParty createParty(@NotNull Player leader);
    void disbandParty(@NotNull UUID partyId);
    @Nullable IParty getParty(@NotNull UUID partyId);
    @Nullable IParty getPartyForPlayer(@NotNull Player player);
    @NotNull Collection<IParty> getAllParties();
    boolean isInParty(@NotNull Player player);
}

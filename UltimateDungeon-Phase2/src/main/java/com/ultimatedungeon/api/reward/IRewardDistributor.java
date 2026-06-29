package com.ultimatedungeon.api.reward;

import com.ultimatedungeon.rewards.model.RewardEvent;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import java.util.Collection;

/** Contract for distributing rewards to players. */
public interface IRewardDistributor {
    void distribute(@NotNull Player player, @NotNull RewardEvent event);
    void distributeAll(@NotNull Collection<Player> players, @NotNull RewardEvent event);
}

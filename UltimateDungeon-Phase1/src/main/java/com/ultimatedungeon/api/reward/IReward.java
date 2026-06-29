package com.ultimatedungeon.api.reward;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/** Contract for a single reward type. */
public interface IReward {
    @NotNull String getRewardType();
    void deliver(@NotNull Player player);
}

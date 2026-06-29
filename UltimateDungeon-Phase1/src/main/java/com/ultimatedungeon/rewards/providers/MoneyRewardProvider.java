package com.ultimatedungeon.rewards.providers;

import com.ultimatedungeon.api.reward.IReward;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/** MoneyRewardProvider — reward delivery implementation. Milestone 6. */
public final class MoneyRewardProvider implements IReward {
    @Override @NotNull public String getRewardType() { return "MoneyRewardProvider"; }
    @Override public void deliver(@NotNull final Player player) {}
}

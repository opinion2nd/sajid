package com.ultimatedungeon.rewards.providers;

import com.ultimatedungeon.api.reward.IReward;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/** CommandRewardProvider — reward delivery implementation. Milestone 6. */
public final class CommandRewardProvider implements IReward {
    @Override @NotNull public String getRewardType() { return "CommandRewardProvider"; }
    @Override public void deliver(@NotNull final Player player) {}
}

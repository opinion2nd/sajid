package com.ultimatedungeon.rewards.providers;

import com.ultimatedungeon.api.reward.IReward;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/** TokenRewardProvider — reward delivery implementation. Milestone 6. */
public final class TokenRewardProvider implements IReward {
    @Override @NotNull public String getRewardType() { return "TokenRewardProvider"; }
    @Override public void deliver(@NotNull final Player player) {}
}

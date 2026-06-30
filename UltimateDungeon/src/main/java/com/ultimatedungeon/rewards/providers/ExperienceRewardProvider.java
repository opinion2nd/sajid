package com.ultimatedungeon.rewards.providers;

import com.ultimatedungeon.api.reward.IReward;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/** ExperienceRewardProvider — reward delivery implementation. Milestone 6. */
public final class ExperienceRewardProvider implements IReward {
    @Override @NotNull public String getRewardType() { return "ExperienceRewardProvider"; }
    @Override public void deliver(@NotNull final Player player) {}
}

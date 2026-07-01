package com.ultimatedungeon.rewards.providers;

import com.ultimatedungeon.api.reward.IReward;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/** Grants a configured amount of vanilla experience. */
public final class ExperienceRewardProvider implements IReward {
    private final int experience;
    public ExperienceRewardProvider(final int experience) { this.experience = experience; }
    @Override @NotNull public String getRewardType() { return "EXPERIENCE"; }
    @Override public void deliver(@NotNull final Player player) {
        if (experience > 0) player.giveExp(experience);
    }
}

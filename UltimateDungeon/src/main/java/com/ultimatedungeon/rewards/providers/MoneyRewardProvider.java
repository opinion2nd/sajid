package com.ultimatedungeon.rewards.providers;

import com.ultimatedungeon.api.economy.IEconomyProvider;
import com.ultimatedungeon.api.reward.IReward;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/** Deposits a configured amount of currency into the player's account. */
public final class MoneyRewardProvider implements IReward {
    private final double amount;
    private final IEconomyProvider economy;
    public MoneyRewardProvider(final double amount, @NotNull final IEconomyProvider economy) {
        this.amount = amount; this.economy = economy;
    }
    @Override @NotNull public String getRewardType() { return "MONEY"; }
    @Override public void deliver(@NotNull final Player player) {
        if (amount > 0 && economy.isAvailable()) economy.deposit(player, amount);
    }
}

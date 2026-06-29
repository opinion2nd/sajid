package com.ultimatedungeon.api.economy;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/** Abstraction over Vault economy or a no-op fallback. */
public interface IEconomyProvider {
    boolean isAvailable();
    boolean deposit(@NotNull Player player, double amount);
    double getBalance(@NotNull Player player);
}

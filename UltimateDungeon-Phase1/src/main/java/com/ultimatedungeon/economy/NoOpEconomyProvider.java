package com.ultimatedungeon.economy;

import com.ultimatedungeon.api.economy.IEconomyProvider;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * No-operation economy provider used when Vault is not present.
 *
 * <p>All money reward operations silently succeed (no-op) so the plugin
 * operates normally on servers without an economy plugin installed.</p>
 */
public final class NoOpEconomyProvider implements IEconomyProvider {

    @Override public boolean isAvailable() { return false; }
    @Override public boolean deposit(@NotNull final Player player, final double amount) { return false; }
    @Override public double getBalance(@NotNull final Player player) { return 0.0; }
}

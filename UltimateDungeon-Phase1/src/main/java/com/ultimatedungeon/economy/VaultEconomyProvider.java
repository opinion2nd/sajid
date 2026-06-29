package com.ultimatedungeon.economy;

import com.ultimatedungeon.api.economy.IEconomyProvider;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Vault-backed economy provider.
 *
 * <p>Hooks into Vault during plugin enable. If Vault or an economy plugin is
 * not present, falls back to {@link NoOpEconomyProvider} gracefully.</p>
 */
public final class VaultEconomyProvider implements IEconomyProvider {

    @Override
    public boolean isAvailable() {
        // Phase 1 implementation: check Vault hook.
        return false;
    }

    @Override
    public boolean deposit(@NotNull final Player player, final double amount) {
        // Milestone 6 implementation.
        return false;
    }

    @Override
    public double getBalance(@NotNull final Player player) {
        return 0.0;
    }
}

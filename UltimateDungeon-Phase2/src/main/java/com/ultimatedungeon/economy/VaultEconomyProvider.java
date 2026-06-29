package com.ultimatedungeon.economy;

import com.ultimatedungeon.UltimateDungeon;
import com.ultimatedungeon.api.economy.IEconomyProvider;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Vault-backed economy provider.
 *
 * <p>Hooks into Vault's {@link Economy} service on construction. If Vault or an
 * economy plugin is absent, {@link #isAvailable()} returns {@code false} and all
 * monetary operations are silent no-ops. The bootstrap then falls back to
 * {@link NoOpEconomyProvider}.</p>
 */
public final class VaultEconomyProvider implements IEconomyProvider {

    @Nullable private final Economy economy;

    public VaultEconomyProvider(@NotNull final UltimateDungeon plugin) {
        this.economy = hookVault(plugin);
    }

    // ── IEconomyProvider ──────────────────────────────────────────────────────

    @Override
    public boolean isAvailable() {
        return economy != null;
    }

    @Override
    public boolean deposit(@NotNull final Player player, final double amount) {
        if (economy == null || amount <= 0) return false;
        return economy.depositPlayer(player, amount).transactionSuccess();
    }

    @Override
    public double getBalance(@NotNull final Player player) {
        if (economy == null) return 0.0;
        return economy.getBalance(player);
    }

    // ── Private ───────────────────────────────────────────────────────────────

    @Nullable
    private Economy hookVault(@NotNull final UltimateDungeon plugin) {
        if (plugin.getServer().getPluginManager().getPlugin("Vault") == null) {
            return null;
        }
        final RegisteredServiceProvider<Economy> rsp =
                plugin.getServer().getServicesManager().getRegistration(Economy.class);
        return rsp != null ? rsp.getProvider() : null;
    }
}

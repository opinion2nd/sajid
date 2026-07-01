package com.ultimatedungeon.boss.engine;

import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/** Wraps a Bukkit {@link BossBar} for a single boss encounter. */
public final class BossBarManager {

    private final BossBar bar;

    public BossBarManager(@NotNull final String title, @NotNull final BarColor color, @NotNull final BarStyle style) {
        this.bar = Bukkit.createBossBar(title, color, style);
        this.bar.setProgress(1.0);
    }

    public void show(@NotNull final Collection<? extends Player> players) {
        players.forEach(bar::addPlayer);
    }

    public void addPlayer(@NotNull final Player player) {
        bar.addPlayer(player);
    }

    /**
     * Reconciles the bar's viewers with {@code players}: anyone who entered the
     * arena late starts seeing the bar, anyone who left the world stops.
     */
    public void syncViewers(@NotNull final Collection<? extends Player> players) {
        for (final Player current : bar.getPlayers()) {
            if (!players.contains(current)) bar.removePlayer(current);
        }
        players.forEach(bar::addPlayer); // addPlayer is a no-op for existing viewers
    }

    public void setProgress(final double ratio) {
        bar.setProgress(Math.max(0.0, Math.min(1.0, ratio)));
    }

    public void setTitle(@NotNull final String title) {
        bar.setTitle(title);
    }

    public void remove() {
        bar.removeAll();
        bar.setVisible(false);
    }
}

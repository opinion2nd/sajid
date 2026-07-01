package com.ultimatedungeon.services;

import com.ultimatedungeon.core.PluginLogger;
import com.ultimatedungeon.util.MiniMessageUtil;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Map;

/**
 * Central delivery point for all player-facing feedback — chat, titles,
 * action bars and sounds. Every message is rendered through MiniMessage so
 * formatting stays fully configurable.
 */
public final class NotificationService {

    private final PluginLogger logger;

    public NotificationService(@NotNull final PluginLogger logger) {
        this.logger = logger;
    }

    // ── Chat ──────────────────────────────────────────────────────────────────

    public void chat(@NotNull final Player player, @NotNull final String miniMessage) {
        MiniMessageUtil.send(player, miniMessage);
    }

    public void chat(@NotNull final Player player, @NotNull final String miniMessage,
                     @NotNull final Map<String, String> placeholders) {
        MiniMessageUtil.send(player, miniMessage, placeholders);
    }

    public void broadcast(@NotNull final Collection<? extends Player> players, @NotNull final String miniMessage) {
        for (final Player p : players) MiniMessageUtil.send(p, miniMessage);
    }

    // ── Title / action bar ──────────────────────────────────────────────────

    public void title(@NotNull final Player player, @NotNull final String title, @NotNull final String subtitle,
                      final int fadeIn, final int stay, final int fadeOut) {
        MiniMessageUtil.sendTitle(player, title, subtitle, fadeIn, stay, fadeOut);
    }

    public void title(@NotNull final Player player, @NotNull final String title, @NotNull final String subtitle) {
        title(player, title, subtitle, 10, 50, 10);
    }

    public void actionBar(@NotNull final Player player, @NotNull final String miniMessage) {
        MiniMessageUtil.sendActionBar(player, miniMessage);
    }

    // ── Sound ─────────────────────────────────────────────────────────────────

    public void sound(@NotNull final Player player, @NotNull final Sound sound, final float volume, final float pitch) {
        player.playSound(player.getLocation(), sound, volume, pitch);
    }

    /** Plays a sound by name, silently ignoring an unknown identifier. */
    public void sound(@NotNull final Player player, @NotNull final String soundName, final float volume, final float pitch) {
        try {
            player.playSound(player.getLocation(), Sound.valueOf(soundName.toUpperCase()), volume, pitch);
        } catch (final IllegalArgumentException ex) {
            logger.debug("Unknown sound ignored: " + soundName);
        }
    }
}

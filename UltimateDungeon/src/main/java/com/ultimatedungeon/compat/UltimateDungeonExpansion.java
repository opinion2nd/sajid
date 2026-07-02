package com.ultimatedungeon.compat;

import com.ultimatedungeon.UltimateDungeon;
import com.ultimatedungeon.database.dao.IPlayerStatsDao;
import com.ultimatedungeon.services.StatisticsService;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * PlaceholderAPI expansion exposing dungeon stats for scoreboards, tab lists and
 * holograms.
 *
 * <p>Placeholders (all prefixed {@code %ultimatedungeon_…%}):</p>
 * <ul>
 *   <li>{@code clears} — dungeons completed</li>
 *   <li>{@code bosses} — bosses defeated</li>
 *   <li>{@code kills} — monsters killed</li>
 *   <li>{@code deaths} — deaths</li>
 *   <li>{@code secrets} — secrets found</li>
 *   <li>{@code besttime} — fastest run (mm:ss, or "—")</li>
 * </ul>
 *
 * <p>PlaceholderAPI resolves placeholders on the main thread, so this expansion
 * serves values from an async-refreshed cache rather than touching the database
 * inline. The cache is refreshed on request (throttled) off the main thread.</p>
 */
public final class UltimateDungeonExpansion extends PlaceholderExpansion {

    private static final long REFRESH_INTERVAL_MS = 15_000L;

    private final UltimateDungeon plugin;
    private final StatisticsService statistics;
    private final Map<UUID, IPlayerStatsDao.PlayerStats> cache = new ConcurrentHashMap<>();
    private final Map<UUID, Long> lastRefresh = new ConcurrentHashMap<>();

    public UltimateDungeonExpansion(@NotNull final UltimateDungeon plugin,
                                    @NotNull final StatisticsService statistics) {
        this.plugin = plugin;
        this.statistics = statistics;
    }

    @Override @NotNull public String getIdentifier() { return "ultimatedungeon"; }
    @Override @NotNull public String getAuthor() { return "UltimateDungeon"; }
    @Override @NotNull public String getVersion() { return plugin.getDescription().getVersion(); }
    @Override public boolean persist() { return true; }

    @Override
    @Nullable
    public String onRequest(@Nullable final OfflinePlayer player, @NotNull final String params) {
        if (player == null) return "";
        refreshIfStale(player.getUniqueId());
        final IPlayerStatsDao.PlayerStats stats = cache.get(player.getUniqueId());
        if (stats == null) return "…"; // first lookup; value ready on next refresh

        return switch (params.toLowerCase()) {
            case "clears"   -> String.valueOf(stats.dungeonsCompleted());
            case "bosses"   -> String.valueOf(stats.bossesDefeated());
            case "kills"    -> String.valueOf(stats.monstersKilled());
            case "deaths"   -> String.valueOf(stats.deathCount());
            case "secrets"  -> String.valueOf(stats.secretsFound());
            case "besttime" -> stats.fastestRunMs() > 0 ? formatTime(stats.fastestRunMs()) : "—";
            default -> null;
        };
    }

    private void refreshIfStale(@NotNull final UUID uuid) {
        final long now = System.currentTimeMillis();
        final Long last = lastRefresh.get(uuid);
        if (last != null && now - last < REFRESH_INTERVAL_MS) return;
        lastRefresh.put(uuid, now);
        statistics.loadStats(uuid).thenAccept(stats -> {
            if (stats != null) cache.put(uuid, stats);
        });
    }

    private String formatTime(final long ms) {
        final long totalSeconds = ms / 1000;
        return String.format("%d:%02d", totalSeconds / 60, totalSeconds % 60);
    }
}

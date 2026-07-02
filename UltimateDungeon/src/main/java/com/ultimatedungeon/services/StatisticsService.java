package com.ultimatedungeon.services;

import com.ultimatedungeon.core.PluginLogger;
import com.ultimatedungeon.core.PluginScheduler;
import com.ultimatedungeon.database.DatabaseManager;
import com.ultimatedungeon.database.dao.IDungeonRecordDao;
import com.ultimatedungeon.database.dao.IPlayerStatsDao;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Records and retrieves per-player dungeon statistics.
 *
 * <p>All database I/O runs off the main thread via {@link PluginScheduler}.
 * Read methods return {@link CompletableFuture}s so callers never block the
 * server tick waiting on the database.</p>
 */
public final class StatisticsService {

    private final DatabaseManager database;
    private final PluginScheduler scheduler;
    private final PluginLogger logger;

    public StatisticsService(@NotNull final DatabaseManager database,
                             @NotNull final PluginScheduler scheduler,
                             @NotNull final PluginLogger logger) {
        this.database = database;
        this.scheduler = scheduler;
        this.logger = logger;
    }

    /** Inserts a stats row on first join if one does not yet exist. */
    public void ensurePlayer(@NotNull final Player player) {
        final UUID uuid = player.getUniqueId();
        final String name = player.getName();
        scheduler.runAsync(() -> {
            try {
                final IPlayerStatsDao dao = database.getPlayerStatsDao();
                if (dao.findByUuid(uuid) == null) {
                    dao.insert(uuid, name);
                } else {
                    dao.updatePlayerName(uuid, name);
                }
            } catch (final SQLException e) {
                logger.severe("Failed to ensure player stats row for " + name, e);
            }
        });
    }

    /** Atomically increments a counter column off-thread. */
    public void increment(@NotNull final UUID uuid, @NotNull final String column, final int amount) {
        scheduler.runAsync(() -> {
            try {
                database.getPlayerStatsDao().increment(uuid, column, amount);
            } catch (final SQLException e) {
                logger.severe("Failed to increment " + column + " for " + uuid, e);
            } catch (final IllegalArgumentException badColumn) {
                logger.warning("Rejected unknown stats column: " + column);
            }
        });
    }

    /** Records the start of a run and hands the generated record id to {@code idConsumer} async. */
    public void recordRunStart(@NotNull final UUID uuid, @NotNull final String theme,
                               @NotNull final String difficulty, final int partySize,
                               @NotNull final java.util.function.LongConsumer idConsumer) {
        scheduler.runAsync(() -> {
            try {
                final long id = database.getDungeonRecordDao().insert(uuid, theme, difficulty, partySize);
                idConsumer.accept(id);
            } catch (final SQLException e) {
                logger.severe("Failed to record run start for " + uuid, e);
            }
        });
    }

    /** Marks a run completed, updates fastest-run and increments the completion counter. */
    public void recordCompletion(@NotNull final UUID uuid, final long recordId, final long durationMs,
                                 final String bossKilled, @NotNull final String difficulty) {
        scheduler.runAsync(() -> {
            try {
                final IDungeonRecordDao records = database.getDungeonRecordDao();
                final IPlayerStatsDao stats = database.getPlayerStatsDao();
                if (recordId > 0) records.markCompleted(recordId, durationMs, bossKilled);
                stats.increment(uuid, "dungeons_completed", 1);
                stats.updateFastestRun(uuid, durationMs);
                stats.updateHighestDifficulty(uuid, difficulty);
            } catch (final SQLException e) {
                logger.severe("Failed to record completion for " + uuid, e);
            }
        });
    }

    /** Loads a leaderboard asynchronously; completes with an empty list on error. */
    @NotNull
    public CompletableFuture<java.util.List<IPlayerStatsDao.TopEntry>> loadTop(
            @NotNull final String column, final int limit) {
        final CompletableFuture<java.util.List<IPlayerStatsDao.TopEntry>> future = new CompletableFuture<>();
        scheduler.runAsync(() -> {
            try {
                future.complete(database.getPlayerStatsDao().topPlayers(column, limit));
            } catch (final SQLException | IllegalArgumentException e) {
                logger.severe("Failed to load leaderboard for " + column, e);
                future.complete(java.util.List.of());
            }
        });
        return future;
    }

    /** Loads a player's stats snapshot asynchronously. */
    @NotNull
    public CompletableFuture<IPlayerStatsDao.PlayerStats> loadStats(@NotNull final UUID uuid) {
        final CompletableFuture<IPlayerStatsDao.PlayerStats> future = new CompletableFuture<>();
        scheduler.runAsync(() -> {
            try {
                future.complete(database.getPlayerStatsDao().findByUuid(uuid));
            } catch (final SQLException e) {
                logger.severe("Failed to load stats for " + uuid, e);
                future.complete(null);
            }
        });
        return future;
    }
}

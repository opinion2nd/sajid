package com.ultimatedungeon.services;

import com.ultimatedungeon.config.files.ScoreboardsConfig;
import com.ultimatedungeon.dungeon.instance.DungeonInstance;
import com.ultimatedungeon.dungeon.instance.DungeonInstanceManager;
import com.ultimatedungeon.monster.engine.WaveManager;
import com.ultimatedungeon.room.model.RoomData;
import com.ultimatedungeon.room.model.RoomGraph;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Per-player sidebar scoreboard shown ONLY inside a dungeon.
 *
 * <p>Players outside a dungeon never receive it; when a run ends (complete,
 * fail, leave, death, disconnect, cleanup) the player's previous main
 * scoreboard is restored. Lines and placeholders come from scoreboards.yml
 * and update live every configured interval.</p>
 *
 * <p>Placeholders: %level% %theme% %rooms_cleared% %total_rooms%
 * %bosses_defeated% %total_bosses% %current_wave% %total_waves%
 * %completed_waves% %mobs_left% %alive_party% %time% %objective%</p>
 */
public final class DungeonScoreboardManager {

    private final ScoreboardsConfig config;
    private final DungeonInstanceManager instances;
    private final WaveManager waves;

    /** Players currently shown a dungeon scoreboard. */
    private final Set<UUID> shown = ConcurrentHashMap.newKeySet();
    private final Map<UUID, Scoreboard> previous = new ConcurrentHashMap<>();

    public DungeonScoreboardManager(@NotNull final ScoreboardsConfig config,
                                    @NotNull final DungeonInstanceManager instances,
                                    @NotNull final WaveManager waves) {
        this.config = config;
        this.instances = instances;
        this.waves = waves;
    }

    /** One tick of the scoreboard system: attach, refresh, or restore per player. */
    public void updateAll() {
        if (!config.isEnabled()) return;
        for (final Player player : Bukkit.getOnlinePlayers()) {
            final var raw = instances.getInstanceForPlayer(player);
            if (raw instanceof final DungeonInstance instance && instance.isActive()) {
                render(player, instance);
            } else if (shown.contains(player.getUniqueId())) {
                restore(player);
            }
        }
    }

    /** Removes the dungeon scoreboard and restores the player's previous one. */
    public void restore(@NotNull final Player player) {
        shown.remove(player.getUniqueId());
        final Scoreboard old = previous.remove(player.getUniqueId());
        final ScoreboardManager manager = Bukkit.getScoreboardManager();
        if (manager == null) return;
        player.setScoreboard(old != null ? old : manager.getMainScoreboard());
    }

    /** Restores every tracked player — called on plugin shutdown. */
    public void restoreAll() {
        for (final UUID id : Set.copyOf(shown)) {
            final Player p = Bukkit.getPlayer(id);
            if (p != null) restore(p);
        }
    }

    // ── Internal ──────────────────────────────────────────────────────────────

    private void render(@NotNull final Player player, @NotNull final DungeonInstance instance) {
        final ScoreboardManager manager = Bukkit.getScoreboardManager();
        if (manager == null) return;

        if (shown.add(player.getUniqueId())) {
            previous.put(player.getUniqueId(), player.getScoreboard());
        }

        final Scoreboard board = manager.getNewScoreboard();
        final Objective obj = board.registerNewObjective(
                "ud_dungeon", org.bukkit.scoreboard.Criteria.DUMMY,
                ChatColor.translateAlternateColorCodes('&', config.getTitle()));
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);

        int score = config.getLines().size();
        for (final String line : config.getLines()) {
            String text = ChatColor.translateAlternateColorCodes('&',
                    resolvePlaceholders(line, instance));
            if (text.length() > 40) text = text.substring(0, 40);
            // Sidebar entries must be unique — pad duplicates invisibly.
            while (board.getEntries().contains(text)) text += ChatColor.RESET;
            obj.getScore(text).setScore(score--);
        }
        player.setScoreboard(board);
    }

    @NotNull
    private String resolvePlaceholders(@NotNull final String line,
                                       @NotNull final DungeonInstance instance) {
        final UUID id = instance.getInstanceId();
        final RoomGraph graph = instance.getRoomGraph();
        int cleared = 0;
        int total = 0;
        if (graph != null) {
            total = graph.getRoomCount();
            for (final RoomData room : graph.getRooms()) {
                if (room.isCleared()) cleared++;
            }
        }
        int alive = 0;
        for (final Player p : Bukkit.getOnlinePlayers()) {
            if (instances.getInstanceForPlayer(p) == instance && !p.isDead()) alive++;
        }
        final long elapsed = (System.currentTimeMillis() - instance.getStartedAtMillis()) / 1000L;
        final String time = String.format("%02d:%02d", elapsed / 60, elapsed % 60);
        final String objective = instance.allBossesDefeated()
                ? "Claim your reward!"
                : "Defeat " + (instance.getTotalBosses() - instance.getBossesDefeated()) + " boss(es)";

        return line
                .replace("%level%", instance.getContext().getRequest().getDifficultyId()
                        .replace("level_", ""))
                .replace("%theme%", instance.getTheme() != null
                        ? instance.getTheme().getDisplayName() : "?")
                .replace("%rooms_cleared%", String.valueOf(cleared))
                .replace("%total_rooms%", String.valueOf(total))
                .replace("%bosses_defeated%", String.valueOf(instance.getBossesDefeated()))
                .replace("%total_bosses%", String.valueOf(instance.getTotalBosses()))
                .replace("%current_wave%", String.valueOf(waves.getCurrentWave(id)))
                .replace("%total_waves%", String.valueOf(waves.getTotalWaves(id)))
                .replace("%completed_waves%", String.valueOf(waves.getCompletedWaves(id)))
                .replace("%mobs_left%", String.valueOf(waves.getMobsLeft(id)))
                .replace("%alive_party%", String.valueOf(alive))
                .replace("%time%", time)
                .replace("%objective%", objective);
    }
}

package com.geodash.stats;

import com.geodash.GeoDashPlugin;
import com.geodash.util.CompatScheduler;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class StatsManager {

    public static class LevelStats {
        public int attempts;
        public int completions;
        public double bestPercent;
        public long bestTimeMs = -1;
    }

    public static class PlayerStats {
        public String name;
        public final Map<String, LevelStats> levels = new ConcurrentHashMap<>();

        public LevelStats level(String levelName) {
            return levels.computeIfAbsent(levelName.toLowerCase(Locale.ROOT), k -> new LevelStats());
        }
    }

    public record TopEntry(String playerName, LevelStats stats) {
    }

    private final GeoDashPlugin plugin;
    private final File file;
    private final Map<UUID, PlayerStats> stats = new ConcurrentHashMap<>();
    private final AtomicBoolean dirty = new AtomicBoolean(false);
    private CompatScheduler.Task saveTask;

    public StatsManager(GeoDashPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "stats.yml");
    }

    public void load() {
        stats.clear();
        if (file.exists()) {
            YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
            ConfigurationSection root = yaml.getConfigurationSection("players");
            if (root != null) {
                for (String key : root.getKeys(false)) {
                    ConfigurationSection ps = root.getConfigurationSection(key);
                    if (ps == null) {
                        continue;
                    }
                    PlayerStats player = new PlayerStats();
                    player.name = ps.getString("name", "?");
                    ConfigurationSection levels = ps.getConfigurationSection("levels");
                    if (levels != null) {
                        for (String levelName : levels.getKeys(false)) {
                            ConfigurationSection ls = levels.getConfigurationSection(levelName);
                            if (ls == null) {
                                continue;
                            }
                            LevelStats level = new LevelStats();
                            level.attempts = ls.getInt("attempts");
                            level.completions = ls.getInt("completions");
                            level.bestPercent = ls.getDouble("best-percent");
                            level.bestTimeMs = ls.getLong("best-time-ms", -1);
                            player.levels.put(levelName, level);
                        }
                    }
                    stats.put(UUID.fromString(key), player);
                }
            }
        }
        // Periodic async flush so a crash loses at most ~60s of stats
        saveTask = CompatScheduler.runGlobalTimer(plugin,
                () -> CompatScheduler.runAsync(plugin, this::saveIfDirty), 20L * 60, 20L * 60);
    }

    public void shutdown() {
        if (saveTask != null) {
            saveTask.cancel();
        }
        save();
    }

    public PlayerStats of(Player player) {
        PlayerStats ps = stats.computeIfAbsent(player.getUniqueId(), k -> new PlayerStats());
        ps.name = player.getName();
        return ps;
    }

    public void markDirty() {
        dirty.set(true);
    }

    private void saveIfDirty() {
        if (dirty.compareAndSet(true, false)) {
            save();
        }
    }

    public synchronized void save() {
        YamlConfiguration yaml = new YamlConfiguration();
        ConfigurationSection root = yaml.createSection("players");
        for (Map.Entry<UUID, PlayerStats> entry : stats.entrySet()) {
            ConfigurationSection ps = root.createSection(entry.getKey().toString());
            ps.set("name", entry.getValue().name);
            ConfigurationSection levels = ps.createSection("levels");
            for (Map.Entry<String, LevelStats> le : entry.getValue().levels.entrySet()) {
                ConfigurationSection ls = levels.createSection(le.getKey());
                ls.set("attempts", le.getValue().attempts);
                ls.set("completions", le.getValue().completions);
                ls.set("best-percent", le.getValue().bestPercent);
                ls.set("best-time-ms", le.getValue().bestTimeMs);
            }
        }
        try {
            yaml.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save stats.yml: " + e.getMessage());
        }
    }

    /**
     * Top players for a level: finishers first (fastest time), then best progress %.
     */
    public List<TopEntry> top(String levelName, int limit) {
        String key = levelName.toLowerCase(Locale.ROOT);
        List<TopEntry> entries = new ArrayList<>();
        for (PlayerStats player : stats.values()) {
            LevelStats level = player.levels.get(key);
            if (level != null && (level.attempts > 0 || level.completions > 0)) {
                entries.add(new TopEntry(player.name, level));
            }
        }
        entries.sort(Comparator
                .comparingInt((TopEntry e) -> e.stats().completions > 0 ? 0 : 1)
                .thenComparingLong(e -> e.stats().completions > 0 ? e.stats().bestTimeMs : 0)
                .thenComparing(e -> -e.stats().bestPercent));
        return entries.subList(0, Math.min(limit, entries.size()));
    }
}

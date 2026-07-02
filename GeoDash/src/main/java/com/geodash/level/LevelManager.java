package com.geodash.level;

import com.geodash.GeoDashPlugin;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LevelManager {

    private final GeoDashPlugin plugin;
    private final Map<String, Level> levels = new ConcurrentHashMap<>();
    private final File file;

    public LevelManager(GeoDashPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "levels.yml");
    }

    public void load() {
        levels.clear();
        if (!file.exists()) {
            return;
        }
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection root = yaml.getConfigurationSection("levels");
        if (root == null) {
            return;
        }
        for (String name : root.getKeys(false)) {
            ConfigurationSection sec = root.getConfigurationSection(name);
            if (sec != null) {
                levels.put(name.toLowerCase(Locale.ROOT), Level.load(name, sec));
            }
        }
        plugin.getLogger().info("Loaded " + levels.size() + " level(s)");
    }

    public void save() {
        YamlConfiguration yaml = new YamlConfiguration();
        ConfigurationSection root = yaml.createSection("levels");
        for (Level level : levels.values()) {
            level.save(root.createSection(level.getName()));
        }
        try {
            yaml.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save levels.yml: " + e.getMessage());
        }
    }

    public Level get(String name) {
        return levels.get(name.toLowerCase(Locale.ROOT));
    }

    public Level create(String name) {
        Level level = new Level(name);
        levels.put(name.toLowerCase(Locale.ROOT), level);
        return level;
    }

    public void delete(String name) {
        levels.remove(name.toLowerCase(Locale.ROOT));
        save();
    }

    public Collection<Level> all() {
        return levels.values();
    }
}

package com.ultimatedungeon.config.files;

import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/** Typed wrapper for {@code scoreboards.yml} — the in-dungeon sidebar. */
public final class ScoreboardsConfig {

    private final boolean enabled;
    private final String title;
    private final int updateIntervalTicks;
    private final List<String> lines;

    public ScoreboardsConfig(@NotNull final FileConfiguration cfg) {
        this.enabled = cfg.getBoolean("enabled", true);
        this.title = cfg.getString("title", "&6&lUltimateDungeon");
        this.updateIntervalTicks = Math.max(5, cfg.getInt("update-interval-ticks", 20));
        this.lines = cfg.getStringList("lines");
    }

    public boolean isEnabled()             { return enabled; }
    @NotNull public String getTitle()      { return title; }
    public int getUpdateIntervalTicks()    { return updateIntervalTicks; }
    @NotNull public List<String> getLines(){ return lines; }
}

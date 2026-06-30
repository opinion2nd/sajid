package com.ultimatedungeon.config.files;

import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

/**
 * Typed wrapper for {@code config.yml}.
 *
 * <p>All values are read once on load and cached as primitives / strings so
 * callers pay no YAML lookup cost at runtime.</p>
 */
public final class MainConfig {

    private final boolean debug;
    private final String  language;
    private final boolean updateChecker;
    private final String  instancePrefix;
    private final int     configVersion;

    public MainConfig(@NotNull final FileConfiguration config) {
        this.configVersion  = config.getInt("config-version", 1);
        this.debug          = config.getBoolean("debug", false);
        this.language       = config.getString("language", "en");
        this.updateChecker  = config.getBoolean("update-checker", true);
        this.instancePrefix = config.getString("instance-prefix", "ud_instance_");
    }

    public int     getConfigVersion()  { return configVersion; }
    public boolean isDebug()           { return debug; }
    @NotNull public String getLanguage()       { return language; }
    public boolean isUpdateChecker()   { return updateChecker; }
    @NotNull public String getInstancePrefix() { return instancePrefix; }
}

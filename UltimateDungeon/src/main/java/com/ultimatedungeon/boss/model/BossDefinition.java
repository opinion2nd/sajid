package com.ultimatedungeon.boss.model;

import org.bukkit.Material;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Immutable boss definition parsed from a {@code bosses.yml} section.
 *
 * <p>Holds every tunable a boss needs — display name, health, arena lock
 * material, countdown, BossBar styling, ordered phases, ability configs,
 * reward bundle and dialogue lines — so no gameplay value is hard-coded.</p>
 */
public final class BossDefinition {

    /** A single configurable ability slot for a boss. */
    public record AbilitySpec(@NotNull String id, double damage, long cooldownTicks, double range) {}

    private final String id;
    private final String displayName;
    private final double maxHealth;
    private final Material arenaLockBlock;
    private final int countdownSeconds;
    private final BarColor barColor;
    private final BarStyle barStyle;
    private final List<BossPhaseData> phases;
    private final List<AbilitySpec> abilities;
    private final double rewardMoney;
    private final int rewardExperience;
    private final String lootTable;
    private final boolean dialogueEnabled;
    private final Map<String, String> dialogue;

    private BossDefinition(final String id, final String displayName, final double maxHealth,
                           final Material arenaLockBlock, final int countdownSeconds,
                           final BarColor barColor, final BarStyle barStyle,
                           final List<BossPhaseData> phases, final List<AbilitySpec> abilities,
                           final double rewardMoney, final int rewardExperience, final String lootTable,
                           final boolean dialogueEnabled, final Map<String, String> dialogue) {
        this.id = id;
        this.displayName = displayName;
        this.maxHealth = maxHealth;
        this.arenaLockBlock = arenaLockBlock;
        this.countdownSeconds = countdownSeconds;
        this.barColor = barColor;
        this.barStyle = barStyle;
        this.phases = phases;
        this.abilities = abilities;
        this.rewardMoney = rewardMoney;
        this.rewardExperience = rewardExperience;
        this.lootTable = lootTable;
        this.dialogueEnabled = dialogueEnabled;
        this.dialogue = dialogue;
    }

    /**
     * Builds a definition from a boss section. Unknown/invalid enum values fall
     * back to safe defaults so a malformed config never crashes startup.
     */
    @NotNull
    public static BossDefinition fromSection(@NotNull final String id, @NotNull final ConfigurationSection s) {
        final Material lock = parseMaterial(s.getString("arena-lock-block", "OBSIDIAN"), Material.OBSIDIAN);

        final ConfigurationSection barSec = s.getConfigurationSection("bossbar");
        final BarColor color = parseEnum(BarColor.class, barSec != null ? barSec.getString("color") : null, BarColor.RED);
        final BarStyle style = parseEnum(BarStyle.class, barSec != null ? barSec.getString("style") : null, BarStyle.SOLID);

        final List<BossPhaseData> phases = new ArrayList<>();
        final List<Map<?, ?>> phaseMaps = s.getMapList("phases");
        for (final Map<?, ?> m : phaseMaps) {
            final Object pid = m.get("phase-id");
            final Object th = m.get("threshold");
            phases.add(new BossPhaseData(
                    pid != null ? pid.toString() : "phase",
                    th instanceof Number n ? n.doubleValue() : 1.0));
        }
        if (phases.isEmpty()) phases.add(new BossPhaseData("phase_one", 1.0));

        final List<AbilitySpec> abilities = new ArrayList<>();
        final ConfigurationSection abSec = s.getConfigurationSection("abilities");
        if (abSec != null) {
            for (final String key : abSec.getKeys(false)) {
                final ConfigurationSection a = abSec.getConfigurationSection(key);
                if (a == null) continue;
                abilities.add(new AbilitySpec(key,
                        a.getDouble("damage", 4.0),
                        a.getLong("cooldown-ticks", 60L),
                        a.getDouble("range", 6.0)));
            }
        }

        final ConfigurationSection rw = s.getConfigurationSection("rewards");
        final double money = rw != null ? rw.getDouble("money", 0) : 0;
        final int exp = rw != null ? rw.getInt("experience", 0) : 0;
        final String loot = rw != null ? rw.getString("loot-table", id + "_loot") : id + "_loot";

        final ConfigurationSection dlg = s.getConfigurationSection("dialogue");
        final boolean dlgOn = dlg != null && dlg.getBoolean("enabled", false);
        final Map<String, String> lines = new LinkedHashMap<>();
        if (dlg != null) {
            for (final String key : dlg.getKeys(false)) {
                if (key.equals("enabled")) continue;
                lines.put(key, dlg.getString(key, ""));
            }
        }

        return new BossDefinition(id,
                s.getString("display-name", id),
                s.getDouble("health", 500.0),
                lock, s.getInt("countdown-seconds", 10),
                color, style, phases, abilities,
                money, exp, loot, dlgOn, lines);
    }

    private static Material parseMaterial(final String raw, final Material def) {
        if (raw == null) return def;
        final Material m = Material.matchMaterial(raw.toUpperCase());
        return m != null ? m : def;
    }

    private static <E extends Enum<E>> E parseEnum(final Class<E> type, final String raw, final E def) {
        if (raw == null) return def;
        try {
            return Enum.valueOf(type, raw.toUpperCase());
        } catch (final IllegalArgumentException ex) {
            return def;
        }
    }

    @NotNull public String getId() { return id; }
    @NotNull public String getDisplayName() { return displayName; }
    public double getMaxHealth() { return maxHealth; }
    @NotNull public Material getArenaLockBlock() { return arenaLockBlock; }
    public int getCountdownSeconds() { return countdownSeconds; }
    @NotNull public BarColor getBarColor() { return barColor; }
    @NotNull public BarStyle getBarStyle() { return barStyle; }
    @NotNull public List<BossPhaseData> getPhases() { return phases; }
    @NotNull public List<AbilitySpec> getAbilities() { return abilities; }
    public double getRewardMoney() { return rewardMoney; }
    public int getRewardExperience() { return rewardExperience; }
    @NotNull public String getLootTable() { return lootTable; }
    public boolean isDialogueEnabled() { return dialogueEnabled; }
    @NotNull public String getDialogue(@NotNull final String key) { return dialogue.getOrDefault(key, ""); }
    public boolean hasDialogue(@NotNull final String key) { return dialogue.containsKey(key); }
}

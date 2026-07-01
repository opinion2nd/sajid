package com.ultimatedungeon.loot.model;

import com.ultimatedungeon.api.loot.ILootEntry;
import com.ultimatedungeon.api.loot.ILootTable;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A named loot table loaded from {@code loot.yml}.
 *
 * <p>Defines how many rolls to perform ({@code min}–{@code max}) and the pool
 * of weighted {@link LootEntry} candidates each roll draws from.</p>
 */
public final class LootTable implements ILootTable {

    private final String tableId;
    private final int minRolls;
    private final int maxRolls;
    private final List<LootEntry> entries;

    public LootTable(@NotNull final String tableId, final int minRolls, final int maxRolls,
                     @NotNull final List<LootEntry> entries) {
        this.tableId = tableId;
        this.minRolls = Math.max(0, minRolls);
        this.maxRolls = Math.max(this.minRolls, maxRolls);
        this.entries = entries;
    }

    /** Parses a table section, using the global rarity-chance map for entry chances. */
    @NotNull
    public static LootTable fromSection(@NotNull final String id, @NotNull final ConfigurationSection s,
                                        @NotNull final Map<LootRarity, Double> rarityChances) {
        final ConfigurationSection rolls = s.getConfigurationSection("rolls");
        final int min = rolls != null ? rolls.getInt("min", 1) : 1;
        final int max = rolls != null ? rolls.getInt("max", 1) : 1;
        final List<LootEntry> entries = new ArrayList<>();
        for (final Map<?, ?> m : s.getMapList("entries")) {
            entries.add(LootEntry.fromMap(m, rarityChances));
        }
        return new LootTable(id, min, max, entries);
    }

    @Override @NotNull public String getTableId() { return tableId; }

    @Override @NotNull
    public List<ILootEntry> getEntries() { return new ArrayList<>(entries); }

    @NotNull public List<LootEntry> getLootEntries() { return entries; }
    public int getMinRolls() { return minRolls; }
    public int getMaxRolls() { return maxRolls; }
}

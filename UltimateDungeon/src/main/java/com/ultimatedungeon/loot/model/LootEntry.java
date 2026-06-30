package com.ultimatedungeon.loot.model;

import com.ultimatedungeon.api.loot.ILootEntry;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * A single weighted entry inside a {@link LootTable}.
 *
 * <p>The {@code chance} is derived from the entry's rarity tier (looked up in
 * the table's rarity-chance map) and may be modified by difficulty.</p>
 */
public final class LootEntry implements ILootEntry {

    private final LootRarity rarity;
    private final Material item;
    private final int min;
    private final int max;
    private final double chance;

    public LootEntry(@NotNull final LootRarity rarity, @NotNull final Material item,
                     final int min, final int max, final double chance) {
        this.rarity = rarity;
        this.item = item;
        this.min = Math.max(1, min);
        this.max = Math.max(this.min, max);
        this.chance = chance;
    }

    /** Reads an entry from a {@code entries:} list element, resolving its rarity chance. */
    @NotNull
    public static LootEntry fromMap(@NotNull final Map<?, ?> m, @NotNull final Map<LootRarity, Double> rarityChances) {
        LootRarity rarity;
        try {
            rarity = LootRarity.valueOf(String.valueOf(m.getOrDefault("rarity", "COMMON")).toUpperCase());
        } catch (final IllegalArgumentException ex) {
            rarity = LootRarity.COMMON;
        }
        Material item = Material.matchMaterial(String.valueOf(m.getOrDefault("item", "STONE")).toUpperCase());
        if (item == null) item = Material.STONE;
        final int min = toInt(m.get("min"), 1);
        final int max = toInt(m.get("max"), 1);
        return new LootEntry(rarity, item, min, max, rarityChances.getOrDefault(rarity, 0.1));
    }

    private static int toInt(final Object o, final int def) {
        return o instanceof Number n ? n.intValue() : def;
    }

    @Override public double getChance() { return chance; }
    @Override @NotNull public String getRarity() { return rarity.name(); }

    @NotNull public LootRarity getRarityTier() { return rarity; }
    @NotNull public Material getItem() { return item; }
    public int getMin() { return min; }
    public int getMax() { return max; }
}

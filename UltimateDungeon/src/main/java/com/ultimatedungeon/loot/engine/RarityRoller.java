package com.ultimatedungeon.loot.engine;

import com.ultimatedungeon.core.PluginLogger;
import com.ultimatedungeon.loot.model.LootEntry;
import com.ultimatedungeon.util.WeightedRandomSelector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Executes weighted random rarity rolls.
 *
 * <p>Each candidate entry's drop chance becomes its selection weight, so a
 * single roll favours common items while still occasionally yielding rare
 * tiers. A {@code lootTierBonus} from difficulty nudges the odds toward higher
 * rarities by scaling rarer entries' weights up.</p>
 */
public final class RarityRoller {

    private final PluginLogger logger;

    public RarityRoller(@NotNull final PluginLogger logger) {
        this.logger = logger;
    }

    /**
     * Rolls a single entry from the pool, weighting by drop chance and applying
     * a difficulty tier bonus that boosts higher-rarity entries.
     *
     * @return the chosen entry, or {@code null} if the pool is empty
     */
    @Nullable
    public LootEntry roll(@NotNull final List<LootEntry> entries, final int lootTierBonus) {
        if (entries.isEmpty()) return null;
        final WeightedRandomSelector<LootEntry> selector = new WeightedRandomSelector<>();
        for (final LootEntry e : entries) {
            // Base weight from chance (scaled to an integer domain).
            int weight = (int) Math.max(1, Math.round(e.getChance() * 1000.0));
            // Higher tiers (ordinal) gain a multiplicative nudge per bonus level.
            if (lootTierBonus > 0) {
                weight += (int) Math.round(weight * (e.getRarityTier().ordinal() * 0.15 * lootTierBonus));
            }
            selector.add(e, Math.max(1, weight));
        }
        return selector.isEmpty() ? null : selector.select();
    }
}

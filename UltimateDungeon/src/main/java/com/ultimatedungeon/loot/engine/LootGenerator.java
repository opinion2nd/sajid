package com.ultimatedungeon.loot.engine;

import com.ultimatedungeon.api.loot.ILootGenerator;
import com.ultimatedungeon.api.loot.ILootTable;
import com.ultimatedungeon.core.PluginLogger;
import com.ultimatedungeon.loot.model.LootEntry;
import com.ultimatedungeon.loot.model.LootRarity;
import com.ultimatedungeon.loot.model.LootRoll;
import com.ultimatedungeon.loot.model.LootTable;
import com.ultimatedungeon.loot.registry.LootTableRegistry;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Evaluates loot tables into concrete item stacks.
 *
 * <p>Performs {@code minRolls}–{@code maxRolls} weighted draws per table via
 * {@link RarityRoller}, builds each resulting {@link ItemStack} with a rarity
 * label, and reports the highest rarity rolled for celebratory effects.</p>
 */
public final class LootGenerator implements ILootGenerator {

    private final LootTableRegistry registry;
    private final RarityRoller rarityRoller;
    private final PluginLogger logger;

    public LootGenerator(@NotNull final LootTableRegistry registry,
                         @NotNull final RarityRoller rarityRoller,
                         @NotNull final PluginLogger logger) {
        this.registry = registry;
        this.rarityRoller = rarityRoller;
        this.logger = logger;
    }

    @Override
    @NotNull
    public List<ItemStack> generate(@NotNull final ILootTable table) {
        return roll(table, 0).getItems();
    }

    @Override
    @NotNull
    public List<ItemStack> generate(@NotNull final String tableId) {
        final ILootTable table = registry.getTable(tableId);
        if (table == null) {
            logger.warning("Loot table not found: " + tableId);
            return List.of();
        }
        return generate(table);
    }

    /**
     * Full roll producing a {@link LootRoll} (items + highest rarity), honouring
     * a difficulty loot-tier bonus.
     */
    @NotNull
    public LootRoll roll(@NotNull final ILootTable table, final int lootTierBonus) {
        final List<ItemStack> items = new ArrayList<>();
        LootRarity highest = LootRarity.COMMON;

        if (!(table instanceof LootTable lt) || lt.getLootEntries().isEmpty()) {
            return new LootRoll(items, highest);
        }

        final int rolls = lt.getMinRolls() >= lt.getMaxRolls()
                ? lt.getMinRolls()
                : ThreadLocalRandom.current().nextInt(lt.getMinRolls(), lt.getMaxRolls() + 1);

        for (int i = 0; i < rolls; i++) {
            final LootEntry entry = rarityRoller.roll(lt.getLootEntries(), lootTierBonus);
            if (entry == null) continue;
            final int amount = entry.getMin() >= entry.getMax()
                    ? entry.getMin()
                    : ThreadLocalRandom.current().nextInt(entry.getMin(), entry.getMax() + 1);
            items.add(ItemBuilder.of(entry.getItem(), amount)
                    .rarity(entry.getRarityTier())
                    .glow(entry.getRarityTier().ordinal() >= LootRarity.EPIC.ordinal())
                    .build());
            if (entry.getRarityTier().ordinal() > highest.ordinal()) {
                highest = entry.getRarityTier();
            }
        }
        return new LootRoll(items, highest);
    }
}

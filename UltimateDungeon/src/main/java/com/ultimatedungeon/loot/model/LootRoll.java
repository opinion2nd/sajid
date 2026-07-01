package com.ultimatedungeon.loot.model;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The immutable result of evaluating a {@link LootTable} — the concrete set of
 * item stacks a single player won, together with the highest rarity rolled.
 */
public final class LootRoll {

    private final List<ItemStack> items;
    private final LootRarity highestRarity;

    public LootRoll(@NotNull final List<ItemStack> items, @NotNull final LootRarity highestRarity) {
        this.items = new ArrayList<>(items);
        this.highestRarity = highestRarity;
    }

    @NotNull public List<ItemStack> getItems() { return Collections.unmodifiableList(items); }
    @NotNull public LootRarity getHighestRarity() { return highestRarity; }
    public boolean isEmpty() { return items.isEmpty(); }
}

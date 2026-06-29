package com.ultimatedungeon.api.loot;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import java.util.List;

/** Contract for loot roll and item generation. */
public interface ILootGenerator {
    @NotNull List<ItemStack> generate(@NotNull ILootTable table);
    @NotNull List<ItemStack> generate(@NotNull String tableId);
}

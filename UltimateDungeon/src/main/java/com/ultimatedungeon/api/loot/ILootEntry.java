package com.ultimatedungeon.api.loot;

import org.jetbrains.annotations.NotNull;

/** Contract for a single entry in a loot table. */
public interface ILootEntry {
    double getChance();
    @NotNull String getRarity();
}

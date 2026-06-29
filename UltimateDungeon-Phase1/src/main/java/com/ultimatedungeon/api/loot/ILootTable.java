package com.ultimatedungeon.api.loot;

import org.jetbrains.annotations.NotNull;
import java.util.List;

/** Contract for a loot table definition. */
public interface ILootTable {
    @NotNull String getTableId();
    @NotNull List<ILootEntry> getEntries();
}

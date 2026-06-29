package com.ultimatedungeon.loot.registry;

import com.ultimatedungeon.api.loot.ILootTable;
import com.ultimatedungeon.core.PluginLogger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.*;

/** Holds all named loot table definitions loaded from loot.yml. */
public final class LootTableRegistry {

    private final PluginLogger logger;
    private final Map<String, ILootTable> tables = new LinkedHashMap<>();

    public LootTableRegistry(@NotNull final PluginLogger logger) {
        this.logger = logger;
    }

    public void register(@NotNull final ILootTable table) {
        tables.put(table.getTableId(), table);
        logger.debug("Registered loot table: " + table.getTableId());
    }

    @Nullable public ILootTable getTable(@NotNull final String tableId) { return tables.get(tableId); }
    @NotNull public Collection<ILootTable> getAllTables() { return Collections.unmodifiableCollection(tables.values()); }
}

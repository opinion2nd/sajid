package com.ultimatedungeon.gui.screens;

import com.ultimatedungeon.api.theme.ITheme;
import com.ultimatedungeon.gui.framework.AbstractGui;
import com.ultimatedungeon.gui.framework.GuiServices;
import com.ultimatedungeon.loot.engine.ItemBuilder;
import com.ultimatedungeon.util.MiniMessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/** Read-only overview of the available themes and difficulties. */
public final class DungeonInfoGui extends AbstractGui {

    private final GuiServices services;

    public DungeonInfoGui(@NotNull final Player viewer, @NotNull final GuiServices services) {
        super(viewer);
        this.services = services;
    }

    @Override
    public void open() {
        inventory = Bukkit.createInventory(null, 36, MiniMessageUtil.legacy("<dark_gray>Dungeon Info"));
        final ItemStack filler = ItemBuilder.of(Material.GRAY_STAINED_GLASS_PANE).name(" ").build();
        for (int i = 0; i < inventory.getSize(); i++) inventory.setItem(i, filler);
        int slot = 10;
        for (final ITheme theme : services.themeRegistry().getAllThemes()) {
            if (slot >= 17) break;
            inventory.setItem(slot++, ItemBuilder.of(Material.FILLED_MAP)
                    .name("<yellow>" + theme.getDisplayName())
                    .lore("<gray>Monsters: <white>" + theme.getMonsterPool().size())
                    .lore("<gray>Bosses: <white>" + theme.getBossPool().size()).build());
        }
        slot = 19;
        for (final String id : services.difficultyConfig().getPresetIds()) {
            if (slot >= 26) break;
            final var p = services.difficultyConfig().getPresetOrDefault(id);
            inventory.setItem(slot++, ItemBuilder.of(Material.REDSTONE)
                    .name("<gold>" + p.displayName())
                    .lore("<gray>Health x" + p.healthMultiplier())
                    .lore("<gray>Damage x" + p.damageMultiplier()).build());
        }
        services.guiManager().register(viewer, this);
        viewer.openInventory(inventory);
    }

    @Override public void refresh() { }
    @Override public void handleClick(final int slot) { }
}

package com.ultimatedungeon.gui.screens;

import com.ultimatedungeon.api.theme.ITheme;
import com.ultimatedungeon.gui.framework.AbstractGui;
import com.ultimatedungeon.gui.framework.GuiManager;
import com.ultimatedungeon.loot.engine.ItemBuilder;
import com.ultimatedungeon.theme.registry.ThemeRegistry;
import com.ultimatedungeon.util.MiniMessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Theme picker for solo dungeons. Each registered theme is shown as a clickable
 * icon that launches a solo run of that theme via the dungeon command.
 */
public final class SoloDungeonGui extends AbstractGui {

    private final GuiManager guiManager;
    private final List<ITheme> themes = new ArrayList<>();

    public SoloDungeonGui(@NotNull final Player viewer, @NotNull final GuiManager guiManager,
                          @NotNull final ThemeRegistry themeRegistry) {
        super(viewer);
        this.guiManager = guiManager;
        this.themes.addAll(themeRegistry.getAllThemes());
    }

    @Override
    public void open() {
        inventory = Bukkit.createInventory(null, 27, MiniMessageUtil.legacy("<dark_gray>Select a Theme"));
        final ItemStack filler = ItemBuilder.of(Material.BLACK_STAINED_GLASS_PANE).name(" ").build();
        for (int i = 0; i < inventory.getSize(); i++) inventory.setItem(i, filler);
        int slot = 10;
        for (final ITheme theme : themes) {
            if (slot >= 17) break;
            inventory.setItem(slot, ItemBuilder.of(themeIcon(slot))
                    .name("<yellow>" + theme.getDisplayName())
                    .lore("<gray>Click to start a solo run.").build());
            slot++;
        }
        guiManager.register(viewer, this);
        viewer.openInventory(inventory);
    }

    @Override public void refresh() { /* static list */ }

    @Override
    public void handleClick(final int slot) {
        final int index = slot - 10;
        if (index < 0 || index >= themes.size()) return;
        final ITheme theme = themes.get(index);
        viewer.closeInventory();
        viewer.performCommand("dungeon solo " + theme.getThemeId());
    }

    @NotNull
    private Material themeIcon(final int slot) {
        return switch (slot % 5) {
            case 0 -> Material.MOSSY_COBBLESTONE;
            case 1 -> Material.PACKED_ICE;
            case 2 -> Material.CRYING_OBSIDIAN;
            case 3 -> Material.MAGMA_BLOCK;
            default -> Material.BONE_BLOCK;
        };
    }
}

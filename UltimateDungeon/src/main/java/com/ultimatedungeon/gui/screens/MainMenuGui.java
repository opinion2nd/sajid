package com.ultimatedungeon.gui.screens;

import com.ultimatedungeon.gui.framework.AbstractGui;
import com.ultimatedungeon.gui.framework.GuiManager;
import com.ultimatedungeon.loot.engine.ItemBuilder;
import com.ultimatedungeon.theme.registry.ThemeRegistry;
import com.ultimatedungeon.util.MiniMessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Entry-point hub GUI. Buttons launch a solo dungeon picker, start a party run,
 * or show personal statistics — each delegating to the existing commands so the
 * GUI stays a thin, decoupled front end.
 */
public final class MainMenuGui extends AbstractGui {

    private static final int SLOT_SOLO = 11;
    private static final int SLOT_PARTY = 13;
    private static final int SLOT_STATS = 15;

    private final GuiManager guiManager;
    private final ThemeRegistry themeRegistry;

    public MainMenuGui(@NotNull final Player viewer, @NotNull final GuiManager guiManager,
                       @NotNull final ThemeRegistry themeRegistry) {
        super(viewer);
        this.guiManager = guiManager;
        this.themeRegistry = themeRegistry;
    }

    @Override
    public void open() {
        inventory = Bukkit.createInventory(null, 27, MiniMessageUtil.legacy("<dark_gray>UltimateDungeon"));
        final ItemStack filler = ItemBuilder.of(Material.GRAY_STAINED_GLASS_PANE).name(" ").build();
        for (int i = 0; i < inventory.getSize(); i++) inventory.setItem(i, filler);
        inventory.setItem(SLOT_SOLO, ItemBuilder.of(Material.IRON_SWORD)
                .name("<green><bold>Solo Dungeon").lore("<gray>Choose a theme and dive in alone.").build());
        inventory.setItem(SLOT_PARTY, ItemBuilder.of(Material.SHIELD)
                .name("<aqua><bold>Party Dungeon").lore("<gray>Start a run with your party.").build());
        inventory.setItem(SLOT_STATS, ItemBuilder.of(Material.BOOK)
                .name("<gold><bold>Statistics").lore("<gray>View your dungeon record.").build());
        guiManager.register(viewer, this);
        viewer.openInventory(inventory);
    }

    @Override public void refresh() { /* static menu */ }

    @Override
    public void handleClick(final int slot) {
        switch (slot) {
            case SLOT_SOLO -> {
                viewer.closeInventory();
                new SoloDungeonGui(viewer, guiManager, themeRegistry).open();
            }
            case SLOT_PARTY -> { viewer.closeInventory(); viewer.performCommand("dungeon party"); }
            case SLOT_STATS -> { viewer.closeInventory(); viewer.performCommand("dungeon stats"); }
            default -> { /* decorative slot */ }
        }
    }

    @NotNull
    public Inventory inventory() { return inventory; }
}

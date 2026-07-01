package com.ultimatedungeon.gui.screens;

import com.ultimatedungeon.gui.framework.AbstractGui;
import com.ultimatedungeon.gui.framework.GuiServices;
import com.ultimatedungeon.loot.engine.ItemBuilder;
import com.ultimatedungeon.util.MiniMessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Entry-point hub GUI. Opens the solo picker, party panel, stats, active-run
 * status, info and (for admins) the admin panel.
 */
public final class MainMenuGui extends AbstractGui {

    private static final int SOLO = 10, PARTY = 12, STATS = 14, ACTIVE = 16, INFO = 21, ADMIN = 23;

    private final GuiServices services;

    public MainMenuGui(@NotNull final Player viewer, @NotNull final GuiServices services) {
        super(viewer);
        this.services = services;
    }

    @Override
    public void open() {
        inventory = Bukkit.createInventory(null, 27, MiniMessageUtil.legacy("<dark_gray>UltimateDungeon"));
        final ItemStack filler = ItemBuilder.of(Material.GRAY_STAINED_GLASS_PANE).name(" ").build();
        for (int i = 0; i < inventory.getSize(); i++) inventory.setItem(i, filler);
        inventory.setItem(SOLO, ItemBuilder.of(Material.IRON_SWORD)
                .name("<green><bold>Solo Dungeon").lore("<gray>Pick a theme and dive in alone.").build());
        inventory.setItem(PARTY, ItemBuilder.of(Material.SHIELD)
                .name("<aqua><bold>Party Dungeon").lore("<gray>Manage and start a party run.").build());
        inventory.setItem(STATS, ItemBuilder.of(Material.BOOK)
                .name("<gold><bold>Statistics").lore("<gray>View your dungeon record.").build());
        inventory.setItem(ACTIVE, ItemBuilder.of(Material.COMPASS)
                .name("<yellow><bold>Active Run").lore("<gray>Your current dungeon status.").build());
        inventory.setItem(INFO, ItemBuilder.of(Material.PAINTING)
                .name("<white><bold>Dungeon Info").lore("<gray>Themes and difficulties.").build());
        if (viewer.hasPermission("dungeon.admin")) {
            inventory.setItem(ADMIN, ItemBuilder.of(Material.COMMAND_BLOCK)
                    .name("<red><bold>Admin Panel").lore("<gray>Administrative controls.").build());
        }
        services.guiManager().register(viewer, this);
        viewer.openInventory(inventory);
    }

    @Override public void refresh() { }

    @Override
    public void handleClick(final int slot) {
        switch (slot) {
            case SOLO -> { viewer.closeInventory(); new SoloDungeonGui(viewer, services).open(); }
            case PARTY -> { viewer.closeInventory(); new PartyDungeonGui(viewer, services).open(); }
            case STATS -> { viewer.closeInventory(); viewer.performCommand("dungeon stats"); }
            case ACTIVE -> { viewer.closeInventory(); new ActiveDungeonGui(viewer, services).open(); }
            case INFO -> { viewer.closeInventory(); new DungeonInfoGui(viewer, services).open(); }
            case ADMIN -> {
                if (viewer.hasPermission("dungeon.admin")) {
                    viewer.closeInventory();
                    new AdminPanelGui(viewer, services).open();
                }
            }
            default -> { }
        }
    }
}

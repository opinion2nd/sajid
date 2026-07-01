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

/** Administrative controls (reload, active-instance info). */
public final class AdminPanelGui extends AbstractGui {

    private static final int RELOAD = 11, INFO = 13, LEAVE_ALL = 15;
    private final GuiServices services;

    public AdminPanelGui(@NotNull final Player viewer, @NotNull final GuiServices services) {
        super(viewer);
        this.services = services;
    }

    @Override
    public void open() {
        inventory = Bukkit.createInventory(null, 27, MiniMessageUtil.legacy("<dark_gray>Admin Panel"));
        final ItemStack filler = ItemBuilder.of(Material.GRAY_STAINED_GLASS_PANE).name(" ").build();
        for (int i = 0; i < inventory.getSize(); i++) inventory.setItem(i, filler);
        inventory.setItem(RELOAD, ItemBuilder.of(Material.REPEATER)
                .name("<green>Reload Config").lore("<gray>Reload all config files.").build());
        inventory.setItem(INFO, ItemBuilder.of(Material.PAPER)
                .name("<yellow>Active Instances")
                .lore("<white>" + services.instanceManager().getActiveCount() + " running").build());
        inventory.setItem(LEAVE_ALL, ItemBuilder.of(Material.BARRIER)
                .name("<red>Leave Dungeon").lore("<gray>Force-leave your own run.").build());
        services.guiManager().register(viewer, this);
        viewer.openInventory(inventory);
    }

    @Override public void refresh() { }

    @Override
    public void handleClick(final int slot) {
        switch (slot) {
            case RELOAD -> { viewer.closeInventory(); viewer.performCommand("dungeon reload"); }
            case INFO -> { viewer.closeInventory(); viewer.performCommand("dungeon admin info"); }
            case LEAVE_ALL -> { viewer.closeInventory(); viewer.performCommand("dungeon leave"); }
            default -> { }
        }
    }
}

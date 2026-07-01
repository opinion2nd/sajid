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
 * Ready-check prompt shown to party members before a run starts. Each member
 * confirms or declines; the underlying tally lives in {@link
 * com.ultimatedungeon.party.manager.ReadyCheckManager}.
 */
public final class ReadyCheckGui extends AbstractGui {

    private static final int READY = 11, DECLINE = 15;
    private final GuiServices services;

    public ReadyCheckGui(@NotNull final Player viewer, @NotNull final GuiServices services) {
        super(viewer);
        this.services = services;
    }

    @Override
    public void open() {
        inventory = Bukkit.createInventory(null, 27, MiniMessageUtil.legacy("<dark_gray>Ready Check"));
        final ItemStack filler = ItemBuilder.of(Material.BLACK_STAINED_GLASS_PANE).name(" ").build();
        for (int i = 0; i < inventory.getSize(); i++) inventory.setItem(i, filler);
        inventory.setItem(13, ItemBuilder.of(Material.BELL)
                .name("<yellow><bold>Are you ready?")
                .lore("<gray>Confirm to start the dungeon.").build());
        inventory.setItem(READY, ItemBuilder.of(Material.LIME_WOOL)
                .name("<green><bold>Ready").build());
        inventory.setItem(DECLINE, ItemBuilder.of(Material.RED_WOOL)
                .name("<red><bold>Not Ready").build());
        services.guiManager().register(viewer, this);
        viewer.openInventory(inventory);
    }

    @Override public void refresh() { }

    @Override
    public void handleClick(final int slot) {
        switch (slot) {
            case READY -> {
                viewer.closeInventory();
                services.partyManager().respondToReadyCheck(viewer, true, p -> { });
                MiniMessageUtil.send(viewer, "<green>You marked yourself ready.");
            }
            case DECLINE -> {
                viewer.closeInventory();
                services.partyManager().respondToReadyCheck(viewer, false, p -> { });
                MiniMessageUtil.send(viewer, "<red>You declined the ready check.");
            }
            default -> { }
        }
    }
}

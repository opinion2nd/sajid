package com.ultimatedungeon.gui.screens;

import com.ultimatedungeon.api.dungeon.IDungeonInstance;
import com.ultimatedungeon.dungeon.instance.DungeonInstance;
import com.ultimatedungeon.gui.framework.AbstractGui;
import com.ultimatedungeon.gui.framework.GuiServices;
import com.ultimatedungeon.loot.engine.ItemBuilder;
import com.ultimatedungeon.util.MiniMessageUtil;
import com.ultimatedungeon.util.TimeUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/** Shows the player's current dungeon status, with a leave button. */
public final class ActiveDungeonGui extends AbstractGui {

    private static final int LEAVE = 15;
    private final GuiServices services;

    public ActiveDungeonGui(@NotNull final Player viewer, @NotNull final GuiServices services) {
        super(viewer);
        this.services = services;
    }

    @Override
    public void open() {
        inventory = Bukkit.createInventory(null, 27, MiniMessageUtil.legacy("<dark_gray>Active Run"));
        final ItemStack filler = ItemBuilder.of(Material.GRAY_STAINED_GLASS_PANE).name(" ").build();
        for (int i = 0; i < inventory.getSize(); i++) inventory.setItem(i, filler);

        final IDungeonInstance inst = services.instanceManager().getInstanceForPlayer(viewer);
        if (inst == null) {
            inventory.setItem(13, ItemBuilder.of(Material.BARRIER)
                    .name("<red>Not in a dungeon").lore("<gray>Start one from the main menu.").build());
        } else {
            String state = "ACTIVE";
            String elapsed = "";
            if (inst instanceof final DungeonInstance di) {
                state = di.getState().name();
                elapsed = TimeUtil.formatMmSs(di.getContext().getElapsedMs());
            }
            inventory.setItem(11, ItemBuilder.of(Material.COMPASS)
                    .name("<green>In Dungeon")
                    .lore("<gray>State: <white>" + state)
                    .lore("<gray>Time: <white>" + elapsed).build());
            inventory.setItem(LEAVE, ItemBuilder.of(Material.RED_BED)
                    .name("<red>Leave Dungeon").lore("<gray>Return to spawn.").build());
        }
        services.guiManager().register(viewer, this);
        viewer.openInventory(inventory);
    }

    @Override public void refresh() { }

    @Override
    public void handleClick(final int slot) {
        if (slot == LEAVE) { viewer.closeInventory(); viewer.performCommand("dungeon leave"); }
    }
}

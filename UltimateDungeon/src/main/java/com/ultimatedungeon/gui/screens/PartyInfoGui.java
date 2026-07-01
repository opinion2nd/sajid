package com.ultimatedungeon.gui.screens;

import com.ultimatedungeon.api.party.IParty;
import com.ultimatedungeon.gui.framework.AbstractGui;
import com.ultimatedungeon.gui.framework.GuiServices;
import com.ultimatedungeon.loot.engine.ItemBuilder;
import com.ultimatedungeon.util.MiniMessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/** Read-only summary of the viewer's party: leader, size and dungeon status. */
public final class PartyInfoGui extends AbstractGui {

    private static final int BACK = 22;
    private final GuiServices services;

    public PartyInfoGui(@NotNull final Player viewer, @NotNull final GuiServices services) {
        super(viewer);
        this.services = services;
    }

    @Override
    public void open() {
        inventory = Bukkit.createInventory(null, 27, MiniMessageUtil.legacy("<dark_gray>Party Info"));
        final ItemStack filler = ItemBuilder.of(Material.BLACK_STAINED_GLASS_PANE).name(" ").build();
        for (int i = 0; i < inventory.getSize(); i++) inventory.setItem(i, filler);

        final IParty party = services.partyManager().getPartyForPlayer(viewer);
        if (party == null) {
            inventory.setItem(13, ItemBuilder.of(Material.BARRIER)
                    .name("<red>You are not in a party").build());
        } else {
            final boolean inDungeon = party.getDungeonInstanceId() != null;
            inventory.setItem(13, ItemBuilder.of(Material.BOOK)
                    .name("<gold><bold>" + party.getLeader().getName() + "'s Party")
                    .lore("<gray>Leader: <white>" + party.getLeader().getName())
                    .lore("<gray>Members: <white>" + party.getSize())
                    .lore("<gray>Status: " + (inDungeon ? "<green>In dungeon" : "<yellow>Idle")).build());
        }
        inventory.setItem(BACK, ItemBuilder.of(Material.ARROW).name("<yellow>Back").build());
        services.guiManager().register(viewer, this);
        viewer.openInventory(inventory);
    }

    @Override public void refresh() { }

    @Override
    public void handleClick(final int slot) {
        if (slot == BACK) { viewer.closeInventory(); new PartyDungeonGui(viewer, services).open(); }
    }
}

package com.ultimatedungeon.gui.screens;

import com.ultimatedungeon.gui.framework.AbstractGui;
import com.ultimatedungeon.gui.framework.GuiServices;
import com.ultimatedungeon.loot.engine.ItemBuilder;
import com.ultimatedungeon.party.model.PartyInvitation;
import com.ultimatedungeon.util.MiniMessageUtil;
import com.ultimatedungeon.util.TimeUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Shows the viewer's pending party invitation with accept and deny buttons.
 * A player can hold at most one pending invitation at a time.
 */
public final class PartyInvitationsGui extends AbstractGui {

    private static final int ACCEPT = 11, DENY = 15, BACK = 22;

    private final GuiServices services;

    public PartyInvitationsGui(@NotNull final Player viewer, @NotNull final GuiServices services) {
        super(viewer);
        this.services = services;
    }

    @Override
    public void open() {
        inventory = Bukkit.createInventory(null, 27, MiniMessageUtil.legacy("<dark_gray>Party Invitations"));
        final ItemStack filler = ItemBuilder.of(Material.BLACK_STAINED_GLASS_PANE).name(" ").build();
        for (int i = 0; i < inventory.getSize(); i++) inventory.setItem(i, filler);

        final PartyInvitation invite = services.invitationManager().getPendingInvitation(viewer);
        if (invite == null) {
            inventory.setItem(13, ItemBuilder.of(Material.BARRIER)
                    .name("<gray>No pending invitations").build());
        } else {
            final long remainMs = Math.max(0L, invite.getExpiresAt() - System.currentTimeMillis());
            inventory.setItem(13, ItemBuilder.of(Material.PAPER)
                    .name("<yellow>Invite from " + invite.getInviter().getName())
                    .lore("<gray>Expires in <white>" + TimeUtil.formatMmSs(remainMs)).build());
            inventory.setItem(ACCEPT, ItemBuilder.of(Material.LIME_WOOL)
                    .name("<green><bold>Accept").build());
            inventory.setItem(DENY, ItemBuilder.of(Material.RED_WOOL)
                    .name("<red><bold>Deny").build());
        }
        inventory.setItem(BACK, ItemBuilder.of(Material.ARROW).name("<yellow>Back").build());
        services.guiManager().register(viewer, this);
        viewer.openInventory(inventory);
    }

    @Override public void refresh() { }

    @Override
    public void handleClick(final int slot) {
        switch (slot) {
            case ACCEPT -> { viewer.closeInventory(); viewer.performCommand("party accept"); }
            case DENY -> { viewer.closeInventory(); viewer.performCommand("party deny"); }
            case BACK -> { viewer.closeInventory(); new PartyDungeonGui(viewer, services).open(); }
            default -> { }
        }
    }
}

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

/**
 * Party hub screen. Adapts to whether the viewer is in a party: when solo it
 * offers party creation and pending invitations; when in a party it exposes
 * members, info and (for the leader) a theme picker that launches a party run.
 */
public final class PartyDungeonGui extends AbstractGui {

    private static final int CREATE = 13;
    private static final int MEMBERS = 10, INFO = 12, INVITES = 14, START = 16, LEAVE = 22;

    private final GuiServices services;

    public PartyDungeonGui(@NotNull final Player viewer, @NotNull final GuiServices services) {
        super(viewer);
        this.services = services;
    }

    @Override
    public void open() {
        inventory = Bukkit.createInventory(null, 27, MiniMessageUtil.legacy("<dark_gray>Party Dungeon"));
        final ItemStack filler = ItemBuilder.of(Material.BLACK_STAINED_GLASS_PANE).name(" ").build();
        for (int i = 0; i < inventory.getSize(); i++) inventory.setItem(i, filler);

        final IParty party = services.partyManager().getPartyForPlayer(viewer);
        if (party == null) {
            inventory.setItem(CREATE, ItemBuilder.of(Material.EMERALD)
                    .name("<green><bold>Create Party")
                    .lore("<gray>Start a party and invite friends.").build());
            if (services.invitationManager().hasPendingInvitation(viewer)) {
                inventory.setItem(INVITES, ItemBuilder.of(Material.PAPER)
                        .name("<yellow><bold>Pending Invitation")
                        .lore("<gray>You have an invite waiting.").build());
            }
        } else {
            final boolean leader = party.isLeader(viewer);
            inventory.setItem(MEMBERS, ItemBuilder.of(Material.PLAYER_HEAD)
                    .name("<aqua><bold>Members")
                    .lore("<gray>" + party.getSize() + " member(s).").build());
            inventory.setItem(INFO, ItemBuilder.of(Material.BOOK)
                    .name("<gold><bold>Party Info")
                    .lore("<gray>Leader: <white>" + party.getLeader().getName()).build());
            inventory.setItem(INVITES, ItemBuilder.of(Material.PAPER)
                    .name("<yellow><bold>Invitations")
                    .lore("<gray>Manage pending invites.").build());
            if (leader) {
                inventory.setItem(START, ItemBuilder.of(Material.NETHER_STAR)
                        .name("<light_purple><bold>Start Dungeon")
                        .lore("<gray>Pick a theme and launch the run.").build());
            }
            inventory.setItem(LEAVE, ItemBuilder.of(Material.BARRIER)
                    .name("<red>Leave Party")
                    .lore("<gray>Leave your current party.").build());
        }
        services.guiManager().register(viewer, this);
        viewer.openInventory(inventory);
    }

    @Override public void refresh() { }

    @Override
    public void handleClick(final int slot) {
        final IParty party = services.partyManager().getPartyForPlayer(viewer);
        if (party == null) {
            switch (slot) {
                case CREATE -> { viewer.closeInventory(); viewer.performCommand("party create"); }
                case INVITES -> { viewer.closeInventory(); new PartyInvitationsGui(viewer, services).open(); }
                default -> { }
            }
            return;
        }
        switch (slot) {
            case MEMBERS -> { viewer.closeInventory(); new PartyMembersGui(viewer, services).open(); }
            case INFO -> { viewer.closeInventory(); new PartyInfoGui(viewer, services).open(); }
            case INVITES -> { viewer.closeInventory(); new PartyInvitationsGui(viewer, services).open(); }
            case START -> {
                if (party.isLeader(viewer)) {
                    viewer.closeInventory();
                    new PartyThemeSelectGui(viewer, services).open();
                }
            }
            case LEAVE -> { viewer.closeInventory(); viewer.performCommand("party leave"); }
            default -> { }
        }
    }
}

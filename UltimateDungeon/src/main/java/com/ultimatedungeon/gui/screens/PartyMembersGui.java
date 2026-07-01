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

import java.util.ArrayList;
import java.util.List;

/**
 * Lists party members. The leader may click a member to kick them; the header
 * button returns to the party hub.
 */
public final class PartyMembersGui extends AbstractGui {

    private static final int FIRST_SLOT = 9;
    private static final int BACK = 0;

    private final GuiServices services;
    private final List<Player> members = new ArrayList<>();

    public PartyMembersGui(@NotNull final Player viewer, @NotNull final GuiServices services) {
        super(viewer);
        this.services = services;
    }

    @Override
    public void open() {
        inventory = Bukkit.createInventory(null, 27, MiniMessageUtil.legacy("<dark_gray>Party Members"));
        final ItemStack filler = ItemBuilder.of(Material.BLACK_STAINED_GLASS_PANE).name(" ").build();
        for (int i = 0; i < inventory.getSize(); i++) inventory.setItem(i, filler);
        inventory.setItem(BACK, ItemBuilder.of(Material.ARROW).name("<yellow>Back").build());

        members.clear();
        final IParty party = services.partyManager().getPartyForPlayer(viewer);
        if (party != null) {
            members.addAll(party.getMembers());
            final boolean canKick = party.isLeader(viewer);
            int slot = FIRST_SLOT;
            for (final Player member : members) {
                if (slot >= inventory.getSize()) break;
                final boolean isLeader = party.isLeader(member);
                final ItemBuilder b = ItemBuilder.of(Material.PLAYER_HEAD)
                        .name((isLeader ? "<gold>★ " : "<white>") + member.getName());
                if (isLeader) b.lore("<gray>Party Leader");
                if (canKick && !isLeader) b.lore("<red>Click to kick.");
                inventory.setItem(slot, b.build());
                slot++;
            }
        }
        services.guiManager().register(viewer, this);
        viewer.openInventory(inventory);
    }

    @Override public void refresh() { }

    @Override
    public void handleClick(final int slot) {
        if (slot == BACK) { viewer.closeInventory(); new PartyDungeonGui(viewer, services).open(); return; }
        final int index = slot - FIRST_SLOT;
        if (index < 0 || index >= members.size()) return;
        final IParty party = services.partyManager().getPartyForPlayer(viewer);
        if (party == null || !party.isLeader(viewer)) return;
        final Player target = members.get(index);
        if (party.isLeader(target)) return;
        viewer.closeInventory();
        viewer.performCommand("party kick " + target.getName());
    }
}

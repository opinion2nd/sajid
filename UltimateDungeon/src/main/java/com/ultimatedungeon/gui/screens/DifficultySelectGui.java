package com.ultimatedungeon.gui.screens;

import com.ultimatedungeon.config.files.DifficultyConfig;
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

/** Difficulty picker; picking a difficulty launches the solo run for the chosen theme. */
public final class DifficultySelectGui extends AbstractGui {

    private static final int FIRST_SLOT = 10;
    private final GuiServices services;
    private final String theme;
    private final String mode;
    private final List<String> difficulties = new ArrayList<>();

    public DifficultySelectGui(@NotNull final Player viewer, @NotNull final GuiServices services,
                               @NotNull final String theme) {
        this(viewer, services, theme, "solo");
    }

    /** @param mode {@code "solo"} or {@code "party"} — decides which command launches the run. */
    public DifficultySelectGui(@NotNull final Player viewer, @NotNull final GuiServices services,
                               @NotNull final String theme, @NotNull final String mode) {
        super(viewer);
        this.services = services;
        this.theme = theme;
        this.mode = mode;
        this.difficulties.addAll(services.difficultyConfig().getPresetIds());
    }

    @Override
    public void open() {
        inventory = Bukkit.createInventory(null, 27, MiniMessageUtil.legacy("<dark_gray>Select Difficulty"));
        final ItemStack filler = ItemBuilder.of(Material.BLACK_STAINED_GLASS_PANE).name(" ").build();
        for (int i = 0; i < inventory.getSize(); i++) inventory.setItem(i, filler);
        int slot = FIRST_SLOT;
        for (final String id : difficulties) {
            if (slot >= 17) break;
            final DifficultyConfig.DifficultyPreset p = services.difficultyConfig().getPresetOrDefault(id);
            inventory.setItem(slot, ItemBuilder.of(icon(slot))
                    .name("<gold>" + p.displayName())
                    .lore("<gray>Health x" + p.healthMultiplier())
                    .lore("<gray>Damage x" + p.damageMultiplier())
                    .lore("<yellow>Click to start!").build());
            slot++;
        }
        services.guiManager().register(viewer, this);
        viewer.openInventory(inventory);
    }

    @Override public void refresh() { }

    @Override
    public void handleClick(final int slot) {
        final int index = slot - FIRST_SLOT;
        if (index < 0 || index >= difficulties.size()) return;
        viewer.closeInventory();
        viewer.performCommand("dungeon " + mode + " " + theme + " " + difficulties.get(index));
    }

    @NotNull
    private Material icon(final int slot) {
        return switch (slot % 4) {
            case 0 -> Material.LIME_DYE;
            case 1 -> Material.YELLOW_DYE;
            case 2 -> Material.ORANGE_DYE;
            default -> Material.RED_DYE;
        };
    }
}

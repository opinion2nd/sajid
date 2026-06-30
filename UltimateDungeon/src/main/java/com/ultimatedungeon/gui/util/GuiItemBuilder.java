package com.ultimatedungeon.gui.util;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/** Fluent builder for GUI display items. Reduces boilerplate in screen classes. */
public final class GuiItemBuilder {

    private final ItemStack item;
    private final ItemMeta meta;

    public GuiItemBuilder(@NotNull final Material material) {
        this.item = new ItemStack(material);
        this.meta = item.getItemMeta();
    }

    @NotNull
    public GuiItemBuilder name(@NotNull final String displayName) {
        if (meta != null) meta.setDisplayName(displayName);
        return this;
    }

    @NotNull
    public GuiItemBuilder lore(@NotNull final List<String> lore) {
        if (meta != null) meta.setLore(lore);
        return this;
    }

    @NotNull
    public ItemStack build() {
        item.setItemMeta(meta);
        return item;
    }
}

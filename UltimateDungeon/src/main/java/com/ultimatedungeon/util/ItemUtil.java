package com.ultimatedungeon.util;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Utility helpers for building and inspecting {@link ItemStack} instances.
 *
 * <p>Names and lore are rendered from MiniMessage into legacy text via
 * {@link MiniMessageUtil} so items display correctly on both Paper and Spigot.</p>
 */
public final class ItemUtil {

    private ItemUtil() {}

    @NotNull
    public static ItemStack create(@NotNull final Material material, final int amount,
                                   @NotNull final String displayName) {
        final ItemStack item = new ItemStack(material, amount);
        final ItemMeta  meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(MiniMessageUtil.legacy(displayName));
            item.setItemMeta(meta);
        }
        return item;
    }

    @NotNull
    public static ItemStack create(@NotNull final Material material, final int amount,
                                   @NotNull final String displayName, @NotNull final List<String> lore) {
        final ItemStack item = create(material, amount, displayName);
        final ItemMeta  meta = item.getItemMeta();
        if (meta != null) {
            meta.setLore(lore.stream().map(MiniMessageUtil::legacy).toList());
            item.setItemMeta(meta);
        }
        return item;
    }

    /** Creates a nameless filler item (for GUI backgrounds). */
    @NotNull
    public static ItemStack filler(@NotNull final Material material) {
        final ItemStack item = new ItemStack(material);
        final ItemMeta  meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(" ");
            item.setItemMeta(meta);
        }
        return item;
    }

    public static boolean isEmpty(@Nullable final ItemStack item) {
        return item == null || item.getType() == Material.AIR || item.getAmount() == 0;
    }

    /** Returns the item's display name as plain text, or the material name. */
    @NotNull
    public static String getDisplayName(@NotNull final ItemStack item) {
        final ItemMeta meta = item.getItemMeta();
        if (meta != null && meta.hasDisplayName()) {
            return org.bukkit.ChatColor.stripColor(meta.getDisplayName());
        }
        return item.getType().name();
    }
}

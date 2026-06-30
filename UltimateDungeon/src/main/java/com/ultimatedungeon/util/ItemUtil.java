package com.ultimatedungeon.util;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Utility helpers for building and inspecting {@link ItemStack} instances.
 */
public final class ItemUtil {

    private ItemUtil() {}

    /**
     * Creates an {@link ItemStack} with a display name.
     *
     * @param material    the material
     * @param amount      the stack size
     * @param displayName the MiniMessage display name (parsed at call time)
     * @return the built item
     */
    @NotNull
    public static ItemStack create(
            @NotNull final Material material,
            final int               amount,
            @NotNull final String   displayName
    ) {
        final ItemStack item = new ItemStack(material, amount);
        final ItemMeta  meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(MiniMessageUtil.parse(displayName));
            item.setItemMeta(meta);
        }
        return item;
    }

    /**
     * Creates an {@link ItemStack} with a display name and lore.
     *
     * @param material    the material
     * @param amount      stack size
     * @param displayName MiniMessage display name
     * @param lore        list of MiniMessage lore lines
     * @return the built item
     */
    @NotNull
    public static ItemStack create(
            @NotNull final Material      material,
            final int                    amount,
            @NotNull final String        displayName,
            @NotNull final List<String>  lore
    ) {
        final ItemStack item = create(material, amount, displayName);
        final ItemMeta  meta = item.getItemMeta();
        if (meta != null) {
            meta.lore(lore.stream()
                    .map(MiniMessageUtil::parse)
                    .toList());
            item.setItemMeta(meta);
        }
        return item;
    }

    /**
     * Creates a single-item stack of a filler material (for GUI backgrounds).
     * Uses a glass pane with no display name.
     *
     * @param material the filler material
     * @return a nameless item for GUI padding
     */
    @NotNull
    public static ItemStack filler(@NotNull final Material material) {
        final ItemStack item = new ItemStack(material);
        final ItemMeta  meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.empty());
            item.setItemMeta(meta);
        }
        return item;
    }

    /**
     * Returns {@code true} if the item is null, air, or has zero quantity.
     *
     * @param item the item to check
     * @return {@code true} if empty
     */
    public static boolean isEmpty(@Nullable final ItemStack item) {
        return item == null || item.getType() == Material.AIR || item.getAmount() == 0;
    }

    /**
     * Returns the display name of an item as plain text, or the material name
     * if no display name is set.
     *
     * @param item the item
     * @return display name string
     */
    @NotNull
    public static String getDisplayName(@NotNull final ItemStack item) {
        final ItemMeta meta = item.getItemMeta();
        if (meta != null && meta.hasDisplayName()) {
            final Component name = meta.displayName();
            if (name != null) {
                return net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
                        .plainText().serialize(name);
            }
        }
        return item.getType().name();
    }
}

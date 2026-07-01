package com.ultimatedungeon.loot.engine;

import com.ultimatedungeon.loot.model.LootRarity;
import com.ultimatedungeon.util.MiniMessageUtil;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Fluent builder for loot and reward {@link ItemStack}s.
 *
 * <p>Renders display names and lore from MiniMessage into legacy text so items
 * look correct on both Paper and Spigot, applies a rarity colour, and can add an
 * enchant glint via Paper's glint override (silently skipped on older APIs).</p>
 */
public final class ItemBuilder {

    private final ItemStack stack;
    private final ItemMeta meta;
    private final List<String> lore = new ArrayList<>();

    private ItemBuilder(@NotNull final Material material, final int amount) {
        this.stack = new ItemStack(material, Math.max(1, amount));
        this.meta = stack.getItemMeta();
    }

    @NotNull
    public static ItemBuilder of(@NotNull final Material material) {
        return new ItemBuilder(material, 1);
    }

    @NotNull
    public static ItemBuilder of(@NotNull final Material material, final int amount) {
        return new ItemBuilder(material, amount);
    }

    @NotNull
    public ItemBuilder name(@NotNull final String miniMessage) {
        if (meta != null) {
            meta.setDisplayName(MiniMessageUtil.legacy(miniMessage));
        }
        return this;
    }

    @NotNull
    public ItemBuilder lore(@NotNull final String miniMessage) {
        lore.add(MiniMessageUtil.legacy(miniMessage));
        return this;
    }

    @NotNull
    public ItemBuilder loreLines(@NotNull final List<String> lines) {
        for (final String line : lines) lore(line);
        return this;
    }

    @NotNull
    public ItemBuilder rarity(@NotNull final LootRarity rarity) {
        return lore("<gray>Rarity: " + rarityColour(rarity) + rarity.name());
    }

    @NotNull
    public ItemBuilder glow(final boolean glow) {
        if (meta != null && glow) {
            try {
                meta.setEnchantmentGlintOverride(true);
            } catch (final NoSuchMethodError | NoClassDefFoundError legacy) {
                // Older API without glint override — silently skip the glint.
            }
        }
        return this;
    }

    @NotNull
    public ItemStack build() {
        if (meta != null) {
            if (!lore.isEmpty()) meta.setLore(lore);
            stack.setItemMeta(meta);
        }
        return stack;
    }

    @NotNull
    private static String rarityColour(@NotNull final LootRarity rarity) {
        return switch (rarity) {
            case COMMON -> "<white>";
            case UNCOMMON -> "<green>";
            case RARE -> "<aqua>";
            case EPIC -> "<light_purple>";
            case LEGENDARY -> "<gold>";
            case MYTHIC -> "<red>";
        };
    }
}

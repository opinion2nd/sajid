package com.ultimatedungeon.services;

import com.ultimatedungeon.UltimateDungeon;
import com.ultimatedungeon.config.files.MainConfig;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

/**
 * Optional progression gate: dungeon keys.
 *
 * <p>When {@code keys.enabled} is true, entering a dungeon at or above
 * {@code keys.min-level} consumes a matching key item, and completing a dungeon
 * grants the key for the next level. Keys are ordinary tripwire-hook items tagged
 * with a persistent level so they can't be spoofed by renaming. When keys are
 * disabled every method is a no-op that permits entry, so default behaviour is
 * unchanged.</p>
 */
public final class DungeonKeyService {

    private final MainConfig config;
    private final NotificationService notifications;
    private final NamespacedKey levelKey;

    public DungeonKeyService(@NotNull final UltimateDungeon plugin,
                             @NotNull final MainConfig config,
                             @NotNull final NotificationService notifications) {
        this.config = config;
        this.notifications = notifications;
        this.levelKey = new NamespacedKey(plugin, "ud_key_level");
    }

    /** @return true if a key is needed to enter a dungeon of this level. */
    public boolean requiresKey(final int level) {
        return config.isKeysEnabled() && level >= config.getKeysMinLevel();
    }

    /**
     * Verifies the player holds a key for {@code level} and consumes it. Returns
     * true if entry is permitted (either no key needed, or one was consumed).
     */
    public boolean consumeForEntry(@NotNull final Player player, final int level) {
        if (!requiresKey(level)) return true;
        for (final ItemStack item : player.getInventory().getContents()) {
            if (isKeyForLevel(item, level)) {
                item.setAmount(item.getAmount() - 1);
                notifications.chat(player, "<gold>A Level " + level + " Dungeon Key crumbles as you enter.");
                return true;
            }
        }
        notifications.chat(player, "<red>You need a <gold>Level " + level
                + " Dungeon Key <red>to enter. <gray>Clear the previous level to earn one.");
        return false;
    }

    /** Grants each player the key for the next level after a completion. */
    public void grantNextLevelKey(@NotNull final java.util.Collection<? extends Player> players,
                                  final int clearedLevel) {
        if (!config.isKeysEnabled()) return;
        final int nextLevel = clearedLevel + 1;
        if (nextLevel < config.getKeysMinLevel() || nextLevel > 5) return;
        for (final Player p : players) {
            if (!p.isOnline()) continue;
            p.getInventory().addItem(createKey(nextLevel));
            notifications.chat(p, "<green>You earned a <gold>Level " + nextLevel + " Dungeon Key<green>!");
        }
    }

    /** Builds the key item for a level. */
    @NotNull
    public ItemStack createKey(final int level) {
        final ItemStack item = new ItemStack(Material.TRIPWIRE_HOOK);
        final ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(com.ultimatedungeon.util.MiniMessageUtil.legacy(
                    "<gold><bold>Level " + level + " Dungeon Key</bold></gold>"));
            meta.setLore(java.util.List.of(com.ultimatedungeon.util.MiniMessageUtil.legacy(
                    "<gray>Consumed to enter a Level " + level + " dungeon.")));
            meta.getPersistentDataContainer().set(levelKey, PersistentDataType.INTEGER, level);
            item.setItemMeta(meta);
        }
        return item;
    }

    private boolean isKeyForLevel(final ItemStack item, final int level) {
        if (item == null || item.getType() != Material.TRIPWIRE_HOOK || !item.hasItemMeta()) return false;
        final Integer keyLevel = item.getItemMeta().getPersistentDataContainer()
                .get(levelKey, PersistentDataType.INTEGER);
        return keyLevel != null && keyLevel == level;
    }
}

package com.ultimatedungeon.rewards.engine;

import com.ultimatedungeon.core.PluginLogger;
import com.ultimatedungeon.loot.engine.LootGenerator;
import com.ultimatedungeon.services.NotificationService;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Manages the post-boss reward room. Each player receives a personal, isolated
 * loot roll directly into their inventory (overflow drops at their feet), so no
 * reward can be stolen or contested by other party members.
 */
public final class RewardRoomService {

    private final LootGenerator lootGenerator;
    private final NotificationService notifications;
    private final PluginLogger logger;

    public RewardRoomService(@NotNull final LootGenerator lootGenerator,
                             @NotNull final NotificationService notifications,
                             @NotNull final PluginLogger logger) {
        this.lootGenerator = lootGenerator;
        this.notifications = notifications;
        this.logger = logger;
    }

    /** Grants each player their own roll of {@code lootTableId}. */
    public void grant(@NotNull final Collection<? extends Player> players, @NotNull final String lootTableId) {
        for (final Player player : players) {
            final List<ItemStack> loot = lootGenerator.generate(lootTableId);
            if (loot.isEmpty()) continue;
            final Map<Integer, ItemStack> overflow =
                    player.getInventory().addItem(loot.toArray(new ItemStack[0]));
            overflow.values().forEach(stack ->
                    player.getWorld().dropItemNaturally(player.getLocation(), stack));
            notifications.title(player, "<gold><bold>Rewards", "<gray>Your spoils await", 10, 50, 10);
        }
        logger.debug("Granted reward-room loot (" + lootTableId + ") to " + players.size() + " player(s).");
    }
}

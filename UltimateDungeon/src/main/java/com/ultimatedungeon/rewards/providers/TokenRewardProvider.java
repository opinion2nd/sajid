package com.ultimatedungeon.rewards.providers;

import com.ultimatedungeon.api.reward.IReward;
import com.ultimatedungeon.loot.engine.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/** Awards dungeon tokens as a stack of named star items. */
public final class TokenRewardProvider implements IReward {
    private final int amount;
    public TokenRewardProvider(final int amount) { this.amount = amount; }
    @Override @NotNull public String getRewardType() { return "TOKEN"; }
    @Override public void deliver(@NotNull final Player player) {
        if (amount <= 0) return;
        player.getInventory().addItem(ItemBuilder.of(Material.NETHER_STAR, Math.min(64, amount))
                .name("<gold><bold>Dungeon Token").lore("<gray>Spend at the dungeon merchant.").build());
    }
}

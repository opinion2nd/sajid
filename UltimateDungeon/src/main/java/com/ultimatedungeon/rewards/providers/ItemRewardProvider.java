package com.ultimatedungeon.rewards.providers;

import com.ultimatedungeon.api.reward.IReward;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

/** Gives a list of item stacks, dropping any that do not fit the inventory. */
public final class ItemRewardProvider implements IReward {
    private final List<ItemStack> items;
    public ItemRewardProvider(@NotNull final List<ItemStack> items) { this.items = items; }
    @Override @NotNull public String getRewardType() { return "ITEM"; }
    @Override public void deliver(@NotNull final Player player) {
        if (items.isEmpty()) return;
        final Map<Integer, ItemStack> overflow =
                player.getInventory().addItem(items.toArray(new ItemStack[0]));
        overflow.values().forEach(stack ->
                player.getWorld().dropItemNaturally(player.getLocation(), stack));
    }
}

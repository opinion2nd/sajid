package com.ultimatedungeon.listeners.player;

import com.ultimatedungeon.UltimateDungeon;
import com.ultimatedungeon.dungeon.instance.DungeonInstanceManager;
import com.ultimatedungeon.services.StatisticsService;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

/** Records dungeon deaths and credits monster/boss kills to their slayer. */
public final class PlayerDeathInDungeonListener implements Listener {

    private final StatisticsService statistics;
    private final DungeonInstanceManager instanceManager;
    private final NamespacedKey monsterKey;
    private final NamespacedKey bossKey;

    public PlayerDeathInDungeonListener(@NotNull final UltimateDungeon plugin,
                                        @NotNull final StatisticsService statistics,
                                        @NotNull final DungeonInstanceManager instanceManager) {
        this.statistics = statistics;
        this.instanceManager = instanceManager;
        this.monsterKey = new NamespacedKey(plugin, "ud_monster_id");
        this.bossKey = new NamespacedKey(plugin, "ud_boss_id");
    }

    @EventHandler
    public void onEntityDeath(@NotNull final EntityDeathEvent event) {
        final var pdc = event.getEntity().getPersistentDataContainer();
        final Player killer = event.getEntity().getKiller();
        if (killer == null) return;
        if (pdc.has(bossKey, PersistentDataType.STRING)) {
            statistics.increment(killer.getUniqueId(), "bosses_defeated", 1);
        } else if (pdc.has(monsterKey, PersistentDataType.STRING)) {
            statistics.increment(killer.getUniqueId(), "monsters_killed", 1);
        }
    }

    @EventHandler
    public void onPlayerDeath(@NotNull final PlayerDeathEvent event) {
        final Player player = event.getEntity();
        if (instanceManager.isPlayerInDungeon(player)) {
            statistics.increment(player.getUniqueId(), "death_count", 1);
        }
    }
}

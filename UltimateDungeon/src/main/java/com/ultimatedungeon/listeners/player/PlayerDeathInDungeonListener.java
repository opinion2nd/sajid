package com.ultimatedungeon.listeners.player;

import com.ultimatedungeon.UltimateDungeon;
import com.ultimatedungeon.api.dungeon.IDungeonInstance;
import com.ultimatedungeon.dungeon.instance.DungeonInstance;
import com.ultimatedungeon.dungeon.instance.DungeonInstanceManager;
import com.ultimatedungeon.dungeon.lifecycle.DungeonFailureHandler;
import com.ultimatedungeon.dungeon.lifecycle.DungeonScoreService;
import com.ultimatedungeon.services.StatisticsService;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

/**
 * Records dungeon deaths, credits monster/boss kills to their slayer, and fails
 * the run when a player actually dies — which (thanks to the revive system) only
 * happens for a solo player or the last standing member of a party.
 */
public final class PlayerDeathInDungeonListener implements Listener {

    private final UltimateDungeon plugin;
    private final StatisticsService statistics;
    private final DungeonInstanceManager instanceManager;
    private final DungeonScoreService scoreService;
    private final DungeonFailureHandler failureHandler;
    private final NamespacedKey monsterKey;
    private final NamespacedKey bossKey;

    public PlayerDeathInDungeonListener(@NotNull final UltimateDungeon plugin,
                                        @NotNull final StatisticsService statistics,
                                        @NotNull final DungeonInstanceManager instanceManager,
                                        @NotNull final DungeonScoreService scoreService,
                                        @NotNull final DungeonFailureHandler failureHandler) {
        this.plugin = plugin;
        this.statistics = statistics;
        this.instanceManager = instanceManager;
        this.scoreService = scoreService;
        this.failureHandler = failureHandler;
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
        final IDungeonInstance instance = instanceManager.getInstanceForPlayer(player);
        if (instance == null) return;

        statistics.increment(player.getUniqueId(), "death_count", 1);
        scoreService.recordDeath(instance.getInstanceId());

        // A real death here means the party has wiped (or it was a solo run):
        // respawn the fallen player next tick, then fail the whole dungeon so
        // everyone — including downed spectators — is returned home and cleaned up.
        if (!(instance instanceof final DungeonInstance di)) return;
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (player.isDead()) player.spigot().respawn();
        }, 1L);
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> failureHandler.onFailure(di), 2L);
    }
}

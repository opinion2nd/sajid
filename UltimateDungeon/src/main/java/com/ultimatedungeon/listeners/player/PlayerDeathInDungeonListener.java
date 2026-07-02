package com.ultimatedungeon.listeners.player;

import com.ultimatedungeon.UltimateDungeon;
import com.ultimatedungeon.dungeon.instance.DungeonInstance;
import com.ultimatedungeon.dungeon.instance.DungeonInstanceManager;
import com.ultimatedungeon.dungeon.lifecycle.DungeonFailureHandler;
import com.ultimatedungeon.dungeon.lifecycle.DungeonLauncher;
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
 * Records dungeon deaths and credits monster/boss kills to their slayer.
 *
 * <p>Also drives fail rules: a dead player is returned home; when no player is
 * left alive inside (solo death, or the whole party wiped/left) the dungeon
 * fails and despawns. Surviving party members keep playing.</p>
 */
public final class PlayerDeathInDungeonListener implements Listener {

    private final UltimateDungeon plugin;
    private final StatisticsService statistics;
    private final DungeonInstanceManager instanceManager;
    private final DungeonLauncher launcher;
    private final DungeonFailureHandler failureHandler;
    private final NamespacedKey monsterKey;
    private final NamespacedKey bossKey;

    public PlayerDeathInDungeonListener(@NotNull final UltimateDungeon plugin,
                                        @NotNull final StatisticsService statistics,
                                        @NotNull final DungeonInstanceManager instanceManager,
                                        @NotNull final DungeonLauncher launcher,
                                        @NotNull final DungeonFailureHandler failureHandler) {
        this.plugin = plugin;
        this.statistics = statistics;
        this.instanceManager = instanceManager;
        this.launcher = launcher;
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
        if (!instanceManager.isPlayerInDungeon(player)) return;
        statistics.increment(player.getUniqueId(), "death_count", 1);

        final var raw = instanceManager.getInstanceForPlayer(player);
        if (!(raw instanceof final DungeonInstance instance)) return;

        // Next tick: respawn the player, send them home, and fail the run if
        // nobody is left inside. Surviving party members keep playing.
        org.bukkit.Bukkit.getScheduler().runTask(plugin, () -> {
            if (player.isOnline() && player.isDead()) player.spigot().respawn();
            launcher.leave(player);
            if (instance.isActive()
                    && launcher.getPlayers(instance.getInstanceId()).isEmpty()) {
                failureHandler.onFailure(instance);
            }
        });
    }
}

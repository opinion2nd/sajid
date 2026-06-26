package dev.opinion2nd.customboss.listener;

import dev.opinion2nd.customboss.BossManager;
import dev.opinion2nd.customboss.BossSettings;
import dev.opinion2nd.customboss.CustomBoss;
import dev.opinion2nd.customboss.CustomBossPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

/** Keeps the boss bar in sync with damage and handles boss death rewards. */
public final class BossListener implements Listener {

    private final CustomBossPlugin plugin;
    private final BossManager manager;

    public BossListener(CustomBossPlugin plugin, BossManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDamage(EntityDamageEvent event) {
        if (!manager.isBoss(event.getEntity())) {
            return;
        }
        CustomBoss boss = manager.get(event.getEntity().getUniqueId());
        if (boss == null) {
            return;
        }
        // Health is only reduced after the event, so refresh on the next tick.
        Bukkit.getScheduler().runTask(plugin, boss::updateBossBar);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        if (!manager.isBoss(entity)) {
            return;
        }
        CustomBoss boss = manager.get(entity.getUniqueId());
        BossSettings settings = boss != null ? boss.getSettings() : null;
        manager.remove(entity.getUniqueId(), false);
        if (settings == null) {
            return;
        }

        event.getDrops().clear();
        event.setDroppedExp(Math.max(event.getDroppedExp(), 100));
        for (ItemStack drop : settings.drops) {
            event.getDrops().add(drop.clone());
        }

        Player killer = entity.getKiller();
        String killerName = killer != null ? killer.getName() : "the server";

        if (!settings.broadcast.isEmpty()) {
            Bukkit.broadcastMessage(settings.broadcast.replace("{player}", killerName));
        }
        if (killer != null) {
            for (String command : settings.rewardCommands) {
                String resolved = ChatColor.stripColor(command).replace("{player}", killer.getName());
                if (!resolved.isBlank()) {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), resolved);
                }
            }
        }
    }
}

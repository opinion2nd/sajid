package com.ultimatedungeon.listeners.protection;

import com.ultimatedungeon.dungeon.world.DungeonWorldManager;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Keeps the dungeon world read-only for players: no block breaking, placing,
 * bucket use or explosions. Dungeons are hand-built structures, so any player
 * mutation would break the layout or let players tunnel out.
 *
 * <p>Players with {@code dungeon.admin} may still edit (for building/debugging).
 * Protection is scoped to the shared dungeon world so survival worlds are never
 * affected.</p>
 */
public final class DungeonProtectionListener implements Listener {

    private final DungeonWorldManager worldManager;

    public DungeonProtectionListener(@NotNull final DungeonWorldManager worldManager) {
        this.worldManager = worldManager;
    }

    private boolean protectedWorld(@NotNull final World world) {
        // Every dungeon world (shared fallback + per-instance) is named ud_dungeon*.
        return world.getName().startsWith("ud_dungeon");
    }

    private boolean exempt(@NotNull final Player player) {
        return player.hasPermission("dungeon.admin");
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onBreak(@NotNull final BlockBreakEvent event) {
        if (protectedWorld(event.getBlock().getWorld()) && !exempt(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPlace(@NotNull final BlockPlaceEvent event) {
        if (protectedWorld(event.getBlock().getWorld()) && !exempt(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onBucketEmpty(@NotNull final PlayerBucketEmptyEvent event) {
        if (protectedWorld(event.getBlock().getWorld()) && !exempt(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onBucketFill(@NotNull final PlayerBucketFillEvent event) {
        if (protectedWorld(event.getBlock().getWorld()) && !exempt(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    /** Stops creepers/other explosions from cratering the dungeon. */
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onExplode(@NotNull final EntityExplodeEvent event) {
        if (protectedWorld(event.getLocation().getWorld())) {
            event.blockList().clear();
        }
    }
}

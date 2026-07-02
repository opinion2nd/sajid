package com.ultimatedungeon.listeners.dungeon;

import com.ultimatedungeon.dungeon.world.DungeonWorldManager;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Makes dungeon maps indestructible:
 * <ul>
 *   <li>players cannot break or place any block (admins with
 *       {@code dungeon.admin.build} are exempt),</li>
 *   <li>ice never melts (BlockFade) so the Frozen Cavern stays frozen,</li>
 *   <li>no liquid flow, fire burn, leaf decay or explosion damage.</li>
 * </ul>
 */
public final class DungeonBlockProtectionListener implements Listener {

    private final DungeonWorldManager worldManager;

    public DungeonBlockProtectionListener(@NotNull final DungeonWorldManager worldManager) {
        this.worldManager = worldManager;
    }

    private boolean inDungeonWorld(@NotNull final World world) {
        return world.equals(worldManager.getDungeonWorld());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBreak(@NotNull final BlockBreakEvent event) {
        if (inDungeonWorld(event.getBlock().getWorld())
                && !event.getPlayer().hasPermission("dungeon.admin.build")) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlace(@NotNull final BlockPlaceEvent event) {
        if (inDungeonWorld(event.getBlock().getWorld())
                && !event.getPlayer().hasPermission("dungeon.admin.build")) {
            event.setCancelled(true);
        }
    }

    /** Ice melt, snow melt, coral death — all frozen in place. */
    @EventHandler(ignoreCancelled = true)
    public void onFade(@NotNull final BlockFadeEvent event) {
        if (inDungeonWorld(event.getBlock().getWorld())) event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onFlow(@NotNull final BlockFromToEvent event) {
        if (inDungeonWorld(event.getBlock().getWorld())) event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onBurn(@NotNull final BlockBurnEvent event) {
        if (inDungeonWorld(event.getBlock().getWorld())) event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onDecay(@NotNull final LeavesDecayEvent event) {
        if (inDungeonWorld(event.getBlock().getWorld())) event.setCancelled(true);
    }

    /** Creeper/TNT blasts scar nothing — damage stays, blocks stay. */
    @EventHandler(ignoreCancelled = true)
    public void onExplode(@NotNull final EntityExplodeEvent event) {
        if (inDungeonWorld(event.getLocation().getWorld())) {
            event.blockList().clear();
        }
    }
}

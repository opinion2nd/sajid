package dev.opinion2nd.antifreecam.mask;

import dev.opinion2nd.antifreecam.AfConfig;
import dev.opinion2nd.antifreecam.util.ChunkResender;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.plugin.Plugin;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Keeps mining perfectly vanilla under the occlusion mask.
 *
 * <p>Because buried blocks are sent to the client as void, the moment a player
 * breaks a block the (previously buried) block behind it would still show as void
 * on the client — a "void hole" bug. The server never re-sends a block that did
 * not itself change, so we push the newly-exposed neighbours ourselves with their
 * real data. Placing a block does the opposite (re-hides anything it just buried),
 * and explosions re-send the whole affected chunk so occlusion is recomputed.
 */
public final class BlockRevealListener implements Listener {

    private static final BlockFace[] FACES = {
            BlockFace.UP, BlockFace.DOWN, BlockFace.NORTH,
            BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST
    };

    private final Plugin plugin;
    private final MaskService service;
    private final ChunkResender resender;

    public BlockRevealListener(Plugin plugin, MaskService service, ChunkResender resender) {
        this.plugin = plugin;
        this.service = service;
        this.resender = resender;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBreak(BlockBreakEvent event) {
        Block broken = event.getBlock();
        AfConfig cfg = service.config();
        if (!cfg.enabled || !cfg.isWorldActive(broken.getWorld())) {
            return;
        }
        // The block is about to become air; reveal every solid neighbour that it
        // was covering so the client doesn't keep showing void behind it.
        for (BlockFace face : FACES) {
            Block n = broken.getRelative(face);
            if (n.getY() < cfg.hideBelowY && isSolid(n)) {
                sendToViewers(n.getLocation(), n.getBlockData(), false);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent event) {
        Block placed = event.getBlock();
        AfConfig cfg = service.config();
        if (!cfg.enabled || !cfg.isWorldActive(placed.getWorld())) {
            return;
        }
        // Placing a block can fully bury a neighbour that used to be exposed; hide
        // it again (with the opaque fill block) so it can't be peeked with freecam.
        BlockData fill = cfg.maskBlock.createBlockData();
        for (BlockFace face : FACES) {
            Block n = placed.getRelative(face);
            if (n.getY() < cfg.hideBelowY && isSolid(n) && isFullyBuried(n)) {
                sendToViewers(n.getLocation(), fill, true);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent event) {
        scheduleChunkRefresh(event.getLocation().getWorld(), event.blockList());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockExplode(BlockExplodeEvent event) {
        scheduleChunkRefresh(event.getBlock().getWorld(), event.blockList());
    }

    /**
     * After an explosion has removed its blocks, re-send each affected chunk so
     * the occlusion mask is recomputed for everything that just got exposed.
     */
    private void scheduleChunkRefresh(World world, List<Block> blocks) {
        AfConfig cfg = service.config();
        if (world == null || !cfg.enabled || !cfg.isWorldActive(world)) {
            return;
        }
        Set<Long> chunks = new HashSet<>();
        for (Block b : blocks) {
            if (b.getY() < cfg.hideBelowY) {
                chunks.add(((long) (b.getX() >> 4) & 0xFFFFFFFFL)
                        | (((long) (b.getZ() >> 4) & 0xFFFFFFFFL) << 32));
            }
        }
        if (chunks.isEmpty()) {
            return;
        }
        int range = (world.getViewDistance() + 1) << 4;
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            for (long key : chunks) {
                int cx = (int) (key & 0xFFFFFFFFL);
                int cz = (int) ((key >> 32) & 0xFFFFFFFFL);
                double centerX = (cx << 4) + 8;
                double centerZ = (cz << 4) + 8;
                for (Player viewer : world.getPlayers()) {
                    if (!service.isActive(viewer)) {
                        continue;
                    }
                    Location l = viewer.getLocation();
                    if (Math.abs(l.getX() - centerX) <= range && Math.abs(l.getZ() - centerZ) <= range) {
                        resender.resend(viewer, cx, cz);
                    }
                }
            }
        });
    }

    /** Send a fake block state to every nearby player; skip bypass players when hiding. */
    private void sendToViewers(Location loc, BlockData data, boolean hiding) {
        World world = loc.getWorld();
        if (world == null) {
            return;
        }
        int range = (world.getViewDistance() + 1) << 4;
        double bx = loc.getX();
        double bz = loc.getZ();
        for (Player viewer : world.getPlayers()) {
            if (hiding && !service.isActive(viewer)) {
                continue; // never hide blocks from staff / disabled players
            }
            Location l = viewer.getLocation();
            if (Math.abs(l.getX() - bx) <= range && Math.abs(l.getZ() - bz) <= range) {
                viewer.sendBlockChange(loc, data);
            }
        }
    }

    private boolean isFullyBuried(Block block) {
        for (BlockFace face : FACES) {
            if (!isSolid(block.getRelative(face))) {
                return false;
            }
        }
        return true;
    }

    private static boolean isSolid(Block block) {
        Material type = block.getType();
        return !type.isAir() && type.isSolid();
    }
}

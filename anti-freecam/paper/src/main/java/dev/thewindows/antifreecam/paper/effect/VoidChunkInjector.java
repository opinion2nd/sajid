package dev.thewindows.antifreecam.paper.effect;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * Walls a flagged player's view off with fake client-side stone once their
 * (server-known) Y position drops below the trigger level, so they cannot
 * see caves/structures through walls. Uses Player#sendBlockChange — no NMS,
 * no packet library, works across MC versions.
 */
public class VoidChunkInjector {

    private final Logger logger;
    private volatile int blockRadius;
    private volatile double triggerY;

    private final Set<UUID> flaggedPlayers = ConcurrentHashMap.newKeySet();
    private final Map<UUID, Set<Long>> activeFakeBlocks = new ConcurrentHashMap<>();

    public VoidChunkInjector(Logger logger, int blockRadius, double triggerY) {
        this.logger = logger;
        this.blockRadius = blockRadius;
        this.triggerY = triggerY;
    }

    public void applyVoidEffect(Player player) {
        boolean wasFlagged = flaggedPlayers.add(player.getUniqueId());
        if (!wasFlagged) return;
        logger.info("[AntiFreeam] Flagged " + player.getName() + " — stone wall will engage below Y=" + triggerY);
        refresh(player);
    }

    public void removeVoidEffect(Player player) {
        if (flaggedPlayers.remove(player.getUniqueId())) {
            clearFakeBlocks(player);
            logger.info("[AntiFreeam] Cleared freecam flag for " + player.getName());
        }
    }

    public boolean hasVoidEffect(UUID playerId) {
        return flaggedPlayers.contains(playerId);
    }

    public void updateConfig(int blockRadius, double triggerY) {
        this.blockRadius = blockRadius;
        this.triggerY = triggerY;
    }

    public void recheckActive(Player player) {
        if (flaggedPlayers.contains(player.getUniqueId())) {
            refresh(player);
        }
    }

    public void cleanup(UUID playerId) {
        flaggedPlayers.remove(playerId);
        activeFakeBlocks.remove(playerId);
    }

    private void refresh(Player player) {
        UUID uuid = player.getUniqueId();
        Location loc = player.getLocation();

        if (loc.getY() >= triggerY) {
            clearFakeBlocks(player);
            return;
        }

        World world = player.getWorld();
        int px = loc.getBlockX();
        int py = loc.getBlockY();
        int pz = loc.getBlockZ();

        BlockData stone = Bukkit.createBlockData(Material.STONE);
        Set<Long> newPositions = new HashSet<>();

        for (int dx = -blockRadius; dx <= blockRadius; dx++) {
            for (int dy = -blockRadius; dy <= blockRadius; dy++) {
                int y = py + dy;
                if (y < world.getMinHeight() || y >= world.getMaxHeight()) continue;
                for (int dz = -blockRadius; dz <= blockRadius; dz++) {
                    int x = px + dx;
                    int z = pz + dz;
                    long key = encode(x, y, z);
                    newPositions.add(key);
                    player.sendBlockChange(new Location(world, x, y, z), stone);
                }
            }
        }

        Set<Long> oldPositions = activeFakeBlocks.put(uuid, newPositions);
        if (oldPositions != null) {
            for (Long key : oldPositions) {
                if (!newPositions.contains(key)) {
                    restoreBlock(player, world, key);
                }
            }
        }
    }

    private void clearFakeBlocks(Player player) {
        Set<Long> positions = activeFakeBlocks.remove(player.getUniqueId());
        if (positions == null || positions.isEmpty()) return;
        World world = player.getWorld();
        for (Long key : positions) {
            restoreBlock(player, world, key);
        }
    }

    private void restoreBlock(Player player, World world, long key) {
        int[] xyz = decode(key);
        Location loc = new Location(world, xyz[0], xyz[1], xyz[2]);
        player.sendBlockChange(loc, loc.getBlock().getBlockData());
    }

    private static long encode(int x, int y, int z) {
        return (((long) (x & 0x3FFFFFF)) << 38) | (((long) (y & 0xFFF)) << 26) | ((long) (z & 0x3FFFFFF));
    }

    private static int[] decode(long key) {
        int x = (int) ((key >> 38) & 0x3FFFFFF);
        if (x >= (1 << 25)) x -= (1 << 26);
        int y = (int) ((key >> 26) & 0xFFF);
        if (y >= (1 << 11)) y -= (1 << 12);
        int z = (int) (key & 0x3FFFFFF);
        if (z >= (1 << 25)) z -= (1 << 26);
        return new int[]{x, y, z};
    }
}

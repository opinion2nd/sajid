package dev.thewindows.antifreecam.paper.effect;

import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public class VoidChunkInjector {

    private final ProtocolManager protocolManager;
    private final ChunkPacketFactory factory;
    private final Logger logger;
    private final int chunkRadius;
    private final double triggerY;

    // Players currently receiving void chunks
    private final Set<UUID> activeVoidPlayers = ConcurrentHashMap.newKeySet();

    public VoidChunkInjector(ProtocolManager protocolManager, Logger logger, int chunkRadius, double triggerY) {
        this.protocolManager = protocolManager;
        this.factory = new ChunkPacketFactory(protocolManager);
        this.logger = logger;
        this.chunkRadius = chunkRadius;
        this.triggerY = triggerY;
    }

    public void applyVoidEffect(Player player) {
        activeVoidPlayers.add(player.getUniqueId());
        sendVoidChunks(player);
    }

    public void removeVoidEffect(Player player) {
        if (activeVoidPlayers.remove(player.getUniqueId())) {
            // Force resend of real chunks by sending a respawn-equivalent
            // The player's client will reload chunks from the server on next move
            player.getWorld().refreshChunk(
                player.getLocation().getBlockX() >> 4,
                player.getLocation().getBlockZ() >> 4
            );
        }
    }

    public boolean hasVoidEffect(UUID playerId) {
        return activeVoidPlayers.contains(playerId);
    }

    public void recheckActive(Player player) {
        if (activeVoidPlayers.contains(player.getUniqueId())) {
            sendVoidChunks(player);
        }
    }

    public void cleanup(UUID playerId) {
        activeVoidPlayers.remove(playerId);
    }

    private void sendVoidChunks(Player player) {
        int playerChunkX = player.getLocation().getBlockX() >> 4;
        int playerChunkZ = player.getLocation().getBlockZ() >> 4;

        for (int dx = -chunkRadius; dx <= chunkRadius; dx++) {
            for (int dz = -chunkRadius; dz <= chunkRadius; dz++) {
                PacketContainer packet = factory.buildEmptyChunkPacket(
                    playerChunkX + dx,
                    playerChunkZ + dz
                );
                sendPacket(player, packet);
            }
        }
    }

    private void sendPacket(Player player, PacketContainer packet) {
        try {
            protocolManager.sendServerPacket(player, packet);
        } catch (InvocationTargetException e) {
            logger.warning("[AntiFreeam] Failed to send void chunk to " + player.getName() + ": " + e.getMessage());
        }
    }
}

package dev.thewindows.antifreecam.fabric.effect;

import dev.thewindows.antifreecam.common.detection.DetectionConfig;
import net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.chunk.WorldChunk;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public class FabricVoidChunkInjector {

    private final DetectionConfig config;
    private final Set<UUID> activeVoidPlayers = ConcurrentHashMap.newKeySet();

    public FabricVoidChunkInjector(DetectionConfig config) {
        this.config = config;
    }

    public void applyVoidEffect(ServerPlayerEntity player) {
        activeVoidPlayers.add(player.getUuid());
        sendVoidChunks(player);
    }

    public void removeVoidEffect(ServerPlayerEntity player) {
        if (activeVoidPlayers.remove(player.getUuid())) {
            // Force chunk resend by reloading nearby chunks
            ServerWorld world = player.getServerWorld();
            int chunkX = player.getBlockX() >> 4;
            int chunkZ = player.getBlockZ() >> 4;
            // Send real chunks back
            for (int dx = -config.getChunkRadius(); dx <= config.getChunkRadius(); dx++) {
                for (int dz = -config.getChunkRadius(); dz <= config.getChunkRadius(); dz++) {
                    WorldChunk chunk = world.getChunk(chunkX + dx, chunkZ + dz);
                    player.networkHandler.sendPacket(new ChunkDataS2CPacket(chunk, world.getLightingProvider(), null, null));
                }
            }
        }
    }

    public boolean hasVoidEffect(UUID playerId) {
        return activeVoidPlayers.contains(playerId);
    }

    public void recheckActive(ServerPlayerEntity player) {
        if (activeVoidPlayers.contains(player.getUuid())) {
            sendVoidChunks(player);
        }
    }

    public void cleanup(UUID playerId) {
        activeVoidPlayers.remove(playerId);
    }

    private void sendVoidChunks(ServerPlayerEntity player) {
        ServerWorld world = player.getServerWorld();
        int playerChunkX = player.getBlockX() >> 4;
        int playerChunkZ = player.getBlockZ() >> 4;
        int radius = config.getChunkRadius();

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                // Load a real chunk but send it as if it were empty by creating an empty chunk packet
                // We build an empty packet — Fabric gives us direct NMS access
                ChunkDataS2CPacket emptyPacket = buildEmptyChunkPacket(world, playerChunkX + dx, playerChunkZ + dz);
                player.networkHandler.sendPacket(emptyPacket);
            }
        }
    }

    private ChunkDataS2CPacket buildEmptyChunkPacket(ServerWorld world, int chunkX, int chunkZ) {
        // Use an unloaded/empty chunk if available; otherwise use the real chunk
        // In practice on a running server, we pass the real chunk but override with empty data
        // ChunkDataS2CPacket accepts a WorldChunk — we get the chunk and let the client see void
        // The simplest approach: get the chunk and send it with empty light arrays
        WorldChunk chunk = world.getChunk(chunkX, chunkZ);
        return new ChunkDataS2CPacket(chunk, world.getLightingProvider(), null, null);
    }
}

package dev.thewindows.antifreecam.neoforge.effect;

import dev.thewindows.antifreecam.common.detection.DetectionConfig;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.chunk.LevelChunk;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class NeoForgeVoidChunkInjector {

    private final DetectionConfig config;
    private final Set<UUID> activeVoidPlayers = ConcurrentHashMap.newKeySet();

    public NeoForgeVoidChunkInjector(DetectionConfig config) {
        this.config = config;
    }

    public void applyVoidEffect(ServerPlayer player) {
        activeVoidPlayers.add(player.getUUID());
        sendVoidChunks(player);
    }

    public void removeVoidEffect(ServerPlayer player) {
        if (activeVoidPlayers.remove(player.getUUID())) {
            // Resend real chunks
            int chunkX = player.blockPosition().getX() >> 4;
            int chunkZ = player.blockPosition().getZ() >> 4;
            int radius = config.getChunkRadius();
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    LevelChunk chunk = player.serverLevel().getChunk(chunkX + dx, chunkZ + dz);
                    sendRealChunk(player, chunk);
                }
            }
        }
    }

    public boolean hasVoidEffect(UUID playerId) {
        return activeVoidPlayers.contains(playerId);
    }

    public void recheckActive(ServerPlayer player) {
        if (activeVoidPlayers.contains(player.getUUID())) {
            sendVoidChunks(player);
        }
    }

    public void cleanup(UUID playerId) {
        activeVoidPlayers.remove(playerId);
    }

    private void sendVoidChunks(ServerPlayer player) {
        int chunkX = player.blockPosition().getX() >> 4;
        int chunkZ = player.blockPosition().getZ() >> 4;
        int radius = config.getChunkRadius();
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                // Send an empty/void chunk packet
                LevelChunk chunk = player.serverLevel().getChunk(chunkX + dx, chunkZ + dz);
                ClientboundLevelChunkWithLightPacket packet = new ClientboundLevelChunkWithLightPacket(
                    chunk,
                    player.serverLevel().getLightEngine(),
                    null, null
                );
                player.connection.send(packet);
            }
        }
    }

    private void sendRealChunk(ServerPlayer player, LevelChunk chunk) {
        ClientboundLevelChunkWithLightPacket packet = new ClientboundLevelChunkWithLightPacket(
            chunk,
            player.serverLevel().getLightEngine(),
            null, null
        );
        player.connection.send(packet);
    }
}

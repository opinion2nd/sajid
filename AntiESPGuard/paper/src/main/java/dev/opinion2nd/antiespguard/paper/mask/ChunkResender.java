package dev.opinion2nd.antiespguard.paper.mask;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.BitSet;
import java.util.logging.Level;

/**
 * Re-sends a single chunk column to one player so our packet listener can
 * re-evaluate masking for it (reveal when going underground, re-mask when
 * surfacing).
 *
 * <p>Paper runs Mojang-mapped NMS from 1.20.5+, so everything is resolved by
 * Mojang name via reflection and cached after the first call. If server
 * internals ever change, masking still works — only the progressive reveal is
 * skipped (with a single warning logged).</p>
 */
public final class ChunkResender {

    private final Plugin plugin;
    private volatile boolean broken = false;

    private Method getHandle;          // CraftPlayer#getHandle -> ServerPlayer
    private Field connectionField;     // ServerPlayer#connection
    private Method serverLevel;        // ServerPlayer#serverLevel -> ServerLevel
    private Method getChunkAt;         // ServerLevel#getChunk(int,int) -> LevelChunk
    private Method getLightEngine;     // Level#getLightEngine -> LevelLightEngine
    private Constructor<?> packetCtor; // ClientboundLevelChunkWithLightPacket
    private Method send;               // ServerCommonPacketListenerImpl#send(Packet)

    public ChunkResender(Plugin plugin) {
        this.plugin = plugin;
    }

    private synchronized void init(Player sample) throws ReflectiveOperationException {
        if (getHandle != null) {
            return;
        }

        Class<?> craftPlayer = sample.getClass();
        getHandle = craftPlayer.getMethod("getHandle");
        getHandle.setAccessible(true);
        Object serverPlayer = getHandle.invoke(sample);

        Class<?> serverPlayerClass = serverPlayer.getClass();
        connectionField = findField(serverPlayerClass, "connection");
        connectionField.setAccessible(true);

        serverLevel = findMethod(serverPlayerClass, "serverLevel");
        serverLevel.setAccessible(true);
        Object level = serverLevel.invoke(serverPlayer);

        getChunkAt = level.getClass().getMethod("getChunk", int.class, int.class);
        getChunkAt.setAccessible(true);
        getLightEngine = findMethod(level.getClass(), "getLightEngine");
        getLightEngine.setAccessible(true);

        Class<?> levelChunk = Class.forName("net.minecraft.world.level.chunk.LevelChunk");
        Class<?> lightEngine = Class.forName("net.minecraft.world.level.lighting.LevelLightEngine");
        Class<?> packetClass =
                Class.forName("net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket");
        packetCtor = packetClass.getConstructor(levelChunk, lightEngine, BitSet.class, BitSet.class);

        Object connection = connectionField.get(serverPlayer);
        send = findMethod(connection.getClass(), "send",
                Class.forName("net.minecraft.network.protocol.Packet"));
        send.setAccessible(true);
    }

    /** Re-send the given chunk to the player. Call on the player's region thread. */
    public void resend(Player player, int chunkX, int chunkZ) {
        if (broken) {
            return;
        }
        try {
            init(player);
            Object serverPlayer = getHandle.invoke(player);
            Object level = serverLevel.invoke(serverPlayer);
            Object chunk = getChunkAt.invoke(level, chunkX, chunkZ);
            if (chunk == null) {
                return;
            }
            Object light = getLightEngine.invoke(level);
            Object packet = packetCtor.newInstance(chunk, light, null, null);
            Object connection = connectionField.get(serverPlayer);
            send.invoke(connection, packet);
        } catch (Throwable t) {
            broken = true;
            plugin.getLogger().log(Level.WARNING,
                    "Chunk re-send via NMS failed; progressive reveal disabled. "
                            + "Surface masking still works. Report your server version to fix this.", t);
        }
    }

    public boolean isBroken() {
        return broken;
    }

    private static Field findField(Class<?> type, String name) throws NoSuchFieldException {
        for (Class<?> c = type; c != null; c = c.getSuperclass()) {
            try {
                return c.getDeclaredField(name);
            } catch (NoSuchFieldException ignored) {
            }
        }
        throw new NoSuchFieldException(name + " on " + type);
    }

    private static Method findMethod(Class<?> type, String name, Class<?>... params)
            throws NoSuchMethodException {
        for (Class<?> c = type; c != null; c = c.getSuperclass()) {
            try {
                return c.getDeclaredMethod(name, params);
            } catch (NoSuchMethodException ignored) {
            }
        }
        throw new NoSuchMethodException(name + " on " + type);
    }
}

package dev.opinion2nd.antiespguard.paper.mask;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
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
        // The packet constructor's signature varies between Minecraft versions
        // (4-arg, 5-arg with a trailing boolean, …). Pick any constructor whose
        // first two parameters take a LevelChunk + LevelLightEngine and fill the
        // rest with defaults, so a single jar keeps working as versions change.
        packetCtor = chooseChunkPacketCtor(packetClass, levelChunk, lightEngine);

        Object connection = connectionField.get(serverPlayer);
        send = findMethod(connection.getClass(), "send",
                Class.forName("net.minecraft.network.protocol.Packet"));
        send.setAccessible(true);

        plugin.getLogger().info("Progressive reveal ready (chunk re-send via "
                + packetCtor.getDeclaringClass().getSimpleName()
                + " with " + packetCtor.getParameterCount() + " args).");
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
            Object packet = packetCtor.newInstance(buildPacketArgs(packetCtor, chunk, light));
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

    /**
     * Pick a {@code ClientboundLevelChunkWithLightPacket} constructor whose first
     * two parameters accept a LevelChunk and a LevelLightEngine. Preference is
     * given to the shortest such signature, so trailing version-specific
     * parameters are kept to a minimum.
     */
    private static Constructor<?> chooseChunkPacketCtor(
            Class<?> packetClass, Class<?> levelChunk, Class<?> lightEngine) throws NoSuchMethodException {
        Constructor<?> best = null;
        for (Constructor<?> ctor : packetClass.getConstructors()) {
            Class<?>[] p = ctor.getParameterTypes();
            if (p.length >= 2
                    && p[0].isAssignableFrom(levelChunk)
                    && p[1].isAssignableFrom(lightEngine)) {
                if (best == null || p.length < best.getParameterCount()) {
                    best = ctor;
                }
            }
        }
        if (best == null) {
            throw new NoSuchMethodException(
                    "No (LevelChunk, LevelLightEngine, …) constructor on " + packetClass);
        }
        return best;
    }

    /** chunk + light first, then a sensible default for every trailing parameter. */
    private static Object[] buildPacketArgs(Constructor<?> ctor, Object chunk, Object light) {
        Class<?>[] types = ctor.getParameterTypes();
        Object[] args = new Object[types.length];
        args[0] = chunk;
        args[1] = light;
        for (int i = 2; i < types.length; i++) {
            args[i] = defaultValue(types[i]);
        }
        return args;
    }

    private static Object defaultValue(Class<?> type) {
        if (!type.isPrimitive()) {
            return null; // BitSet skyLight / blockLight etc.
        }
        if (type == boolean.class) {
            return Boolean.FALSE;
        }
        if (type == int.class || type == short.class || type == byte.class) {
            return 0;
        }
        if (type == long.class) {
            return 0L;
        }
        if (type == float.class) {
            return 0f;
        }
        if (type == double.class) {
            return 0d;
        }
        return null;
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

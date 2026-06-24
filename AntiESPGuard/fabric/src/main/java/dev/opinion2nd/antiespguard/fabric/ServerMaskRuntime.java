package dev.opinion2nd.antiespguard.fabric;

import dev.opinion2nd.antiespguard.common.AntiEspConfig;
import dev.opinion2nd.antiespguard.common.MaskRules;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Process-wide singleton holding the live config and per-player mask state.
 * The mixins consult this to decide whether to mask a chunk/entity for a given
 * recipient. Equivalent to the Paper {@code MaskService}.
 */
public final class ServerMaskRuntime {

    private static final ServerMaskRuntime INSTANCE = new ServerMaskRuntime();

    private volatile AntiEspConfig config = new AntiEspConfig();
    private volatile MaskRules rules = new MaskRules(config);
    private final Map<UUID, PlayerMaskState> players = new ConcurrentHashMap<>();

    private ServerMaskRuntime() {
    }

    public static ServerMaskRuntime get() {
        return INSTANCE;
    }

    public void setConfig(AntiEspConfig config) {
        this.config = config;
        this.rules = new MaskRules(config);
    }

    public AntiEspConfig config() {
        return config;
    }

    public MaskRules rules() {
        return rules;
    }

    public PlayerMaskState state(UUID uuid) {
        return players.get(uuid);
    }

    public PlayerMaskState getOrCreate(UUID uuid) {
        return players.computeIfAbsent(uuid, k -> new PlayerMaskState());
    }

    public void remove(UUID uuid) {
        players.remove(uuid);
    }

    /** Dimension key ("NORMAL"/"NETHER"/"THE_END") for the shared rules. */
    public static String environmentOf(Level level) {
        if (level.dimension() == Level.NETHER) {
            return "NETHER";
        }
        if (level.dimension() == Level.END) {
            return "THE_END";
        }
        return "NORMAL";
    }

    /** Mirror of Paper MaskService.shouldMaskChunk. */
    public boolean shouldMaskChunk(ServerPlayer player, int cx, int cz) {
        PlayerMaskState data = players.get(player.getUUID());
        if (data == null || data.bypass || !data.worldActive) {
            return false;
        }
        if (!data.underground) {
            return true;
        }
        return !data.revealedChunks.contains(PlayerMaskState.chunkKey(cx, cz));
    }
}

package dev.opinion2nd.antiespguard.neoforge;

import dev.opinion2nd.antiespguard.common.AntiEspConfig;
import dev.opinion2nd.antiespguard.common.MaskRules;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashSet;
import java.util.Set;

/**
 * NeoForge (dedicated-server) entrypoint for AntiESPGuard. Loads the shared
 * {@code config.yml}, tracks surface/underground transitions each tick, and
 * re-sends chunks across the boundary; masking itself is applied per-recipient
 * by the {@code ServerCommonPacketListenerSendMixin}.
 */
@Mod("antiespguard")
public final class AntiEspGuardNeoForge {

    public static final Logger LOGGER = LoggerFactory.getLogger("AntiESPGuard");

    public AntiEspGuardNeoForge() {
        loadConfig();
        NeoForge.EVENT_BUS.register(this);
        LOGGER.info("AntiESPGuard (NeoForge) initialised; hideBelowY={}",
                ServerMaskRuntime.get().config().hideBelowY);
    }

    @SubscribeEvent
    public void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        PlayerMaskState data = ServerMaskRuntime.get().getOrCreate(player.getUUID());
        data.bypass = player.hasPermissions(2);
        refresh(player, data, true);
    }

    @SubscribeEvent
    public void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        ServerMaskRuntime.get().remove(event.getEntity().getUUID());
    }

    @SubscribeEvent
    public void onServerTick(ServerTickEvent.Post event) {
        for (ServerPlayer player : event.getServer().getPlayerList().getPlayers()) {
            tickPlayer(player);
        }
    }

    private void tickPlayer(ServerPlayer player) {
        PlayerMaskState data = ServerMaskRuntime.get().state(player.getUUID());
        if (data == null) {
            return;
        }
        MaskRules rules = ServerMaskRuntime.get().rules();
        boolean nowUnder = player.getY() < ServerMaskRuntime.get().config().revealBelowYWhenUnder;
        boolean stateFlip = nowUnder != data.underground;

        int rescan = rules.rescanThreshold(player.isFallFlying());
        boolean movedEnough = Double.isNaN(data.lastScanX)
                || Math.abs(player.getX() - data.lastScanX) >= rescan
                || Math.abs(player.getZ() - data.lastScanZ) >= rescan;

        if (stateFlip || movedEnough) {
            refresh(player, data, stateFlip);
        }
    }

    private void refresh(ServerPlayer player, PlayerMaskState data, boolean force) {
        AntiEspConfig cfg = ServerMaskRuntime.get().config();
        MaskRules rules = ServerMaskRuntime.get().rules();
        ServerLevel level = player.serverLevel();

        data.worldActive = rules.worldActive(level.dimension().location().toString(),
                ServerMaskRuntime.environmentOf(level));
        data.underground = player.getY() < cfg.revealBelowYWhenUnder;
        data.lastScanX = player.getX();
        data.lastScanZ = player.getZ();

        Set<Long> previous = new HashSet<>(data.revealedChunks);
        Set<Long> desired = new HashSet<>();
        if (data.worldActive && !data.bypass && data.underground) {
            int radius = revealRadius(player, cfg, rules);
            int pcx = player.getBlockX() >> 4;
            int pcz = player.getBlockZ() >> 4;
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    desired.add(PlayerMaskState.chunkKey(pcx + dx, pcz + dz));
                }
            }
        }
        if (desired.equals(previous) && !force) {
            return;
        }
        data.revealedChunks.clear();
        data.revealedChunks.addAll(desired);

        Set<Long> toResend = new HashSet<>(previous);
        toResend.addAll(desired);
        if (cfg.remaskOnReturn || !desired.isEmpty()) {
            for (long key : toResend) {
                if (previous.contains(key) != desired.contains(key)) {
                    resend(player, level, (int) (key & 0xFFFFFFFFL), (int) (key >> 32));
                }
            }
        }
    }

    private void resend(ServerPlayer player, ServerLevel level, int cx, int cz) {
        if (!level.hasChunk(cx, cz)) {
            return;
        }
        LevelChunk chunk = level.getChunk(cx, cz);
        player.connection.send(new ClientboundLevelChunkWithLightPacket(
                chunk, level.getLightEngine(), null, null));
    }

    private int revealRadius(ServerPlayer player, AntiEspConfig cfg, MaskRules rules) {
        boolean elytra = player.isFallFlying();
        int distBlocks = rules.revealLookahead(elytra);
        int radius = Math.max(cfg.scanRadiusChunks, (int) Math.ceil(distBlocks / 16.0));
        int view = player.server.getPlayerList().getViewDistance() + 1;
        return Math.min(radius, Math.max(cfg.scanRadiusChunks, view));
    }

    // ------------------------------------------------------------------------

    private void loadConfig() {
        Path dir = net.neoforged.fml.loading.FMLPaths.CONFIGDIR.get().resolve("antiespguard");
        Path file = dir.resolve("config.yml");
        try {
            if (!Files.exists(file)) {
                Files.createDirectories(dir);
                try (InputStream in = AntiEspGuardNeoForge.class
                        .getResourceAsStream("/antiespguard/config.yml")) {
                    if (in != null) {
                        Files.copy(in, file, StandardCopyOption.REPLACE_EXISTING);
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.warn("Could not write default config.yml; using built-in defaults.", e);
        }

        AntiEspConfig cfg;
        try (InputStream in = Files.exists(file)
                ? Files.newInputStream(file)
                : AntiEspGuardNeoForge.class.getResourceAsStream("/antiespguard/config.yml")) {
            cfg = AntiEspConfig.load(in);
        } catch (Exception e) {
            LOGGER.warn("Failed to read config.yml; using built-in defaults.", e);
            cfg = new AntiEspConfig();
        }
        for (String w : cfg.validateAndClamp()) {
            LOGGER.warn("[config] {}", w);
        }
        ServerMaskRuntime.get().setConfig(cfg);
    }
}

package dev.opinion2nd.antiespguard.fabric;

import dev.opinion2nd.antiespguard.common.AntiEspConfig;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;

/**
 * Builds a masked copy of a chunk packet for a surface player.
 *
 * <p>Strategy: Minecraft sends chunk packets on the server thread, so we can
 * temporarily rewrite the live chunk's below-Y block states to the mask block,
 * serialise a fresh {@link ClientboundLevelChunkWithLightPacket} from that
 * state, then restore the originals — all synchronously, before control returns
 * to any other server logic. No other thread observes the swap. This mirrors
 * what the Paper module does at the packet layer via PacketEvents.</p>
 */
public final class ChunkMasking {

    private ChunkMasking() {
    }

    /** Resolve the configured mask block (defaults to STONE). */
    public static BlockState maskState(AntiEspConfig cfg) {
        try {
            ResourceLocation id = ResourceLocation.parse(cfg.maskBlock.toLowerCase(java.util.Locale.ROOT));
            Block block = BuiltInRegistries.BLOCK.getOptional(id).orElse(Blocks.STONE);
            return block.defaultBlockState();
        } catch (Exception e) {
            return Blocks.STONE.defaultBlockState();
        }
    }

    /**
     * Produce a masked packet for {@code chunk} as seen by {@code player}, or
     * the original {@code packet} if nothing needed masking.
     */
    public static ClientboundLevelChunkWithLightPacket maskedPacket(
            ServerPlayer player,
            ClientboundLevelChunkWithLightPacket packet) {
        ServerLevel level = player.serverLevel();
        AntiEspConfig cfg = ServerMaskRuntime.get().config();

        int cx = packet.getX();
        int cz = packet.getZ();
        LevelChunk chunk = level.getChunk(cx, cz);

        BlockState mask = maskState(cfg);
        int minY = level.getMinBuildHeight();
        int hideBelowY = cfg.hideBelowY;

        // Snapshot + mask, build packet, restore.
        Snapshot snap = mask(chunk, minY, hideBelowY, mask, cfg.skipMaskIfAlreadyAir);
        if (snap == null) {
            return packet; // nothing below the line in this column
        }
        try {
            return new ClientboundLevelChunkWithLightPacket(
                    chunk, level.getLightEngine(), null, null);
        } finally {
            snap.restore(chunk);
        }
    }

    /** Mask every block below {@code hideBelowY}; returns a restore snapshot. */
    private static Snapshot mask(LevelChunk chunk, int minY, int hideBelowY,
                                 BlockState mask, boolean skipAir) {
        LevelChunkSection[] sections = chunk.getSections();
        Snapshot snap = new Snapshot(sections.length);
        boolean any = false;

        for (int i = 0; i < sections.length; i++) {
            LevelChunkSection section = sections[i];
            if (section == null || section.hasOnlyAir()) {
                continue;
            }
            int sectionBottom = minY + (i << 4);
            if (sectionBottom >= hideBelowY) {
                break;
            }
            BlockState[] saved = new BlockState[16 * 16 * 16];
            boolean sectionTouched = false;
            for (int y = 0; y < 16; y++) {
                if (sectionBottom + y >= hideBelowY) {
                    break;
                }
                for (int x = 0; x < 16; x++) {
                    for (int z = 0; z < 16; z++) {
                        BlockState cur = section.getBlockState(x, y, z);
                        if (skipAir && cur.isAir()) {
                            continue;
                        }
                        saved[(y << 8) | (x << 4) | z] = cur;
                        section.setBlockState(x, y, z, mask, false);
                        sectionTouched = true;
                    }
                }
            }
            if (sectionTouched) {
                snap.saved[i] = saved;
                any = true;
            }
        }
        return any ? snap : null;
    }

    /** Holds the pre-mask block states so the live chunk can be restored. */
    private static final class Snapshot {
        final BlockState[][] saved;

        Snapshot(int sections) {
            this.saved = new BlockState[sections][];
        }

        void restore(LevelChunk chunk) {
            LevelChunkSection[] sections = chunk.getSections();
            for (int i = 0; i < saved.length; i++) {
                BlockState[] sec = saved[i];
                if (sec == null) {
                    continue;
                }
                LevelChunkSection section = sections[i];
                for (int idx = 0; idx < sec.length; idx++) {
                    BlockState old = sec[idx];
                    if (old == null) {
                        continue;
                    }
                    int y = (idx >> 8) & 0xF;
                    int x = (idx >> 4) & 0xF;
                    int z = idx & 0xF;
                    section.setBlockState(x, y, z, old, false);
                }
            }
        }
    }
}

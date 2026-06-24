package dev.opinion2nd.antiespguard.neoforge;

import dev.opinion2nd.antiespguard.common.AntiEspConfig;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;

import java.util.Locale;

/**
 * Builds a masked copy of a chunk packet for a surface player by temporarily
 * rewriting the live chunk's below-Y blocks on the server thread, serialising a
 * fresh packet, then restoring the originals. See the Fabric module's
 * {@code ChunkMasking} for the rationale — the implementation is identical.
 */
public final class ChunkMasking {

    private ChunkMasking() {
    }

    public static BlockState maskState(AntiEspConfig cfg) {
        try {
            ResourceLocation id = ResourceLocation.parse(cfg.maskBlock.toLowerCase(Locale.ROOT));
            Block block = BuiltInRegistries.BLOCK.getOptional(id).orElse(Blocks.STONE);
            return block.defaultBlockState();
        } catch (Exception e) {
            return Blocks.STONE.defaultBlockState();
        }
    }

    public static ClientboundLevelChunkWithLightPacket maskedPacket(
            ServerPlayer player, ClientboundLevelChunkWithLightPacket packet) {
        ServerLevel level = player.serverLevel();
        AntiEspConfig cfg = ServerMaskRuntime.get().config();

        LevelChunk chunk = level.getChunk(packet.getX(), packet.getZ());
        BlockState mask = maskState(cfg);
        int minY = level.getMinBuildHeight();

        Snapshot snap = mask(chunk, minY, cfg.hideBelowY, mask, cfg.skipMaskIfAlreadyAir);
        if (snap == null) {
            return packet;
        }
        try {
            return new ClientboundLevelChunkWithLightPacket(chunk, level.getLightEngine(), null, null);
        } finally {
            snap.restore(chunk);
        }
    }

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
            boolean touched = false;
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
                        touched = true;
                    }
                }
            }
            if (touched) {
                snap.saved[i] = saved;
                any = true;
            }
        }
        return any ? snap : null;
    }

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
                    section.setBlockState((idx >> 4) & 0xF, (idx >> 8) & 0xF, idx & 0xF, old, false);
                }
            }
        }
    }
}

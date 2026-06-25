package dev.opinion2nd.antifreecam.mask;

import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.world.chunk.BaseChunk;
import com.github.retrooper.packetevents.protocol.world.chunk.Column;
import com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import com.github.retrooper.packetevents.protocol.world.states.type.StateType;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerChunkData;
import dev.opinion2nd.antifreecam.AfConfig;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import org.bukkit.entity.Player;

/**
 * Occlusion masking of outgoing CHUNK_DATA packets.
 *
 * <p>For every block strictly below {@code hideBelowY} a solid block is rewritten
 * to the configured opaque {@code maskBlock} (default deepslate) <em>iff it is
 * fully buried</em> — i.e. all six of its neighbours are themselves fully-occluding
 * solids. Any block that touches air, a cave, a tunnel or a transparent block is
 * left untouched, so the parts of the world a legitimate player can actually see
 * stay 100% vanilla, while ore veins hidden inside solid rock are never revealed.
 * A freecam / x-ray camera flying into the rock therefore hits solid rock and
 * finds nothing.
 *
 * <p>The fill must be an opaque solid: the server sends identical block data to a
 * player's real camera and their freecam camera, so an air fill would make the
 * buried volume see-through for everyone (not just freecam).
 */
public final class ChunkMaskListener extends PacketListenerAbstract {

    private final MaskService service;

    public ChunkMaskListener(MaskService service) {
        super(PacketListenerPriority.HIGH);
        this.service = service;
    }

    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (event.getPacketType() != PacketType.Play.Server.CHUNK_DATA) {
            return;
        }
        if (!(event.getPlayer() instanceof Player player)) {
            return;
        }
        if (!service.isActive(player)) {
            return;
        }

        AfConfig cfg = service.config();
        WrapperPlayServerChunkData wrapper = new WrapperPlayServerChunkData(event);
        Column column = wrapper.getColumn();

        int minY = player.getWorld().getMinHeight();
        int fillId = SpigotConversionUtil
                .fromBukkitBlockData(cfg.maskBlock.createBlockData())
                .getGlobalId();
        if (maskColumn(column.getChunks(), minY, cfg.hideBelowY, fillId)) {
            event.markForReEncode(true);
        }
    }

    /**
     * Hide every fully-buried solid block below {@code hideBelowY}. Two passes so
     * neighbour reads in pass&nbsp;1 always see the original geometry (writing in
     * place during the scan would corrupt later neighbour checks).
     */
    private boolean maskColumn(BaseChunk[] sections, int minY, int hideBelowY, int fillId) {
        int height = hideBelowY - minY;
        if (height <= 0) {
            return false;
        }

        boolean[] toHide = new boolean[height << 8]; // height * 16 * 16
        boolean changed = false;

        for (int s = 0; s < sections.length; s++) {
            int sectionBottom = minY + (s << 4);
            if (sectionBottom >= hideBelowY) {
                break; // this section and everything above it is fully visible
            }
            BaseChunk sec = sections[s];
            if (sec == null) {
                continue; // empty (all-air) section — nothing solid to hide
            }
            for (int ly = 0; ly < 16; ly++) {
                int worldY = sectionBottom + ly;
                if (worldY >= hideBelowY) {
                    break;
                }
                for (int x = 0; x < 16; x++) {
                    for (int z = 0; z < 16; z++) {
                        if (!occludes(sec.get(x, ly, z))) {
                            continue; // air / transparent / non-solid: not hideable
                        }
                        if (occludesAt(sections, minY, x - 1, worldY, z)
                                && occludesAt(sections, minY, x + 1, worldY, z)
                                && occludesAt(sections, minY, x, worldY - 1, z)
                                && occludesAt(sections, minY, x, worldY + 1, z)
                                && occludesAt(sections, minY, x, worldY, z - 1)
                                && occludesAt(sections, minY, x, worldY, z + 1)) {
                            toHide[index(worldY - minY, x, z)] = true;
                            changed = true;
                        }
                    }
                }
            }
        }

        if (!changed) {
            return false;
        }

        for (int s = 0; s < sections.length; s++) {
            int sectionBottom = minY + (s << 4);
            if (sectionBottom >= hideBelowY) {
                break;
            }
            BaseChunk sec = sections[s];
            if (sec == null) {
                continue;
            }
            for (int ly = 0; ly < 16; ly++) {
                int worldY = sectionBottom + ly;
                if (worldY >= hideBelowY) {
                    break;
                }
                for (int x = 0; x < 16; x++) {
                    for (int z = 0; z < 16; z++) {
                        if (toHide[index(worldY - minY, x, z)]) {
                            sec.set(x, ly, z, fillId);
                        }
                    }
                }
            }
        }
        return true;
    }

    private static int index(int yOff, int x, int z) {
        return (yOff << 8) | (x << 4) | z;
    }

    /**
     * Does the block at column-local {@code (x, worldY, z)} fully occlude vision?
     * Out-of-chunk horizontal neighbours are treated as <em>exposing</em> (so the
     * thin shell on chunk borders is revealed and never leaves a void hole in a
     * legit player's view); below the world bottom is treated as solid.
     */
    private static boolean occludesAt(BaseChunk[] sections, int minY, int x, int worldY, int z) {
        if (x < 0 || x > 15 || z < 0 || z > 15) {
            return false; // neighbour lives in an adjacent chunk we don't have
        }
        if (worldY < minY) {
            return true; // below the world: solid boundary
        }
        int sIdx = (worldY - minY) >> 4;
        if (sIdx >= sections.length) {
            return false; // above the world: air
        }
        BaseChunk sec = sections[sIdx];
        if (sec == null) {
            return false; // empty section: air
        }
        int localY = worldY - (minY + (sIdx << 4));
        return occludes(sec.get(x, localY, z));
    }

    /** A block occludes (hides what's behind it) if it is a solid, non-air block. */
    private static boolean occludes(WrappedBlockState state) {
        if (state == null) {
            return false;
        }
        StateType type = state.getType();
        return type != null && !type.isAir() && type.isSolid();
    }
}

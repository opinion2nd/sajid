package dev.opinion2nd.antifreecam.mask;

import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.world.chunk.BaseChunk;
import com.github.retrooper.packetevents.protocol.world.chunk.Column;
import com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerChunkData;
import dev.opinion2nd.antifreecam.AfConfig;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import org.bukkit.entity.Player;

/**
 * Rewrites every block strictly below {@code hideBelowY} to the configured mask
 * block in outgoing CHUNK_DATA packets, for any player/chunk the
 * {@link MaskService} says must be masked.
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

        AfConfig cfg = service.config();
        WrapperPlayServerChunkData wrapper = new WrapperPlayServerChunkData(event);
        Column column = wrapper.getColumn();

        if (!service.shouldMaskChunk(player.getUniqueId(), column.getX(), column.getZ())) {
            return;
        }

        int maskId = SpigotConversionUtil
                .fromBukkitBlockData(cfg.maskBlock.createBlockData())
                .getGlobalId();
        int minY = player.getWorld().getMinHeight();

        boolean changed = maskColumn(column, minY, cfg.hideBelowY, maskId, cfg.skipMaskIfAlreadyAir);
        if (changed) {
            event.markForReEncode(true);
        }
    }

    /** Set every block with worldY &lt; hideBelowY to {@code maskId}. */
    private boolean maskColumn(Column column, int minY, int hideBelowY, int maskId, boolean skipAir) {
        BaseChunk[] sections = column.getChunks();
        boolean changed = false;

        for (int i = 0; i < sections.length; i++) {
            BaseChunk section = sections[i];
            if (section == null) {
                continue;
            }
            int sectionBottom = minY + (i << 4);
            if (sectionBottom >= hideBelowY) {
                break; // this section and everything above it is fully visible
            }
            for (int y = 0; y < 16; y++) {
                if (sectionBottom + y >= hideBelowY) {
                    break;
                }
                for (int x = 0; x < 16; x++) {
                    for (int z = 0; z < 16; z++) {
                        if (skipAir) {
                            WrappedBlockState cur = section.get(x, y, z);
                            if (cur != null && cur.getType().isAir()) {
                                continue;
                            }
                        }
                        section.set(x, y, z, maskId);
                        changed = true;
                    }
                }
            }
        }
        return changed;
    }
}

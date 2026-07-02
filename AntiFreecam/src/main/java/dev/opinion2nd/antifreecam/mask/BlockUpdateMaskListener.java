package dev.opinion2nd.antifreecam.mask;

import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import com.github.retrooper.packetevents.util.Vector3i;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerBlockBreakAnimation;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerBlockChange;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerBlockEntityData;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerMultiBlockChange;
import dev.opinion2nd.antifreecam.AfConfig;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import org.bukkit.entity.Player;

/**
 * Closes the live-update leak: {@link ChunkMaskListener} masks blocks in
 * CHUNK_DATA, but the server keeps sending real-time block updates afterwards
 * (liquid flow, crop growth, block break/place, pistons, ...). Without this
 * listener a freecam camera below {@code hideBelowY} slowly "collects" every
 * updated block — floating lava, water, plants and the air hole left where a
 * block was broken.
 *
 * <p>Any BLOCK_CHANGE / MULTI_BLOCK_CHANGE entry below the threshold in a
 * masked chunk is rewritten to the mask block, and the side-channel packets
 * that would still reveal it (break animation, block-entity data, world
 * effects) are cancelled.
 */
public final class BlockUpdateMaskListener extends PacketListenerAbstract {

    private final MaskService service;

    public BlockUpdateMaskListener(MaskService service) {
        super(PacketListenerPriority.HIGH);
        this.service = service;
    }

    @Override
    public void onPacketSend(PacketSendEvent event) {
        Object type = event.getPacketType();
        if (type != PacketType.Play.Server.BLOCK_CHANGE
                && type != PacketType.Play.Server.MULTI_BLOCK_CHANGE
                && type != PacketType.Play.Server.BLOCK_BREAK_ANIMATION
                && type != PacketType.Play.Server.BLOCK_ENTITY_DATA
                && type != PacketType.Play.Server.EFFECT) {
            return;
        }
        if (!(event.getPlayer() instanceof Player player)) {
            return;
        }

        AfConfig cfg = service.config();

        if (type == PacketType.Play.Server.BLOCK_CHANGE) {
            WrapperPlayServerBlockChange wrapper = new WrapperPlayServerBlockChange(event);
            Vector3i pos = wrapper.getBlockPosition();
            if (!hidden(player, pos.getX(), pos.getY(), pos.getZ(), cfg)) {
                return;
            }
            int maskId = maskId(cfg);
            if (wrapper.getBlockId() == maskId) {
                return;
            }
            if (cfg.skipMaskIfAlreadyAir && isAir(wrapper.getBlockId())) {
                return;
            }
            wrapper.setBlockID(maskId);
            event.markForReEncode(true);
            return;
        }

        if (type == PacketType.Play.Server.MULTI_BLOCK_CHANGE) {
            WrapperPlayServerMultiBlockChange wrapper = new WrapperPlayServerMultiBlockChange(event);
            WrapperPlayServerMultiBlockChange.EncodedBlock[] blocks = wrapper.getBlocks();
            if (blocks == null || blocks.length == 0) {
                return;
            }
            int maskId = maskId(cfg);
            boolean changed = false;
            for (WrapperPlayServerMultiBlockChange.EncodedBlock block : blocks) {
                if (!hidden(player, block.getX(), block.getY(), block.getZ(), cfg)) {
                    continue;
                }
                if (block.getBlockId() == maskId) {
                    continue;
                }
                if (cfg.skipMaskIfAlreadyAir && isAir(block.getBlockId())) {
                    continue;
                }
                block.setBlockId(maskId);
                changed = true;
            }
            if (changed) {
                wrapper.setBlocks(blocks);
                event.markForReEncode(true);
            }
            return;
        }

        // Side channels that reveal hidden blocks: cancel outright.
        if (type == PacketType.Play.Server.BLOCK_BREAK_ANIMATION) {
            WrapperPlayServerBlockBreakAnimation wrapper = new WrapperPlayServerBlockBreakAnimation(event);
            Vector3i pos = wrapper.getBlockPosition();
            if (hidden(player, pos.getX(), pos.getY(), pos.getZ(), cfg)) {
                event.setCancelled(true);
            }
            return;
        }

        if (type == PacketType.Play.Server.BLOCK_ENTITY_DATA) {
            WrapperPlayServerBlockEntityData wrapper = new WrapperPlayServerBlockEntityData(event);
            Vector3i pos = wrapper.getPosition();
            if (hidden(player, pos.getX(), pos.getY(), pos.getZ(), cfg)) {
                event.setCancelled(true);
            }
            return;
        }

        // EFFECT: block-break particles/sounds etc. reveal the real block type.
        // PacketEvents 2.5.0 has no wrapper for this packet, so read the
        // position ourselves (int effectId, then a block position).
        try {
            Vector3i pos = new EffectPositionReader(event).position;
            if (pos != null && hidden(player, pos.getX(), pos.getY(), pos.getZ(), cfg)) {
                event.setCancelled(true);
            }
        } catch (Throwable ignored) {
            // Unknown encoding on this server version — let the packet through.
        }
    }

    /** Reads just enough of an EFFECT (world event) packet to get its position. */
    private static final class EffectPositionReader extends PacketWrapper<EffectPositionReader> {
        private Vector3i position;

        EffectPositionReader(PacketSendEvent event) {
            super(event);
        }

        @Override
        public void read() {
            readInt(); // effect / world-event id
            position = readBlockPosition();
        }
    }

    /** True if this world position must show the mask block to this viewer. */
    private boolean hidden(Player viewer, int x, int y, int z, AfConfig cfg) {
        if (y >= cfg.hideBelowY) {
            return false;
        }
        return service.shouldMaskChunk(viewer.getUniqueId(), x >> 4, z >> 4);
    }

    private int maskId(AfConfig cfg) {
        return SpigotConversionUtil
                .fromBukkitBlockData(cfg.maskBlock.createBlockData())
                .getGlobalId();
    }

    private static boolean isAir(int globalId) {
        WrappedBlockState state = WrappedBlockState.getByGlobalId(globalId);
        return state != null && state.getType().isAir();
    }
}

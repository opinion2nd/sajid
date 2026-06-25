package dev.opinion2nd.antifreecam.mask;

import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnEntity;
import dev.opinion2nd.antifreecam.AfConfig;
import org.bukkit.entity.Player;

/**
 * Optional anti entity-ESP: stops entity-spawn packets for entities below
 * {@code hideBelowY} from reaching masked players.
 *
 * <p>Off by default ({@code maskEntities: false}) — it conflicts with a fully
 * vanilla feel (it would also hide mobs in caves you are actually in) and is
 * experimental: a hidden entity reappears the next time the client receives a
 * spawn for it (e.g. it moves, or the player relogs). Enable only if you
 * specifically want to fight entity radar.
 */
public final class EntityMaskListener extends PacketListenerAbstract {

    private final MaskService service;

    public EntityMaskListener(MaskService service) {
        super(PacketListenerPriority.NORMAL);
        this.service = service;
    }

    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (event.getPacketType() != PacketType.Play.Server.SPAWN_ENTITY) {
            return;
        }
        AfConfig cfg = service.config();
        if (!cfg.maskEntities) {
            return;
        }
        if (!(event.getPlayer() instanceof Player viewer)) {
            return;
        }

        if (!service.isActive(viewer)) {
            return;
        }

        WrapperPlayServerSpawnEntity spawn = new WrapperPlayServerSpawnEntity(event);
        if (spawn.getPosition().getY() < cfg.hideBelowY) {
            event.setCancelled(true);
        }
    }
}

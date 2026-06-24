package dev.opinion2nd.antiespguard.paper.mask;

import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnEntity;
import dev.opinion2nd.antiespguard.paper.PaperConfig;
import org.bukkit.entity.Player;

/**
 * Stops entity-spawn packets for entities/players below {@code hideBelowY} from
 * reaching surface players, closing the entity-radar and combat-ESP leaks.
 *
 * <p>Since MC 1.20.2 players spawn through the generic spawn-entity packet too,
 * so a single listener covers both cases:</p>
 * <ul>
 *   <li>non-player entity below the line → gated by {@code maskEntities}</li>
 *   <li>player below the line → gated by {@code maskUndergroundPlayers}</li>
 * </ul>
 *
 * <p>Only the world entity is suppressed; the player's TAB-list entry is
 * untouched, so hidden players keep their normal name/prefix in the tab list.</p>
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
        if (!(event.getPlayer() instanceof Player viewer)) {
            return;
        }

        PaperConfig cfg = service.config();
        boolean maskEntities = cfg.raw().maskEntities;
        boolean maskPlayers = cfg.raw().maskUndergroundPlayers;
        if (!maskEntities && !maskPlayers) {
            return;
        }

        PlayerMaskData data = service.get(viewer.getUniqueId());
        if (data == null || data.bypass || !data.worldActive || data.underground) {
            return; // only surface players are protected from below-Y entities
        }

        WrapperPlayServerSpawnEntity spawn = new WrapperPlayServerSpawnEntity(event);
        if (spawn.getPosition().getY() >= cfg.raw().hideBelowY) {
            return;
        }

        boolean isPlayer = spawn.getEntityType() == EntityTypes.PLAYER;
        if (isPlayer ? maskPlayers : maskEntities) {
            event.setCancelled(true);
        }
    }
}

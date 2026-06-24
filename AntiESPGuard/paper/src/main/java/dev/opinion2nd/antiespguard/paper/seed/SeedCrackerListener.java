package dev.opinion2nd.antiespguard.paper.seed;

import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerJoinGame;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerRespawn;
import dev.opinion2nd.antiespguard.common.SeedScrambler;

import java.lang.reflect.Method;

/**
 * Anti seed-cracker: replaces the hashed world seed in the JOIN_GAME and
 * RESPAWN packets with a random value, removing the verification oracle that
 * seed-cracker mods (SeedcrackerX, …) rely on. No gameplay impact — the client
 * uses the hashed seed only for cosmetic biome-border noise.
 *
 * <p>The {@code setHashedSeed} method has moved around between protocol/library
 * versions, so it is invoked reflectively and the listener simply no-ops if the
 * field no longer exists for the running version.</p>
 */
public final class SeedCrackerListener extends PacketListenerAbstract {

    public SeedCrackerListener() {
        super(PacketListenerPriority.LOW);
    }

    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (event.getPacketType() == PacketType.Play.Server.JOIN_GAME) {
            if (scramble(new WrapperPlayServerJoinGame(event))) {
                event.markForReEncode(true);
            }
        } else if (event.getPacketType() == PacketType.Play.Server.RESPAWN) {
            if (scramble(new WrapperPlayServerRespawn(event))) {
                event.markForReEncode(true);
            }
        }
    }

    /** Reflectively call setHashedSeed(long) on the wrapper if present. */
    private boolean scramble(Object wrapper) {
        try {
            Method setter = wrapper.getClass().getMethod("setHashedSeed", long.class);
            setter.invoke(wrapper, SeedScrambler.randomSeed());
            return true;
        } catch (NoSuchMethodException e) {
            return false; // this version's packet has no hashed seed; nothing to do
        } catch (Throwable t) {
            return false;
        }
    }
}

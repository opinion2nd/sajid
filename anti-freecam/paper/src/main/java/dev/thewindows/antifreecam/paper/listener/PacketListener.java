package dev.thewindows.antifreecam.paper.listener;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import dev.thewindows.antifreecam.common.detection.FreecamDetector;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class PacketListener extends PacketAdapter {

    private final FreecamDetector detector;
    private long tick = 0;

    public PacketListener(Plugin plugin, ProtocolManager protocolManager, FreecamDetector detector) {
        super(plugin, ListenerPriority.NORMAL,
            PacketType.Play.Client.POSITION,
            PacketType.Play.Client.POSITION_LOOK,
            PacketType.Play.Client.LOOK
        );
        this.detector = detector;
    }

    @Override
    public void onPacketReceiving(PacketEvent event) {
        Player player = event.getPlayer();
        if (player == null) return;

        PacketType type = event.getPacketType();
        double x = player.getLocation().getX();
        double y = player.getLocation().getY();
        double z = player.getLocation().getZ();
        float yaw = player.getLocation().getYaw();
        float pitch = player.getLocation().getPitch();
        boolean onGround = true;

        if (type == PacketType.Play.Client.POSITION || type == PacketType.Play.Client.POSITION_LOOK) {
            x = event.getPacket().getDoubles().read(0);
            y = event.getPacket().getDoubles().read(1);
            z = event.getPacket().getDoubles().read(2);
            onGround = event.getPacket().getBooleans().read(0);
        }

        if (type == PacketType.Play.Client.POSITION_LOOK || type == PacketType.Play.Client.LOOK) {
            yaw = event.getPacket().getFloat().read(0);
            pitch = event.getPacket().getFloat().read(1);
        }

        detector.recordMovement(player.getUniqueId(), x, y, z, yaw, pitch, onGround, tick++);
    }
}

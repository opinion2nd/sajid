package dev.thewindows.antifreecam.paper.listener;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import dev.thewindows.antifreecam.common.detection.FreecamDetector;
import org.bukkit.Location;
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

        // Read the live position/look from Bukkit's own player state instead of raw packet
        // fields. ProtocolLib's structure-modifier field order for these packets can shift
        // between Minecraft versions and silently misreads x/y/z/yaw/pitch otherwise — this
        // listener only needs to know *that* and *when* a movement packet arrived; Bukkit
        // already has the correctly decoded values.
        Location loc = player.getLocation();
        detector.recordMovement(player.getUniqueId(),
            loc.getX(), loc.getY(), loc.getZ(),
            loc.getYaw(), loc.getPitch(),
            player.isOnGround(),
            tick++);
    }
}


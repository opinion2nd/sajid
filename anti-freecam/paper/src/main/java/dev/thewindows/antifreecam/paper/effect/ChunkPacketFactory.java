package dev.thewindows.antifreecam.paper.effect;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.bukkit.Bukkit;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class ChunkPacketFactory {

    // World height in Minecraft 1.18+ is -64 to 320, giving 24 sections of 16 blocks each
    private static final int SECTION_COUNT = 24;

    private final ProtocolManager protocolManager;

    public ChunkPacketFactory(ProtocolManager protocolManager) {
        this.protocolManager = protocolManager;
    }

    public PacketContainer buildEmptyChunkPacket(int chunkX, int chunkZ) {
        PacketContainer packet = protocolManager.createPacket(PacketType.Play.Server.MAP_CHUNK);

        // Chunk coordinates
        packet.getIntegers().write(0, chunkX);
        packet.getIntegers().write(1, chunkZ);

        // Empty heightmaps NBT (write empty compound tag)
        // ProtocolLib wraps NMS NBT — we write an empty compound
        // The packet structure differs by MC version; using byte array approach for compatibility
        byte[] chunkData = buildEmptyChunkData();
        packet.getByteArrays().write(0, chunkData);

        return packet;
    }

    /**
     * Builds serialised chunk section data where all blocks are AIR.
     * Format follows MC 1.18+ chunk data protocol.
     */
    private byte[] buildEmptyChunkData() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);

        try {
            for (int i = 0; i < SECTION_COUNT; i++) {
                writeEmptySection(dos);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to build empty chunk data", e);
        }

        return baos.toByteArray();
    }

    private void writeEmptySection(DataOutputStream dos) throws IOException {
        // Block count (short): 0 non-air blocks
        dos.writeShort(0);

        // Block states palette: single-value (bits = 0)
        dos.writeByte(0);       // bitsPerEntry = 0 (single value indirect)
        writeVarInt(dos, 0);    // palette value = AIR (block state id 0)
        writeVarInt(dos, 0);    // data array length = 0

        // Biomes palette: single-value
        dos.writeByte(0);       // bitsPerEntry = 0
        writeVarInt(dos, 0);    // palette value = plains (biome id 0)
        writeVarInt(dos, 0);    // data array length = 0
    }

    private void writeVarInt(DataOutputStream dos, int value) throws IOException {
        while ((value & ~0x7F) != 0) {
            dos.writeByte((value & 0x7F) | 0x80);
            value >>>= 7;
        }
        dos.writeByte(value);
    }
}

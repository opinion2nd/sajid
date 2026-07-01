package com.ultimatedungeon.config.files;

import com.ultimatedungeon.dungeon.event.DynamicEventSettings;
import com.ultimatedungeon.dungeon.hazard.HazardSettings;
import com.ultimatedungeon.room.model.RoomType;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

import java.util.EnumMap;
import java.util.Map;

/** Typed wrapper for {@code dungeon.yml}. */
public final class DungeonConfig {

    private final int  dungeonSizeMin;
    private final int  dungeonSizeMax;
    private final int  corridorLengthMin;
    private final int  corridorLengthMax;
    private final double decorationDensity;
    private final double secretRoomChance;
    private final double puzzleFrequency;
    private final double trapFrequency;
    private final double eventChance;
    private final int  maxConcurrentInstances;
    private final int  rewardRoomTimeoutSeconds;
    private final int  waveResetSeconds;
    private final Map<RoomType, Integer> roomWeights;
    private final HazardSettings hazardSettings;
    private final DynamicEventSettings dynamicEventSettings;

    public DungeonConfig(@NotNull final FileConfiguration cfg) {
        dungeonSizeMin           = cfg.getInt("generation.dungeon-size.min", 12);
        dungeonSizeMax           = cfg.getInt("generation.dungeon-size.max", 20);
        corridorLengthMin        = cfg.getInt("generation.corridor-length.min", 5);
        corridorLengthMax        = cfg.getInt("generation.corridor-length.max", 15);
        decorationDensity        = cfg.getDouble("generation.decoration-density", 0.6);
        secretRoomChance         = cfg.getDouble("generation.secret-room-chance", 0.25);
        puzzleFrequency          = cfg.getDouble("generation.puzzle-frequency", 0.3);
        trapFrequency            = cfg.getDouble("generation.trap-frequency", 0.4);
        eventChance              = cfg.getDouble("generation.event-chance", 0.2);
        maxConcurrentInstances   = cfg.getInt("max-concurrent-instances", 10);
        rewardRoomTimeoutSeconds = cfg.getInt("reward-room-timeout", 300);
        waveResetSeconds         = cfg.getInt("wave-reset-seconds", 60);

        roomWeights = new EnumMap<>(RoomType.class);
        final ConfigurationSection weightsSection = cfg.getConfigurationSection("room-weights");
        if (weightsSection != null) {
            for (final String key : weightsSection.getKeys(false)) {
                try {
                    final RoomType type = RoomType.valueOf(key.toUpperCase());
                    roomWeights.put(type, weightsSection.getInt(key, 0));
                } catch (final IllegalArgumentException ignored) {
                    // Unknown key in config — skip silently; validator warns separately.
                }
            }
        }
        // Ensure all types have a default weight if not configured.
        for (final RoomType type : RoomType.values()) {
            roomWeights.putIfAbsent(type, 0);
        }

        hazardSettings       = new HazardSettings(cfg.getConfigurationSection("hazards"));
        dynamicEventSettings = new DynamicEventSettings(cfg.getConfigurationSection("dynamic-events"));
    }

    public int    getDungeonSizeMin()            { return dungeonSizeMin; }
    public int    getDungeonSizeMax()            { return dungeonSizeMax; }
    public int    getCorridorLengthMin()         { return corridorLengthMin; }
    public int    getCorridorLengthMax()         { return corridorLengthMax; }
    public double getDecorationDensity()         { return decorationDensity; }
    public double getSecretRoomChance()          { return secretRoomChance; }
    public double getPuzzleFrequency()           { return puzzleFrequency; }
    public double getTrapFrequency()             { return trapFrequency; }
    public double getEventChance()               { return eventChance; }
    public int    getMaxConcurrentInstances()    { return maxConcurrentInstances; }
    public int    getRewardRoomTimeoutSeconds()  { return rewardRoomTimeoutSeconds; }
    public int    getWaveResetSeconds()          { return waveResetSeconds; }
    @NotNull public Map<RoomType, Integer> getRoomWeights() { return roomWeights; }
    public int    getRoomWeight(@NotNull final RoomType type) {
        return roomWeights.getOrDefault(type, 0);
    }
    @NotNull public HazardSettings       getHazardSettings()       { return hazardSettings; }
    @NotNull public DynamicEventSettings getDynamicEventSettings() { return dynamicEventSettings; }
}

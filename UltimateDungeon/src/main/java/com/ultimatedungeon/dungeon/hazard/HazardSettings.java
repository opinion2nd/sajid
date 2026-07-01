package com.ultimatedungeon.dungeon.hazard;

import com.ultimatedungeon.room.model.RoomType;
import org.bukkit.Particle;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

/**
 * Typed view of the {@code hazards} section of {@code dungeon.yml}: which room
 * types carry an ambient hazard and the profiles those hazards use.
 */
public final class HazardSettings {

    private final boolean enabled;
    private final int tickIntervalTicks;
    private final Map<RoomType, HazardProfile> roomHazards = new EnumMap<>(RoomType.class);

    public HazardSettings(@Nullable final ConfigurationSection section) {
        if (section == null) {
            this.enabled = false;
            this.tickIntervalTicks = 40;
            return;
        }
        this.enabled = section.getBoolean("enabled", true);
        this.tickIntervalTicks = Math.max(1, section.getInt("tick-interval-ticks", 40));

        final Map<String, HazardProfile> profiles = new HashMap<>();
        final ConfigurationSection profilesSec = section.getConfigurationSection("profiles");
        if (profilesSec != null) {
            for (final String id : profilesSec.getKeys(false)) {
                final ConfigurationSection p = profilesSec.getConfigurationSection(id);
                if (p != null) profiles.put(id, parseProfile(id, p));
            }
        }
        final ConfigurationSection roomSec = section.getConfigurationSection("room-hazards");
        if (roomSec != null) {
            for (final String key : roomSec.getKeys(false)) {
                final RoomType type = parseRoomType(key);
                final HazardProfile profile = profiles.get(roomSec.getString(key));
                if (type != null && profile != null) roomHazards.put(type, profile);
            }
        }
    }

    @NotNull
    private HazardProfile parseProfile(@NotNull final String id, @NotNull final ConfigurationSection p) {
        return new HazardProfile(
                id,
                p.getDouble("damage", 0.0),
                resolveEffect(p.getString("potion-effect")),
                p.getInt("effect-duration-ticks", 60),
                p.getInt("effect-amplifier", 0),
                resolveParticle(p.getString("particle")));
    }

    @Nullable
    private static PotionEffectType resolveEffect(@Nullable final String name) {
        if (name == null || name.isBlank()) return null;
        return PotionEffectType.getByName(name.toUpperCase());
    }

    @Nullable
    private static Particle resolveParticle(@Nullable final String name) {
        if (name == null || name.isBlank()) return null;
        try {
            return Particle.valueOf(name.toUpperCase());
        } catch (final IllegalArgumentException ex) {
            return null;
        }
    }

    @Nullable
    private static RoomType parseRoomType(@NotNull final String key) {
        try {
            return RoomType.valueOf(key.toUpperCase());
        } catch (final IllegalArgumentException ex) {
            return null;
        }
    }

    public boolean isEnabled()          { return enabled; }
    public int getTickIntervalTicks()   { return tickIntervalTicks; }

    /** @return the hazard for a room type, or {@code null} if that type is not hazardous. */
    @Nullable
    public HazardProfile forRoomType(@NotNull final RoomType type) {
        return roomHazards.get(type);
    }

    public boolean hasAnyHazards() { return !roomHazards.isEmpty(); }
}

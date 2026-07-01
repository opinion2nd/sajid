package com.ultimatedungeon.dungeon.event;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/** Typed view of the {@code dynamic-events} section of {@code dungeon.yml}. */
public final class DynamicEventSettings {

    private final boolean enabled;
    private final List<DynamicEventSpec> events = new ArrayList<>();
    private final int totalWeight;

    public DynamicEventSettings(@Nullable final ConfigurationSection section) {
        if (section == null) {
            this.enabled = false;
            this.totalWeight = 0;
            return;
        }
        this.enabled = section.getBoolean("enabled", true);
        final ConfigurationSection eventsSec = section.getConfigurationSection("events");
        int weightSum = 0;
        if (eventsSec != null) {
            for (final String id : eventsSec.getKeys(false)) {
                final ConfigurationSection e = eventsSec.getConfigurationSection(id);
                if (e == null) continue;
                final DynamicEventSpec spec = parse(id, e);
                if (spec == null || spec.weight() <= 0) continue;
                events.add(spec);
                weightSum += spec.weight();
            }
        }
        this.totalWeight = weightSum;
    }

    @Nullable
    private DynamicEventSpec parse(@NotNull final String id, @NotNull final ConfigurationSection e) {
        final DynamicEventSpec.DynamicEventKind kind;
        try {
            kind = DynamicEventSpec.DynamicEventKind.valueOf(e.getString("type", "").toUpperCase());
        } catch (final IllegalArgumentException ex) {
            return null;
        }
        final List<PotionEffectType> effects = new ArrayList<>();
        for (final String name : e.getStringList("potion-effects")) {
            final PotionEffectType type = PotionEffectType.getByName(name.toUpperCase());
            if (type != null) effects.add(type);
        }
        return new DynamicEventSpec(
                id,
                kind,
                e.getInt("weight", 0),
                e.getInt("extra-waves", 1),
                e.getInt("per-wave", 4),
                effects,
                e.getInt("effect-duration-ticks", 400),
                e.getInt("effect-amplifier", 0),
                e.getString("loot-table"));
    }

    public boolean isEnabled()                    { return enabled; }
    public boolean hasEvents()                    { return !events.isEmpty(); }
    public int getTotalWeight()                   { return totalWeight; }
    @NotNull public List<DynamicEventSpec> getEvents() { return events; }
}

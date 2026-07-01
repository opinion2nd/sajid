package com.ultimatedungeon.monster.model;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Immutable monster definition parsed from a {@code monsters.yml} section.
 *
 * <p>Every stat (health, damage, speed), the spawn weight, equipment layout,
 * ability list and drop table is configurable so no value is hard-coded.</p>
 */
public final class MonsterDefinition {

    /** Equipment slot keys understood by the spawner. */
    public enum Slot { HELMET, CHESTPLATE, LEGGINGS, BOOTS, HAND, OFFHAND }

    /** A configurable monster ability slot. */
    public record AbilitySpec(@NotNull String id, long cooldownTicks, double damage, double range) {}

    /** A weighted drop entry. */
    public record DropSpec(@NotNull Material item, double chance, int min, int max) {}

    private final String id;
    private final String displayName;
    private final MonsterCategory category;
    private final double health;
    private final double damage;
    private final double speed;
    private final int spawnWeight;
    private final List<AbilitySpec> abilities;
    private final Map<Slot, Material> equipment;
    private final List<DropSpec> drops;

    private MonsterDefinition(final String id, final String displayName, final MonsterCategory category,
                              final double health, final double damage, final double speed, final int spawnWeight,
                              final List<AbilitySpec> abilities, final Map<Slot, Material> equipment,
                              final List<DropSpec> drops) {
        this.id = id;
        this.displayName = displayName;
        this.category = category;
        this.health = health;
        this.damage = damage;
        this.speed = speed;
        this.spawnWeight = spawnWeight;
        this.abilities = abilities;
        this.equipment = equipment;
        this.drops = drops;
    }

    @NotNull
    public static MonsterDefinition fromSection(@NotNull final String id, @NotNull final ConfigurationSection s) {
        MonsterCategory cat;
        try {
            cat = MonsterCategory.valueOf(s.getString("category", "COMMON").toUpperCase());
        } catch (final IllegalArgumentException ex) {
            cat = MonsterCategory.COMMON;
        }

        final List<AbilitySpec> abilities = new ArrayList<>();
        for (final Map<?, ?> m : s.getMapList("abilities")) {
            final Object aid = m.get("id");
            if (aid == null) continue;
            abilities.add(new AbilitySpec(aid.toString(),
                    toLong(m.get("cooldown-ticks"), 60L),
                    toDouble(m.get("damage"), 2.0),
                    toDouble(m.get("range"), 0.0)));
        }

        final Map<Slot, Material> equipment = new EnumMap<>(Slot.class);
        final ConfigurationSection eq = s.getConfigurationSection("equipment");
        if (eq != null) {
            for (final String key : eq.getKeys(false)) {
                final Slot slot = matchSlot(key);
                final Material mat = Material.matchMaterial(eq.getString(key, "").toUpperCase());
                if (slot != null && mat != null) equipment.put(slot, mat);
            }
        }

        final List<DropSpec> drops = new ArrayList<>();
        for (final Map<?, ?> m : s.getMapList("drops")) {
            final Object item = m.get("item");
            if (item == null) continue;
            final Material mat = Material.matchMaterial(item.toString().toUpperCase());
            if (mat == null) continue;
            drops.add(new DropSpec(mat,
                    toDouble(m.get("chance"), 0.1),
                    (int) toLong(m.get("min"), 1L),
                    (int) toLong(m.get("max"), 1L)));
        }

        return new MonsterDefinition(id,
                s.getString("display-name", id), cat,
                s.getDouble("health", 20.0),
                s.getDouble("damage", 3.0),
                s.getDouble("speed", 0.25),
                s.getInt("spawn-weight", 10),
                abilities, equipment, drops);
    }

    private static Slot matchSlot(final String key) {
        try {
            return Slot.valueOf(key.toUpperCase());
        } catch (final IllegalArgumentException ex) {
            return null;
        }
    }

    private static long toLong(final Object o, final long def) {
        return o instanceof Number n ? n.longValue() : def;
    }

    private static double toDouble(final Object o, final double def) {
        return o instanceof Number n ? n.doubleValue() : def;
    }

    @NotNull public String getId() { return id; }
    @NotNull public String getDisplayName() { return displayName; }
    @NotNull public MonsterCategory getCategory() { return category; }
    public double getHealth() { return health; }
    public double getDamage() { return damage; }
    public double getSpeed() { return speed; }
    public int getSpawnWeight() { return spawnWeight; }
    @NotNull public List<AbilitySpec> getAbilities() { return abilities; }
    @NotNull public Map<Slot, Material> getEquipment() { return equipment; }
    @NotNull public List<DropSpec> getDrops() { return drops; }
}

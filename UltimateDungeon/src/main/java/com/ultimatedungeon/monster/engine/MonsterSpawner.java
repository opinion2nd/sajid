package com.ultimatedungeon.monster.engine;

import com.ultimatedungeon.UltimateDungeon;
import com.ultimatedungeon.core.PluginLogger;
import com.ultimatedungeon.monster.model.MonsterCategory;
import com.ultimatedungeon.monster.model.MonsterDefinition;
import com.ultimatedungeon.util.MiniMessageUtil;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * Spawns a configured dungeon monster into the world.
 *
 * <p>Each spawned entity is tagged in its persistent data container with the
 * monster id and its scaled damage so listeners can identify dungeon monsters,
 * award kills and apply the correct hit damage. Health is scaled per difficulty;
 * the entity is named, equipped and prevented from despawning.</p>
 */
public final class MonsterSpawner {

    private final PluginLogger logger;
    private final NamespacedKey idKey;
    private final NamespacedKey damageKey;

    public MonsterSpawner(@NotNull final UltimateDungeon plugin, @NotNull final PluginLogger logger) {
        this.logger = logger;
        this.idKey = new NamespacedKey(plugin, "ud_monster_id");
        this.damageKey = new NamespacedKey(plugin, "ud_monster_damage");
    }

    @NotNull public NamespacedKey idKey() { return idKey; }
    @NotNull public NamespacedKey damageKey() { return damageKey; }

    /**
     * Spawns a monster at {@code location}.
     *
     * @return the spawned living entity, or {@code null} if the world rejected it
     */
    @Nullable
    public LivingEntity spawn(@NotNull final MonsterDefinition def,
                              @NotNull final MonsterScaler.Scaled stats,
                              @NotNull final Location location) {
        if (location.getWorld() == null) return null;
        final Entity entity = location.getWorld().spawnEntity(location, entityType(def.getCategory()));
        if (!(entity instanceof final LivingEntity living)) {
            entity.remove();
            return null;
        }
        living.setRemoveWhenFarAway(false);
        living.customName(MiniMessageUtil.parse(def.getDisplayName()));
        living.setCustomNameVisible(true);
        applyHealth(living, stats.health());
        applyEquipment(living, def.getEquipment());
        living.getPersistentDataContainer().set(idKey, PersistentDataType.STRING, def.getId());
        living.getPersistentDataContainer().set(damageKey, PersistentDataType.DOUBLE, stats.damage());
        return living;
    }

    @SuppressWarnings("deprecation")
    private void applyHealth(@NotNull final LivingEntity living, final double health) {
        try {
            living.setMaxHealth(health);
            living.setHealth(health);
        } catch (final IllegalArgumentException ex) {
            logger.debug("Could not apply health " + health + " to monster: " + ex.getMessage());
        }
    }

    private void applyEquipment(@NotNull final LivingEntity living,
                                @NotNull final Map<MonsterDefinition.Slot, org.bukkit.Material> equipment) {
        final EntityEquipment eq = living.getEquipment();
        if (eq == null) return;
        equipment.forEach((slot, material) -> {
            final ItemStack item = new ItemStack(material);
            switch (slot) {
                case HELMET -> eq.setHelmet(item);
                case CHESTPLATE -> eq.setChestplate(item);
                case LEGGINGS -> eq.setLeggings(item);
                case BOOTS -> eq.setBoots(item);
                case HAND -> eq.setItemInMainHand(item);
                case OFFHAND -> eq.setItemInOffHand(item);
            }
        });
    }

    @NotNull
    private EntityType entityType(@NotNull final MonsterCategory category) {
        return switch (category) {
            case COMMON -> EntityType.ZOMBIE;
            case ELITE -> EntityType.ZOMBIE_VILLAGER;
            case RARE -> EntityType.WITHER_SKELETON;
            case MINI_BOSS -> EntityType.PIGLIN_BRUTE;
            case BOSS_SUMMON -> EntityType.VEX;
            case ENVIRONMENTAL -> EntityType.SILVERFISH;
            case EVENT -> EntityType.SKELETON;
        };
    }
}

package dev.opinion2nd.customboss;

import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Tracks every live {@link CustomBoss} and handles spawning / removal. */
public final class BossManager {

    private final CustomBossPlugin plugin;
    private final Map<UUID, CustomBoss> bosses = new HashMap<>();

    public BossManager(CustomBossPlugin plugin) {
        this.plugin = plugin;
    }

    /** Spawns a boss using the given settings, or null if the type can't live. */
    public CustomBoss spawn(BossSettings settings, Location location) {
        if (location.getWorld() == null) {
            return null;
        }
        Entity spawned = location.getWorld().spawnEntity(location, settings.entityType);
        if (!(spawned instanceof LivingEntity living)) {
            spawned.remove();
            return null;
        }

        living.setCustomName(settings.displayName);
        living.setCustomNameVisible(true);
        living.setRemoveWhenFarAway(false);
        living.setPersistent(true);

        AttributeInstance maxHealth = living.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (maxHealth != null) {
            maxHealth.setBaseValue(settings.health);
            living.setHealth(settings.health);
        }
        AttributeInstance attack = living.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE);
        if (attack != null) {
            attack.setBaseValue(settings.damage);
        }
        AttributeInstance speed = living.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);
        if (speed != null && settings.movementSpeed > 0) {
            speed.setBaseValue(settings.movementSpeed);
        }

        living.getPersistentDataContainer().set(plugin.getBossKey(), PersistentDataType.BYTE, (byte) 1);
        if (living instanceof Mob mob) {
            mob.setRemoveWhenFarAway(false);
        }

        CustomBoss boss = new CustomBoss(plugin, settings, living);
        bosses.put(living.getUniqueId(), boss);
        boss.start();
        return boss;
    }

    public boolean isBoss(Entity entity) {
        return entity != null && entity.getPersistentDataContainer()
                .has(plugin.getBossKey(), PersistentDataType.BYTE);
    }

    public CustomBoss get(UUID entityId) {
        return bosses.get(entityId);
    }

    public void remove(UUID entityId, boolean killEntity) {
        CustomBoss boss = bosses.remove(entityId);
        if (boss != null) {
            boss.despawn(killEntity);
        }
    }

    public int killAll() {
        int count = bosses.size();
        for (CustomBoss boss : new ArrayList<>(bosses.values())) {
            boss.despawn(true);
        }
        bosses.clear();
        return count;
    }

    public void removeAll() {
        for (CustomBoss boss : new ArrayList<>(bosses.values())) {
            boss.despawn(false);
        }
        bosses.clear();
    }

    public Collection<CustomBoss> all() {
        return bosses.values();
    }

    public int count() {
        return bosses.size();
    }
}

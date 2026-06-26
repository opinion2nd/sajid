package dev.opinion2nd.customboss;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;

/**
 * A live boss instance: owns the bound entity, its boss bar and the repeating
 * task that drives the boss bar updates and the special attacks.
 */
public final class CustomBoss {

    /** Players within this range see the boss bar and can be targeted. */
    private static final double TRACK_RANGE = 60.0;

    private final CustomBossPlugin plugin;
    private final BossSettings settings;
    private final LivingEntity entity;
    private final BossBar bossBar;

    private BukkitRunnable task;
    private int tickAccumulator;
    private boolean enraged;

    public CustomBoss(CustomBossPlugin plugin, BossSettings settings, LivingEntity entity) {
        this.plugin = plugin;
        this.settings = settings;
        this.entity = entity;
        this.bossBar = Bukkit.createBossBar(settings.displayName, settings.barColor, settings.barStyle);
        this.bossBar.setProgress(1.0);
    }

    public void start() {
        this.task = new BukkitRunnable() {
            @Override
            public void run() {
                tick();
            }
        };
        // Runs once per second; abilities fire on the configured interval.
        task.runTaskTimer(plugin, 20L, 20L);
    }

    private void tick() {
        if (entity.isDead() || !entity.isValid()) {
            plugin.getBossManager().remove(getEntityId(), false);
            return;
        }
        updateBossBar();
        maybeEnrage();

        tickAccumulator += 20;
        if (tickAccumulator >= settings.abilityIntervalTicks) {
            tickAccumulator = 0;
            useRandomAbility();
        }
    }

    /** Refreshes the bar progress and the set of players that can see it. */
    public void updateBossBar() {
        double max = maxHealth();
        bossBar.setProgress(Math.max(0.0, Math.min(1.0, entity.getHealth() / max)));

        List<Player> nearby = nearbyPlayers(TRACK_RANGE);
        for (Player current : new ArrayList<>(bossBar.getPlayers())) {
            if (!nearby.contains(current)) {
                bossBar.removePlayer(current);
            }
        }
        for (Player player : nearby) {
            if (!bossBar.getPlayers().contains(player)) {
                bossBar.addPlayer(player);
            }
        }
    }

    private void maybeEnrage() {
        if (!settings.enrageEnabled || enraged) {
            return;
        }
        if (entity.getHealth() / maxHealth() > settings.enrageThreshold) {
            return;
        }
        enraged = true;
        AttributeInstance speed = entity.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);
        if (speed != null) {
            speed.setBaseValue(speed.getBaseValue() * settings.enrageSpeedMultiplier);
        }
        entity.getWorld().spawnParticle(Particle.ANGRY_VILLAGER, entity.getLocation().add(0, 2, 0), 30, 0.6, 0.6, 0.6);
        announce("&4The boss is enraged!");
    }

    // ----- abilities ------------------------------------------------------

    private void useRandomAbility() {
        List<Consumer<Player>> pool = new ArrayList<>();
        if (settings.fireballEnabled) {
            pool.add(this::fireballAttack);
        }
        if (settings.aoeEnabled) {
            pool.add(target -> aoeKnockback());
        }
        if (settings.summonEnabled) {
            pool.add(this::summonMinions);
        }
        if (settings.teleportEnabled) {
            pool.add(this::teleport);
        }
        if (pool.isEmpty()) {
            return;
        }
        Player target = nearestPlayer();
        if (target == null) {
            return;
        }
        pool.get(ThreadLocalRandom.current().nextInt(pool.size())).accept(target);
    }

    private void fireballAttack(Player target) {
        Vector direction = target.getEyeLocation().toVector()
                .subtract(entity.getEyeLocation().toVector());
        if (direction.lengthSquared() < 1.0e-4) {
            return;
        }
        direction.normalize().multiply(1.4);
        Fireball fireball = entity.launchProjectile(Fireball.class, direction);
        fireball.setYield(2.0f);
        fireball.setIsIncendiary(true);
        entity.getWorld().playSound(entity.getLocation(), Sound.ENTITY_GHAST_SHOOT, 1.0f, 1.0f);
        announce("&6The boss hurls a fireball!");
    }

    private void summonMinions(Player target) {
        Location base = entity.getLocation();
        for (int i = 0; i < settings.minionCount; i++) {
            Location loc = base.clone().add(rand(-3.0, 3.0), 0.0, rand(-3.0, 3.0));
            org.bukkit.entity.Entity minion = entity.getWorld().spawnEntity(loc, settings.minionType);
            minion.getPersistentDataContainer().set(plugin.getMinionKey(), PersistentDataType.BYTE, (byte) 1);
            if (minion instanceof Mob mob) {
                mob.setTarget(target);
            }
        }
        announce("&5The boss summons minions!");
    }

    private void aoeKnockback() {
        Location center = entity.getLocation();
        for (Player player : nearbyPlayers(settings.aoeRadius)) {
            Vector push = player.getLocation().toVector().subtract(center.toVector());
            if (push.lengthSquared() < 1.0e-4) {
                push = new Vector(0, 1, 0);
            }
            push.normalize().multiply(settings.aoeStrength);
            push.setY(0.6);
            player.setVelocity(push);
            if (settings.aoeDamage > 0) {
                player.damage(settings.aoeDamage, entity);
            }
        }
        entity.getWorld().spawnParticle(Particle.EXPLOSION, center, 5, 1.5, 0.5, 1.5);
        entity.getWorld().playSound(center, Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 1.0f);
        announce("&cThe boss unleashes a shockwave!");
    }

    private void teleport(Player target) {
        entity.getWorld().spawnParticle(Particle.PORTAL, entity.getLocation(), 40, 0.5, 1.0, 0.5);
        entity.teleport(target.getLocation());
        entity.getWorld().playSound(target.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 0.8f);
        announce("&dThe boss teleports!");
    }

    // ----- lifecycle ------------------------------------------------------

    public void despawn(boolean killEntity) {
        if (task != null) {
            task.cancel();
            task = null;
        }
        bossBar.removeAll();
        if (killEntity && entity.isValid()) {
            entity.remove();
        }
    }

    // ----- helpers --------------------------------------------------------

    private double maxHealth() {
        AttributeInstance attr = entity.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        return attr != null ? attr.getValue() : settings.health;
    }

    private List<Player> nearbyPlayers(double range) {
        List<Player> players = new ArrayList<>();
        Location loc = entity.getLocation();
        for (Player player : entity.getWorld().getPlayers()) {
            if (player.getGameMode() == org.bukkit.GameMode.SPECTATOR) {
                continue;
            }
            if (player.getLocation().distanceSquared(loc) <= range * range) {
                players.add(player);
            }
        }
        return players;
    }

    private Player nearestPlayer() {
        Player nearest = null;
        double best = Double.MAX_VALUE;
        Location loc = entity.getLocation();
        for (Player player : nearbyPlayers(TRACK_RANGE)) {
            double d = player.getLocation().distanceSquared(loc);
            if (d < best) {
                best = d;
                nearest = player;
            }
        }
        return nearest;
    }

    private void announce(String message) {
        String formatted = org.bukkit.ChatColor.translateAlternateColorCodes('&', message);
        for (Player player : bossBar.getPlayers()) {
            player.sendActionBar(formatted);
        }
    }

    private static double rand(double min, double max) {
        return ThreadLocalRandom.current().nextDouble(min, max);
    }

    public LivingEntity getEntity() {
        return entity;
    }

    public UUID getEntityId() {
        return entity.getUniqueId();
    }

    public BossSettings getSettings() {
        return settings;
    }

    public BossBar getBossBar() {
        return bossBar;
    }
}

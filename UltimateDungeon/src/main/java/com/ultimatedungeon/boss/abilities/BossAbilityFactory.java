package com.ultimatedungeon.boss.abilities;

import com.ultimatedungeon.api.boss.IBossAbility;
import com.ultimatedungeon.boss.model.BossDefinition;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.ShulkerBullet;
import org.bukkit.entity.SmallFireball;
import org.bukkit.entity.SpectralArrow;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Creates the signature ability for each configured ability id.
 *
 * <p>Every boss owns four powers and no two bosses share a power — all 28
 * behaviours below are distinct. Damage, cooldown and range come from
 * {@code bosses.yml}, so server owners can still tune every number.</p>
 *
 * <h3>Roster</h3>
 * <ul>
 *   <li><b>Tharok</b> — seismic_slam, boulder_toss, stone_bulwark, earthen_grasp</li>
 *   <li><b>Sylvara</b> — thorn_volley, strangling_vines, spore_cloud, wild_regrowth</li>
 *   <li><b>Boreas</b> — glacial_lance, frost_nova, hailstorm, permafrost_field</li>
 *   <li><b>Aethon</b> — thunder_strike, gale_burst, tempest_shield, storm_orbs</li>
 *   <li><b>Zharok</b> — void_fangs, mind_fracture, rift_pull, null_curse</li>
 *   <li><b>Vulkhan</b> — magma_eruption, slag_volley, forge_summon, molten_rush</li>
 *   <li><b>Nyxara</b> — soul_reap, wraith_summon, life_siphon, dread_toll</li>
 * </ul>
 */
public final class BossAbilityFactory {

    /** The concrete behaviour of one unique ability. */
    @FunctionalInterface
    private interface Effect {
        void run(@NotNull LivingEntity boss, @NotNull UniqueAbility ability);
    }

    /** Ability wrapper that delegates its effect to a lambda from the registry. */
    private static final class UniqueAbility extends AbstractBossAbility {
        private final Effect effect;

        UniqueAbility(final String id, final double damage, final long cooldownTicks,
                      final double range, final Effect effect) {
            super(id, damage, cooldownTicks, range);
            this.effect = effect;
        }

        @Override
        protected void perform(@NotNull final LivingEntity boss) {
            if (boss.getWorld() == null) return;
            effect.run(boss, this);
        }

        double damage()          { return damage; }
        double range()           { return range;  }
        List<Player> players(@NotNull final LivingEntity boss) { return nearbyPlayers(boss); }
        @Nullable Player nearest(@NotNull final LivingEntity boss) { return nearestPlayer(boss); }
    }

    private static final Map<String, Effect> EFFECTS = new HashMap<>();

    private BossAbilityFactory() {}

    /**
     * Builds the unique ability registered for {@code spec.id()}, or
     * {@code null} if the id is unknown (caller falls back to a generic slot).
     */
    @Nullable
    public static IBossAbility create(@NotNull final BossDefinition.AbilitySpec spec) {
        final Effect effect = EFFECTS.get(spec.id());
        if (effect == null) return null;
        final double range = spec.range() > 0 ? spec.range() : 8.0;
        return new UniqueAbility(spec.id(), spec.damage(), spec.cooldownTicks(), range, effect);
    }

    /** True if an ability id has a unique behaviour registered. */
    public static boolean isUnique(@NotNull final String abilityId) {
        return EFFECTS.containsKey(abilityId);
    }

    // ── Shared helpers ────────────────────────────────────────────────────────

    private static void sound(final LivingEntity boss, final Sound sound,
                              final float volume, final float pitch) {
        boss.getWorld().playSound(boss.getLocation(), sound, volume, pitch);
    }

    private static void burst(final LivingEntity boss, final Particle particle,
                              final Location at, final int count, final double spread) {
        boss.getWorld().spawnParticle(particle, at, count, spread, 0.6, spread, 0.02);
    }

    private static void rubble(final LivingEntity boss, final Location at,
                               final int count, final double spread) {
        boss.getWorld().spawnParticle(Particle.BLOCK, at, count, spread, 0.6, spread, 0.05,
                org.bukkit.Material.COBBLESTONE.createBlockData());
    }

    private static Vector towards(final LivingEntity from, final LivingEntity to) {
        final Vector dir = to.getEyeLocation().toVector()
                .subtract(from.getEyeLocation().toVector());
        return dir.lengthSquared() < 1.0E-4 ? new Vector(0, 0, 1) : dir.normalize();
    }

    private static AreaEffectCloud cloud(final LivingEntity boss, final Location at,
                                         final PotionEffectType type, final int amplifier,
                                         final float radius, final int durationTicks) {
        final AreaEffectCloud cloud = (AreaEffectCloud) boss.getWorld()
                .spawnEntity(at, EntityType.AREA_EFFECT_CLOUD);
        cloud.addCustomEffect(new PotionEffect(type, 60, amplifier), true);
        cloud.setRadius(radius);
        cloud.setDuration(durationTicks);
        cloud.setSource(boss);
        return cloud;
    }

    private static LivingEntity minion(final LivingEntity boss, final EntityType type,
                                       final double dx, final double dz) {
        final var e = boss.getWorld().spawnEntity(
                boss.getLocation().clone().add(dx, 0, dz), type);
        return e instanceof final LivingEntity le ? le : null;
    }

    // ── Registry: 28 unique effects ───────────────────────────────────────────

    static {

        // ═══ Tharok, the Stone Colossus (Ancient Ruins) ═══════════════════════

        // Smashes the ground: heavy damage, players are launched skyward.
        EFFECTS.put("seismic_slam", (boss, a) -> {
            sound(boss, Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 0.6f);
            burst(boss, Particle.EXPLOSION, boss.getLocation(), 8, a.range() / 2);
            rubble(boss, boss.getLocation(), 40, a.range() / 2);
            for (final Player p : a.players(boss)) {
                if (a.damage() > 0) p.damage(a.damage(), boss);
                p.setVelocity(p.getVelocity().setY(1.1));
            }
        });

        // Hurls a boulder: the farthest player in range takes a crushing hit.
        EFFECTS.put("boulder_toss", (boss, a) -> {
            Player farthest = null;
            double best = -1;
            for (final Player p : a.players(boss)) {
                final double d = p.getLocation().distanceSquared(boss.getLocation());
                if (d > best) { best = d; farthest = p; }
            }
            if (farthest == null) return;
            sound(boss, Sound.ENTITY_IRON_GOLEM_ATTACK, 1.0f, 0.5f);
            boss.getWorld().spawnParticle(Particle.BLOCK, farthest.getLocation(), 60,
                    0.8, 1.2, 0.8, 0.05, org.bukkit.Material.COBBLESTONE.createBlockData());
            if (a.damage() > 0) farthest.damage(a.damage(), boss);
            farthest.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 1));
        });

        // Hardens to living stone: brief massive damage resistance.
        EFFECTS.put("stone_bulwark", (boss, a) -> {
            sound(boss, Sound.BLOCK_ANVIL_LAND, 1.0f, 0.5f);
            rubble(boss, boss.getLocation().add(0, 1, 0), 80, 0.8);
            boss.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 120, 2));
        });

        // Stone hands erupt and drag every player toward the colossus.
        EFFECTS.put("earthen_grasp", (boss, a) -> {
            sound(boss, Sound.BLOCK_GRAVEL_BREAK, 1.0f, 0.4f);
            for (final Player p : a.players(boss)) {
                final Vector pull = boss.getLocation().toVector()
                        .subtract(p.getLocation().toVector()).normalize().multiply(1.4).setY(0.2);
                p.setVelocity(pull);
                p.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 50, 3));
                rubble(boss, p.getLocation(), 20, 0.4);
                if (a.damage() > 0) p.damage(a.damage(), boss);
            }
        });

        // ═══ Sylvara, the Briar Queen (Ancient Ruins) ═════════════════════════

        // Fires a fan of poison-tipped thorns.
        EFFECTS.put("thorn_volley", (boss, a) -> {
            final Player target = a.nearest(boss);
            if (target == null) return;
            sound(boss, Sound.ENTITY_ARROW_SHOOT, 1.0f, 0.7f);
            final Vector base = towards(boss, target);
            for (int i = -2; i <= 2; i++) {
                final Vector dir = base.clone().rotateAroundY(Math.toRadians(i * 9));
                final Arrow arrow = boss.launchProjectile(Arrow.class, dir.multiply(1.6));
                arrow.addCustomEffect(new PotionEffect(PotionEffectType.POISON, 80, 0), true);
                arrow.setDamage(Math.max(1.0, a.damage() / 2));
            }
        });

        // Roots the closest player in place with strangling vines.
        EFFECTS.put("strangling_vines", (boss, a) -> {
            final Player target = a.nearest(boss);
            if (target == null) return;
            sound(boss, Sound.BLOCK_CAVE_VINES_BREAK, 1.0f, 0.5f);
            burst(boss, Particle.HAPPY_VILLAGER, target.getLocation(), 30, 0.6);
            target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 100, 5));
            target.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, 100, 128));
            if (a.damage() > 0) target.damage(a.damage(), boss);
        });

        // Releases a lingering cloud of toxic spores.
        EFFECTS.put("spore_cloud", (boss, a) -> {
            final Player target = a.nearest(boss);
            final Location at = target != null ? target.getLocation() : boss.getLocation();
            sound(boss, Sound.ENTITY_PUFFER_FISH_BLOW_UP, 1.0f, 0.6f);
            cloud(boss, at, PotionEffectType.POISON, 1, (float) Math.min(4.0, a.range() / 2), 140);
        });

        // Draws life from the roots: heals herself and quickens her step.
        EFFECTS.put("wild_regrowth", (boss, a) -> {
            sound(boss, Sound.BLOCK_AZALEA_LEAVES_PLACE, 1.0f, 1.2f);
            burst(boss, Particle.HEART, boss.getLocation().add(0, 1.5, 0), 12, 0.8);
            final var maxAttr = boss.getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH);
            final double max = maxAttr != null ? maxAttr.getValue() : boss.getHealth();
            boss.setHealth(Math.min(max, boss.getHealth() + Math.max(10.0, max * 0.06)));
            boss.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 100, 1));
        });

        // ═══ Boreas, the Frost Sovereign (Frozen Cavern) ══════════════════════

        // Launches a piercing lance of ice at the closest player.
        EFFECTS.put("glacial_lance", (boss, a) -> {
            final Player target = a.nearest(boss);
            if (target == null) return;
            sound(boss, Sound.BLOCK_GLASS_BREAK, 1.0f, 0.6f);
            final SpectralArrow lance = boss.launchProjectile(
                    SpectralArrow.class, towards(boss, target).multiply(2.2));
            lance.setDamage(Math.max(1.0, a.damage()));
            burst(boss, Particle.SNOWFLAKE, boss.getEyeLocation(), 25, 0.3);
        });

        // Explodes in a nova of frost, freezing everyone nearby solid.
        EFFECTS.put("frost_nova", (boss, a) -> {
            sound(boss, Sound.BLOCK_POWDER_SNOW_BREAK, 1.0f, 0.5f);
            burst(boss, Particle.SNOWFLAKE, boss.getLocation(), 120, a.range() / 2);
            for (final Player p : a.players(boss)) {
                if (a.damage() > 0) p.damage(a.damage(), boss);
                p.setFreezeTicks(p.getMaxFreezeTicks() + 60);
                p.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 80, 2));
            }
        });

        // Calls down a battering hailstorm over every player's head.
        EFFECTS.put("hailstorm", (boss, a) -> {
            sound(boss, Sound.ENTITY_PLAYER_HURT_FREEZE, 1.0f, 0.6f);
            for (final Player p : a.players(boss)) {
                boss.getWorld().spawnParticle(Particle.SNOWFLAKE,
                        p.getLocation().add(0, 3, 0), 80, 0.8, 0.4, 0.8, 0.1);
                if (a.damage() > 0) p.damage(a.damage(), boss);
                p.addPotionEffect(new PotionEffect(PotionEffectType.MINING_FATIGUE, 100, 1));
            }
        });

        // Sheets of permafrost spread from the throne: a lingering slow-field.
        EFFECTS.put("permafrost_field", (boss, a) -> {
            sound(boss, Sound.BLOCK_GLASS_PLACE, 1.0f, 0.4f);
            final AreaEffectCloud field = cloud(boss, boss.getLocation(),
                    PotionEffectType.SLOWNESS, 2, (float) Math.min(5.0, a.range() / 2), 160);
            field.setParticle(Particle.SNOWFLAKE);
        });

        // ═══ Aethon, the Storm Herald (Frozen Cavern) ═════════════════════════

        // Lightning cracks down on up to three players at once.
        EFFECTS.put("thunder_strike", (boss, a) -> {
            int struck = 0;
            for (final Player p : a.players(boss)) {
                if (struck >= 3) break;
                boss.getWorld().strikeLightningEffect(p.getLocation());
                if (a.damage() > 0) p.damage(a.damage(), boss);
                struck++;
            }
            if (struck > 0) sound(boss, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1.0f, 1.0f);
        });

        // A hurricane blast that flings players away and lifts them briefly.
        EFFECTS.put("gale_burst", (boss, a) -> {
            sound(boss, Sound.ENTITY_BREEZE_SHOOT, 1.0f, 0.6f);
            burst(boss, Particle.CLOUD, boss.getLocation(), 80, a.range() / 2);
            for (final Player p : a.players(boss)) {
                final Vector away = p.getLocation().toVector()
                        .subtract(boss.getLocation().toVector()).normalize().multiply(2.0).setY(0.8);
                p.setVelocity(away);
                p.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, 20, 1));
                if (a.damage() > 0) p.damage(a.damage(), boss);
            }
        });

        // Wraps himself in howling winds: absorption hearts and storm-speed.
        EFFECTS.put("tempest_shield", (boss, a) -> {
            sound(boss, Sound.ITEM_TRIDENT_RETURN, 1.0f, 0.8f);
            burst(boss, Particle.CLOUD, boss.getLocation().add(0, 1, 0), 50, 0.8);
            boss.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 200, 3));
            boss.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 200, 1));
        });

        // Homing orbs of charged storm energy chase their targets.
        EFFECTS.put("storm_orbs", (boss, a) -> {
            final List<Player> players = a.players(boss);
            if (players.isEmpty()) return;
            sound(boss, Sound.ENTITY_SHULKER_SHOOT, 1.0f, 1.4f);
            int fired = 0;
            for (final Player p : players) {
                if (fired >= 2) break;
                final ShulkerBullet orb = (ShulkerBullet) boss.getWorld().spawnEntity(
                        boss.getEyeLocation(), EntityType.SHULKER_BULLET);
                orb.setShooter(boss);
                orb.setTarget(p);
                fired++;
            }
        });

        // ═══ Zharok, the Void Seer (Corrupted Temple) ═════════════════════════

        // A line of void fangs snaps up from the floor toward the target.
        EFFECTS.put("void_fangs", (boss, a) -> {
            final Player target = a.nearest(boss);
            if (target == null) return;
            sound(boss, Sound.ENTITY_EVOKER_PREPARE_ATTACK, 1.0f, 0.8f);
            final Vector step = towards(boss, target).setY(0);
            final Location cursor = boss.getLocation().clone();
            for (int i = 1; i <= (int) Math.min(12, a.range()); i++) {
                cursor.add(step);
                boss.getWorld().spawnEntity(cursor, EntityType.EVOKER_FANGS);
            }
        });

        // Shatters minds: nausea, blindness and creeping darkness.
        EFFECTS.put("mind_fracture", (boss, a) -> {
            sound(boss, Sound.ENTITY_ELDER_GUARDIAN_CURSE, 1.0f, 0.7f);
            burst(boss, Particle.PORTAL, boss.getLocation().add(0, 1, 0), 100, a.range() / 2);
            for (final Player p : a.players(boss)) {
                p.addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA, 140, 0));
                p.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 60, 0));
                p.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, 120, 0));
                if (a.damage() > 0) p.damage(a.damage(), boss);
            }
        });

        // Tears a rift and drags the farthest player through it to his feet.
        EFFECTS.put("rift_pull", (boss, a) -> {
            Player farthest = null;
            double best = -1;
            for (final Player p : a.players(boss)) {
                final double d = p.getLocation().distanceSquared(boss.getLocation());
                if (d > best) { best = d; farthest = p; }
            }
            if (farthest == null) return;
            sound(boss, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 0.5f);
            burst(boss, Particle.REVERSE_PORTAL, farthest.getLocation(), 60, 0.5);
            farthest.teleport(boss.getLocation().clone().add(
                    towards(boss, farthest).setY(0).multiply(1.5)));
            if (a.damage() > 0) farthest.damage(a.damage(), boss);
        });

        // Annuls all blessings: strips positive effects and saps strength.
        EFFECTS.put("null_curse", (boss, a) -> {
            sound(boss, Sound.BLOCK_RESPAWN_ANCHOR_DEPLETE, 1.0f, 0.6f);
            final List<PotionEffectType> blessings = List.of(
                    PotionEffectType.SPEED, PotionEffectType.REGENERATION,
                    PotionEffectType.STRENGTH, PotionEffectType.RESISTANCE,
                    PotionEffectType.ABSORPTION, PotionEffectType.FIRE_RESISTANCE,
                    PotionEffectType.INVISIBILITY, PotionEffectType.JUMP_BOOST,
                    PotionEffectType.HASTE, PotionEffectType.NIGHT_VISION,
                    PotionEffectType.HEALTH_BOOST, PotionEffectType.SATURATION);
            for (final Player p : a.players(boss)) {
                blessings.forEach(p::removePotionEffect);
                p.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 160, 1));
                burst(boss, Particle.SQUID_INK, p.getEyeLocation(), 20, 0.3);
            }
        });

        // ═══ Vulkhan, the Forgefather (Volcanic Fortress) ═════════════════════

        // The forge floor erupts: everyone nearby is set ablaze.
        EFFECTS.put("magma_eruption", (boss, a) -> {
            sound(boss, Sound.ENTITY_GENERIC_BURN, 1.0f, 0.5f);
            burst(boss, Particle.LAVA, boss.getLocation(), 60, a.range() / 2);
            for (final Player p : a.players(boss)) {
                p.setFireTicks(Math.max(p.getFireTicks(), 100));
                if (a.damage() > 0) p.damage(a.damage(), boss);
            }
        });

        // Spits a burst of three molten slag bolts in a spread.
        EFFECTS.put("slag_volley", (boss, a) -> {
            final Player target = a.nearest(boss);
            if (target == null) return;
            sound(boss, Sound.ENTITY_BLAZE_SHOOT, 1.0f, 0.7f);
            final Vector base = towards(boss, target);
            for (int i = -1; i <= 1; i++) {
                boss.launchProjectile(SmallFireball.class,
                        base.clone().rotateAroundY(Math.toRadians(i * 12)));
            }
        });

        // Hammers the anvil: molten constructs crawl out of the forge.
        EFFECTS.put("forge_summon", (boss, a) -> {
            sound(boss, Sound.BLOCK_ANVIL_USE, 1.0f, 0.6f);
            for (int i = 0; i < 2; i++) {
                minion(boss, EntityType.MAGMA_CUBE, Math.random() * 4 - 2, Math.random() * 4 - 2);
            }
            burst(boss, Particle.FLAME, boss.getLocation(), 40, 1.5);
        });

        // Charges the nearest player in a wave of molten metal.
        EFFECTS.put("molten_rush", (boss, a) -> {
            final Player target = a.nearest(boss);
            if (target == null) return;
            sound(boss, Sound.ENTITY_RAVAGER_ROAR, 1.0f, 0.8f);
            burst(boss, Particle.FLAME, boss.getLocation(), 40, 0.5);
            boss.setVelocity(towards(boss, target).setY(0.2).multiply(2.2));
            if (target.getLocation().distanceSquared(boss.getLocation()) < 16 && a.damage() > 0) {
                target.damage(a.damage(), boss);
                target.setFireTicks(Math.max(target.getFireTicks(), 60));
            }
        });

        // ═══ Nyxara, the Grave Whisper (Forgotten Catacombs) ══════════════════

        // Sweeps a spectral scythe through everyone within reach, withering them.
        EFFECTS.put("soul_reap", (boss, a) -> {
            sound(boss, Sound.ENTITY_WITHER_SHOOT, 1.0f, 0.5f);
            burst(boss, Particle.SCULK_SOUL, boss.getLocation().add(0, 1, 0), 50, a.range() / 3);
            for (final Player p : a.players(boss)) {
                if (a.damage() > 0) p.damage(a.damage(), boss);
                p.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 80, 0));
            }
        });

        // Whispers to the dark: vengeful wraiths answer the call.
        EFFECTS.put("wraith_summon", (boss, a) -> {
            sound(boss, Sound.ENTITY_VEX_AMBIENT, 1.0f, 0.6f);
            for (int i = 0; i < 2; i++) {
                minion(boss, EntityType.VEX, Math.random() * 3 - 1.5, Math.random() * 3 - 1.5);
            }
            burst(boss, Particle.SOUL, boss.getLocation().add(0, 1.5, 0), 40, 1.0);
        });

        // Drains the life of every player nearby and mends her own wounds.
        EFFECTS.put("life_siphon", (boss, a) -> {
            sound(boss, Sound.ENTITY_WARDEN_HEARTBEAT, 1.0f, 0.7f);
            double drained = 0;
            for (final Player p : a.players(boss)) {
                if (a.damage() > 0) {
                    p.damage(a.damage(), boss);
                    drained += a.damage();
                }
                burst(boss, Particle.DAMAGE_INDICATOR, p.getEyeLocation(), 8, 0.2);
            }
            if (drained > 0) {
                final var maxAttr = boss.getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH);
                final double max = maxAttr != null ? maxAttr.getValue() : boss.getHealth();
                boss.setHealth(Math.min(max, boss.getHealth() + drained));
            }
        });

        // The death-knell tolls: dread crushes and scatters the living.
        EFFECTS.put("dread_toll", (boss, a) -> {
            sound(boss, Sound.BLOCK_BELL_RESONATE, 1.0f, 0.4f);
            burst(boss, Particle.SONIC_BOOM, boss.getLocation().add(0, 1, 0), 3, 0.4);
            for (final Player p : a.players(boss)) {
                p.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, 100, 0));
                p.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 100, 1));
                final Vector away = p.getLocation().toVector()
                        .subtract(boss.getLocation().toVector()).normalize().multiply(1.2).setY(0.4);
                p.setVelocity(away);
                if (a.damage() > 0) p.damage(a.damage(), boss);
            }
        });
    }
}

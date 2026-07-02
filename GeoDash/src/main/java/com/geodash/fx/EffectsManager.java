package com.geodash.fx;

import com.geodash.GeoDashPlugin;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;

/**
 * Particle trails, death explosions, sounds and fireworks.
 * Sounds are played by string key so the jar works across all 1.21.x
 * versions regardless of the Sound enum/interface changes.
 */
public class EffectsManager {

    private final GeoDashPlugin plugin;
    private boolean trail;
    private Particle trailParticle;
    private boolean deathExplosion;
    private boolean sounds;
    private boolean winFirework;

    public EffectsManager(GeoDashPlugin plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        trail = plugin.getConfig().getBoolean("effects.trail", true);
        deathExplosion = plugin.getConfig().getBoolean("effects.death-explosion", true);
        sounds = plugin.getConfig().getBoolean("effects.sounds", true);
        winFirework = plugin.getConfig().getBoolean("effects.win-firework", true);
        try {
            trailParticle = Particle.valueOf(plugin.getConfig().getString("effects.trail-particle", "END_ROD"));
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Unknown trail particle, falling back to END_ROD");
            trailParticle = Particle.END_ROD;
        }
    }

    public void trail(Player player) {
        if (trail) {
            player.getWorld().spawnParticle(trailParticle, player.getLocation().add(0, 0.4, 0),
                    2, 0.1, 0.1, 0.1, 0.01);
        }
    }

    public void death(Player player) {
        Location loc = player.getLocation();
        if (deathExplosion) {
            loc.getWorld().spawnParticle(Particle.EXPLOSION, loc.clone().add(0, 1, 0), 1);
            loc.getWorld().spawnParticle(Particle.FLAME, loc.clone().add(0, 1, 0), 20, 0.3, 0.3, 0.3, 0.05);
        }
        if (sounds) {
            player.playSound(loc, "minecraft:entity.generic.explode", 0.8f, 1.2f);
        }
    }

    public void jumpPad(Player player) {
        if (sounds) {
            player.playSound(player.getLocation(), "minecraft:entity.slime.jump", 1f, 1.4f);
        }
    }

    public void countdownTick(Player player) {
        if (sounds) {
            player.playSound(player.getLocation(), "minecraft:block.note_block.hat", 1f, 1f);
        }
    }

    public void countdownGo(Player player) {
        if (sounds) {
            player.playSound(player.getLocation(), "minecraft:block.note_block.pling", 1f, 2f);
        }
    }

    public void win(Player player) {
        if (sounds) {
            player.playSound(player.getLocation(), "minecraft:ui.toast.challenge_complete", 1f, 1f);
        }
        if (winFirework) {
            Location loc = player.getLocation();
            loc.getWorld().spawn(loc, Firework.class, fw -> {
                FireworkMeta meta = fw.getFireworkMeta();
                meta.addEffect(FireworkEffect.builder()
                        .with(FireworkEffect.Type.BALL_LARGE)
                        .withColor(Color.AQUA, Color.YELLOW)
                        .withFade(Color.WHITE)
                        .flicker(true)
                        .build());
                meta.setPower(1);
                fw.setFireworkMeta(meta);
            });
        }
    }
}

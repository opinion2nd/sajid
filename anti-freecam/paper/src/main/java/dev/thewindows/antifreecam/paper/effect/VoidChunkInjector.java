package dev.thewindows.antifreecam.paper.effect;

import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public class VoidChunkInjector {

    private final Logger logger;
    private final double triggerY;
    private final Set<UUID> activeVoidPlayers = ConcurrentHashMap.newKeySet();

    public VoidChunkInjector(Object unused1, Logger logger, int unused2, double triggerY) {
        this.logger = logger;
        this.triggerY = triggerY;
    }

    public void applyVoidEffect(Player player) {
        activeVoidPlayers.add(player.getUniqueId());
        player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, Integer.MAX_VALUE, 0, false, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, Integer.MAX_VALUE, 0, false, false, false));
        logger.info("[AntiFreeam] Void effect applied to " + player.getName());
    }

    public void removeVoidEffect(Player player) {
        if (activeVoidPlayers.remove(player.getUniqueId())) {
            player.removePotionEffect(PotionEffectType.BLINDNESS);
            player.removePotionEffect(PotionEffectType.DARKNESS);
            logger.info("[AntiFreeam] Void effect removed from " + player.getName());
        }
    }

    public boolean hasVoidEffect(UUID playerId) {
        return activeVoidPlayers.contains(playerId);
    }

    public void recheckActive(Player player) {
        if (activeVoidPlayers.contains(player.getUniqueId())) {
            applyVoidEffect(player);
        }
    }

    public void cleanup(UUID playerId) {
        activeVoidPlayers.remove(playerId);
    }
}

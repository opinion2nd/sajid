package com.geodash.game;

import com.geodash.level.Level;
import com.geodash.util.CompatScheduler;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * One player's run through a level, including everything needed to put
 * the player back exactly how they were when they leave the arena.
 */
public class GameSession {

    public final Player player;
    public final Level level;
    public final Race race;

    public int attempts = 1;
    public long attemptStartMs;
    public double bestPercentThisSession;
    public BossBar bossBar;
    public CompatScheduler.Task task;
    public int lastPadTick = -100;
    public int tickCount;

    // Saved player state
    private final ItemStack[] savedInventory;
    private final GameMode savedGameMode;
    private final Location savedLocation;
    private final double savedHealth;
    private final int savedFood;
    private final float savedSaturation;
    private final boolean savedAllowFlight;

    public GameSession(Player player, Level level, Race race) {
        this.player = player;
        this.level = level;
        this.race = race;
        this.attemptStartMs = System.currentTimeMillis();

        this.savedInventory = player.getInventory().getContents();
        this.savedGameMode = player.getGameMode();
        this.savedLocation = player.getLocation().clone();
        this.savedHealth = player.getHealth();
        this.savedFood = player.getFoodLevel();
        this.savedSaturation = player.getSaturation();
        this.savedAllowFlight = player.getAllowFlight();
    }

    /** True while a race lobby/countdown is holding the player at the start. */
    public boolean isFrozen() {
        return race != null && !race.isRunning();
    }

    public void restorePlayer() {
        player.getInventory().setContents(savedInventory);
        player.setGameMode(savedGameMode);
        player.setHealth(Math.min(savedHealth, player.getMaxHealth()));
        player.setFoodLevel(savedFood);
        player.setSaturation(savedSaturation);
        player.setAllowFlight(savedAllowFlight);
        player.setFallDistance(0);
        CompatScheduler.teleport(player, savedLocation);
    }
}

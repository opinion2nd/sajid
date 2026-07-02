package com.geodash.game;

import com.geodash.GeoDashPlugin;
import com.geodash.level.Level;
import com.geodash.stats.StatsManager;
import com.geodash.util.CompatScheduler;
import com.geodash.util.Msg;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The heart of GeoDash: forces auto-run each tick, detects hazard contact
 * (one-hit death), tracks progress % and handles death / completion.
 */
public class GameManager {

    private final GeoDashPlugin plugin;
    private final Map<UUID, GameSession> sessions = new ConcurrentHashMap<>();
    private Set<Material> hazards = EnumSet.noneOf(Material.class);
    private double voidOffset = 8;
    private double jumpPadBoost = 1.05;
    private boolean useBossBar = true;
    private boolean useActionBar = true;

    public GameManager(GeoDashPlugin plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        Set<Material> set = EnumSet.noneOf(Material.class);
        for (String name : plugin.getConfig().getStringList("hazard-blocks")) {
            Material material = Material.matchMaterial(name);
            if (material != null) {
                set.add(material);
            } else {
                plugin.getLogger().warning("Unknown hazard block in config: " + name);
            }
        }
        hazards = set;
        voidOffset = plugin.getConfig().getDouble("void-death-offset", 8);
        jumpPadBoost = plugin.getConfig().getDouble("jump-pad-boost", 1.05);
        useBossBar = plugin.getConfig().getBoolean("scoreboard.bossbar", true);
        useActionBar = plugin.getConfig().getBoolean("scoreboard.actionbar", true);
    }

    public GameSession session(Player player) {
        return sessions.get(player.getUniqueId());
    }

    public boolean inGame(Player player) {
        return sessions.containsKey(player.getUniqueId());
    }

    public void join(Player player, Level level, Race race) {
        if (inGame(player)) {
            Msg.send(player, "&cYou are already in a level. Use &f/gd leave &cfirst.");
            return;
        }
        if (!level.isReady() || level.getWorld() == null) {
            Msg.send(player, "&cThat level is not playable yet (missing start/finish or world).");
            return;
        }

        GameSession session = new GameSession(player, level, race);
        sessions.put(player.getUniqueId(), session);

        player.getInventory().clear();
        player.setGameMode(GameMode.ADVENTURE);
        player.setHealth(player.getMaxHealth());
        player.setFoodLevel(20);
        player.setSaturation(20);
        player.setAllowFlight(false);
        player.setFallDistance(0);
        CompatScheduler.teleport(player, level.startLocation());

        if (useBossBar) {
            session.bossBar = Bukkit.createBossBar(barTitle(session, 0), BarColor.BLUE, BarStyle.SEGMENTED_10);
            session.bossBar.setProgress(0);
            session.bossBar.addPlayer(player);
        }

        Msg.title(player, "&b" + level.getName(), "&7Attempt &f1", 5, 30, 10);
        Msg.send(player, "&aGO! &7Jump over the spikes. &f/gd leave &7to quit.");

        session.task = CompatScheduler.runEntityTimer(plugin, player, () -> tick(session), 1, 1);
    }

    public void leave(Player player, boolean announce) {
        GameSession session = sessions.remove(player.getUniqueId());
        if (session == null) {
            return;
        }
        if (session.task != null) {
            session.task.cancel();
        }
        if (session.bossBar != null) {
            session.bossBar.removeAll();
        }
        if (session.race != null) {
            session.race.playerLeft(player);
        }
        session.restorePlayer();
        if (announce) {
            Msg.send(player, "&7You left &f" + session.level.getName() + "&7.");
        }
    }

    public void leaveAll() {
        for (UUID id : new ArrayList<>(sessions.keySet())) {
            Player player = Bukkit.getPlayer(id);
            if (player != null) {
                leave(player, false);
            }
        }
    }

    private void tick(GameSession session) {
        Player player = session.player;
        if (!player.isOnline()) {
            leave(player, false);
            return;
        }
        Level level = session.level;
        Location loc = player.getLocation();

        // Player teleported to another world (e.g. by another plugin) -> end the run
        if (loc.getWorld() == null || !loc.getWorld().getName().equals(level.getWorldName())) {
            leave(player, true);
            return;
        }

        session.tickCount++;

        // Race lobby / countdown: hold position, no movement yet.
        // Keep resetting the clock so the run time starts at GO.
        if (session.isFrozen()) {
            session.attemptStartMs = System.currentTimeMillis();
            player.setVelocity(new Vector(0, Math.min(0, player.getVelocity().getY()), 0));
            return;
        }

        // Auto-run: force forward speed, keep vertical motion (jumping stays client controlled)
        Vector dir = level.directionVector();
        player.setVelocity(new Vector(
                dir.getX() * level.getSpeed(),
                player.getVelocity().getY(),
                dir.getZ() * level.getSpeed()));

        // Jump pad: standing on slime launches you
        Material below = loc.clone().subtract(0, 0.1, 0).getBlock().getType();
        if (below == Material.SLIME_BLOCK && session.tickCount - session.lastPadTick > 10) {
            session.lastPadTick = session.tickCount;
            player.setVelocity(new Vector(
                    dir.getX() * level.getSpeed(), jumpPadBoost, dir.getZ() * level.getSpeed()));
            plugin.getEffects().jumpPad(player);
        }

        plugin.getEffects().trail(player);

        // One-hit death: hazard contact or falling below the course
        if (isHazard(loc) || loc.getY() < level.getStartY() - voidOffset) {
            death(session);
            return;
        }

        // Progress
        double percent = level.progress(loc);
        session.bestPercentThisSession = Math.max(session.bestPercentThisSession, percent);
        if (session.bossBar != null) {
            session.bossBar.setProgress(percent / 100.0);
            if (session.tickCount % 5 == 0) {
                session.bossBar.setTitle(barTitle(session, percent));
            }
        }
        if (useActionBar && session.tickCount % 10 == 0) {
            Msg.actionBar(player, "&b" + (int) percent + "% &8| &7Attempt &f" + session.attempts
                    + " &8| &7" + Msg.time(System.currentTimeMillis() - session.attemptStartMs));
        }

        if (percent >= 100.0) {
            complete(session);
        }
    }

    private String barTitle(GameSession session, double percent) {
        return Msg.color("&b" + session.level.getName() + " &8- &f" + (int) percent
                + "% &8- &7Attempt &f" + session.attempts);
    }

    private boolean isHazard(Location loc) {
        return hazards.contains(loc.getBlock().getType())
                || hazards.contains(loc.clone().add(0, 1, 0).getBlock().getType())
                || hazards.contains(loc.clone().subtract(0, 0.0625, 0).getBlock().getType());
    }

    public void death(GameSession session) {
        Player player = session.player;
        plugin.getEffects().death(player);

        StatsManager.LevelStats stats = plugin.getStats().of(player).level(session.level.getName());
        stats.attempts++;
        stats.bestPercent = Math.max(stats.bestPercent, session.bestPercentThisSession);
        plugin.getStats().markDirty();

        session.attempts++;
        session.attemptStartMs = System.currentTimeMillis();
        session.bestPercentThisSession = 0;
        player.setFallDistance(0);
        player.setVelocity(new Vector(0, 0, 0));
        CompatScheduler.teleport(player, session.level.startLocation());
        Msg.title(player, "&c✖", "&7Attempt &f" + session.attempts, 0, 15, 5);
    }

    public void complete(GameSession session) {
        Player player = session.player;
        long timeMs = System.currentTimeMillis() - session.attemptStartMs;
        Level level = session.level;

        StatsManager.LevelStats stats = plugin.getStats().of(player).level(level.getName());
        stats.attempts++;
        stats.completions++;
        stats.bestPercent = 100;
        if (stats.bestTimeMs < 0 || timeMs < stats.bestTimeMs) {
            stats.bestTimeMs = timeMs;
        }
        plugin.getStats().markDirty();

        plugin.getEffects().win(player);
        Msg.title(player, "&a&lLEVEL COMPLETE!", "&f" + Msg.time(timeMs) + " &7- attempt &f" + session.attempts, 5, 50, 15);

        int place = session.race != null ? session.race.playerFinished(player, timeMs) : 0;
        int attempts = session.attempts;
        leave(player, false);

        if (place == 1) {
            plugin.getRewards().raceWin(player, level, timeMs, attempts);
        }
        if (!plugin.getRewards().firstTimeOnly() || stats.completions == 1) {
            plugin.getRewards().completion(player, level, timeMs, attempts);
        }
    }
}

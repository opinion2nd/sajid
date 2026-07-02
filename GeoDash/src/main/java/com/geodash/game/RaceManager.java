package com.geodash.game;

import com.geodash.GeoDashPlugin;
import com.geodash.level.Level;
import com.geodash.util.CompatScheduler;
import com.geodash.util.Msg;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.UUID;

public class RaceManager {

    private final GeoDashPlugin plugin;
    private Race active;

    public RaceManager(GeoDashPlugin plugin) {
        this.plugin = plugin;
    }

    public Race getActive() {
        return active != null && active.getState() != Race.State.DONE ? active : null;
    }

    public void create(CommandSender sender, Level level) {
        if (getActive() != null) {
            Msg.send(sender, "&cA race is already open. Cancel it first with &f/gd race cancel&c.");
            return;
        }
        active = new Race(level);
        Bukkit.broadcastMessage(Msg.color("&8[&bRace&8] &e" + sender.getName()
                + " &7opened a race on &f" + level.getName()
                + "&7! Join with &b/gd race join"));
    }

    public void join(Player player) {
        Race race = getActive();
        if (race == null) {
            Msg.send(player, "&cThere is no open race right now.");
            return;
        }
        if (race.getState() != Race.State.LOBBY) {
            Msg.send(player, "&cThat race has already started.");
            return;
        }
        if (race.getPlayers().contains(player.getUniqueId())) {
            Msg.send(player, "&cYou already joined this race.");
            return;
        }
        race.addPlayer(player);
        plugin.getGame().join(player, race.getLevel(), race);
        if (plugin.getGame().inGame(player)) {
            Bukkit.broadcastMessage(Msg.color("&8[&bRace&8] &b" + player.getName()
                    + " &7joined the race! (&f" + race.getPlayers().size() + "&7)"));
        } else {
            race.playerLeft(player); // join was refused (e.g. already in a level)
        }
    }

    public void start(CommandSender sender) {
        Race race = getActive();
        if (race == null || race.getState() != Race.State.LOBBY) {
            Msg.send(sender, "&cThere is no race lobby to start.");
            return;
        }
        if (race.getPlayers().isEmpty()) {
            Msg.send(sender, "&cNobody joined the race yet.");
            return;
        }
        race.setState(Race.State.COUNTDOWN);
        for (UUID id : new ArrayList<>(race.getPlayers())) {
            Player player = Bukkit.getPlayer(id);
            if (player == null) {
                continue;
            }
            for (int i = 3; i >= 1; i--) {
                final int number = i;
                CompatScheduler.runEntityLater(plugin, player, () -> {
                    Msg.title(player, "&e" + number, "&7Get ready...", 0, 20, 5);
                    plugin.getEffects().countdownTick(player);
                }, (4 - i) * 20L);
            }
            CompatScheduler.runEntityLater(plugin, player, () -> {
                Msg.title(player, "&a&lGO!", "", 0, 15, 5);
                plugin.getEffects().countdownGo(player);
            }, 80L);
        }
        CompatScheduler.runGlobalLater(plugin, () -> race.setState(Race.State.RUNNING), 80L);
    }

    public void cancel(CommandSender sender) {
        Race race = getActive();
        if (race == null) {
            Msg.send(sender, "&cThere is no active race.");
            return;
        }
        for (UUID id : new ArrayList<>(race.getPlayers())) {
            Player player = Bukkit.getPlayer(id);
            if (player != null) {
                plugin.getGame().leave(player, true);
            }
        }
        race.setState(Race.State.DONE);
        active = null;
        Bukkit.broadcastMessage(Msg.color("&8[&bRace&8] &cThe race was cancelled."));
    }
}

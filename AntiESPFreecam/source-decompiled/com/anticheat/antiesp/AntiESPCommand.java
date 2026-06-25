/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.ChatColor
 *  org.bukkit.command.Command
 *  org.bukkit.command.CommandExecutor
 *  org.bukkit.command.CommandSender
 *  org.bukkit.command.TabCompleter
 *  org.bukkit.entity.Player
 *  org.bukkit.event.Listener
 *  org.bukkit.plugin.Plugin
 */
package com.anticheat.antiesp;

import com.anticheat.antiesp.AntiESPFreecamPlugin;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

public class AntiESPCommand
implements CommandExecutor,
TabCompleter {
    private final AntiESPFreecamPlugin plugin;

    public AntiESPCommand(AntiESPFreecamPlugin antiESPFreecamPlugin) {
        this.plugin = antiESPFreecamPlugin;
    }

    public boolean onCommand(CommandSender commandSender, Command command, String string, String[] stringArray) {
        if (stringArray.length > 0 && stringArray[0].equalsIgnoreCase("reload")) {
            if (!commandSender.hasPermission("antiesp.reload")) {
                commandSender.sendMessage(String.valueOf(ChatColor.RED) + "You do not have permission to use this command.");
                return true;
            }
            this.plugin.reloadAntiEsp();
            commandSender.sendMessage(String.valueOf(ChatColor.GREEN) + "AntiESPFreecam configuration reloaded.");
            return true;
        }
        if (stringArray.length > 0 && stringArray[0].equalsIgnoreCase("bypass")) {
            if (!commandSender.hasPermission("antiesp.bypass")) {
                commandSender.sendMessage(String.valueOf(ChatColor.RED) + "You do not have permission to use this command.");
                return true;
            }
            b b2 = this.plugin.getMaskManager();
            if (b2 == null) {
                commandSender.sendMessage(String.valueOf(ChatColor.RED) + "AntiESP is not running.");
                return true;
            }
            if (stringArray.length == 1) {
                if (!(commandSender instanceof Player)) {
                    commandSender.sendMessage(String.valueOf(ChatColor.RED) + "Console must specify a player: /" + string + " bypass <player>");
                    return true;
                }
                Player player = (Player)commandSender;
                if (b2.c(player)) {
                    b2.b(player);
                    commandSender.sendMessage(String.valueOf(ChatColor.YELLOW) + "AntiESP bypass disabled.");
                } else {
                    b2.a(player);
                    commandSender.sendMessage(String.valueOf(ChatColor.GREEN) + "AntiESP bypass enabled.");
                }
                return true;
            }
            Player player = Bukkit.getPlayerExact((String)stringArray[1]);
            if (player == null) {
                commandSender.sendMessage(String.valueOf(ChatColor.RED) + "Player '" + stringArray[1] + "' is not online.");
                return true;
            }
            if (b2.c(player)) {
                b2.b(player);
                commandSender.sendMessage(String.valueOf(ChatColor.YELLOW) + "AntiESP bypass disabled for " + player.getName() + ".");
            } else {
                b2.a(player);
                commandSender.sendMessage(String.valueOf(ChatColor.GREEN) + "AntiESP bypass enabled for " + player.getName() + ".");
            }
            return true;
        }
        if (stringArray.length > 0 && stringArray[0].equalsIgnoreCase("xray")) {
            if (!commandSender.hasPermission("antiesp.xray")) {
                commandSender.sendMessage(String.valueOf(ChatColor.RED) + "You do not have permission to use this command.");
                return true;
            }
            if (!(commandSender instanceof Player)) {
                commandSender.sendMessage(String.valueOf(ChatColor.RED) + "This command can only be used by a player.");
                return true;
            }
            Player player = (Player)commandSender;
            a a2 = new a(this.plugin);
            Bukkit.getPluginManager().registerEvents((Listener)a2, (Plugin)this.plugin);
            a2.a(player);
            return true;
        }
        if (stringArray.length > 0 && stringArray[0].equalsIgnoreCase("brand")) {
            if (!commandSender.hasPermission("antiesp.notify")) {
                commandSender.sendMessage(String.valueOf(ChatColor.RED) + "You do not have permission to use this command.");
                return true;
            }
            c c2 = this.plugin.getBrandDetector();
            if (c2 == null) {
                commandSender.sendMessage(String.valueOf(ChatColor.RED) + "Brand detector is not running.");
                return true;
            }
            if (stringArray.length == 1) {
                commandSender.sendMessage(String.valueOf(ChatColor.YELLOW) + "=== Client Brands ===");
                for (Player player : Bukkit.getOnlinePlayers()) {
                    String string2 = c2.a(player.getUniqueId());
                    boolean bl = c2.c(player.getUniqueId());
                    Set<String> set = c2.b(player.getUniqueId());
                    String string3 = bl ? ChatColor.RED.toString() : ChatColor.GREEN.toString();
                    commandSender.sendMessage(string3 + player.getName() + String.valueOf(ChatColor.GRAY) + " brand=" + String.valueOf(ChatColor.WHITE) + (string2 != null ? string2 : "?") + String.valueOf(ChatColor.GRAY) + " channels=" + String.valueOf(ChatColor.WHITE) + set.size());
                }
                return true;
            }
            Player player = Bukkit.getPlayerExact((String)stringArray[1]);
            if (player == null) {
                commandSender.sendMessage(String.valueOf(ChatColor.RED) + "Player '" + stringArray[1] + "' is not online.");
                return true;
            }
            String string4 = c2.a(player.getUniqueId());
            boolean bl = c2.c(player.getUniqueId());
            Set<String> set = c2.b(player.getUniqueId());
            commandSender.sendMessage(String.valueOf(ChatColor.YELLOW) + "=== " + player.getName() + " ===");
            commandSender.sendMessage(String.valueOf(ChatColor.GRAY) + "Brand: " + String.valueOf(bl ? ChatColor.RED : ChatColor.GREEN) + (string4 != null ? string4 : "unknown"));
            commandSender.sendMessage(String.valueOf(ChatColor.GRAY) + "Modded: " + (bl ? String.valueOf(ChatColor.RED) + "YES" : String.valueOf(ChatColor.GREEN) + "NO"));
            if (!set.isEmpty()) {
                commandSender.sendMessage(String.valueOf(ChatColor.GRAY) + "Channels (" + set.size() + "):");
                for (String string5 : set) {
                    commandSender.sendMessage(String.valueOf(ChatColor.GRAY) + " - " + String.valueOf(ChatColor.WHITE) + string5);
                }
            } else {
                commandSender.sendMessage(String.valueOf(ChatColor.GRAY) + "Channels: " + String.valueOf(ChatColor.WHITE) + "none");
            }
            return true;
        }
        if (stringArray.length > 0 && stringArray[0].equalsIgnoreCase("probe")) {
            if (!commandSender.hasPermission("antiesp.notify")) {
                commandSender.sendMessage(String.valueOf(ChatColor.RED) + "You do not have permission to use this command.");
                return true;
            }
            f f2 = this.plugin.getSignProbe();
            if (f2 == null) {
                commandSender.sendMessage(String.valueOf(ChatColor.RED) + "Sign probe is not running.");
                return true;
            }
            if (stringArray.length >= 2) {
                Player player = Bukkit.getPlayerExact((String)stringArray[1]);
                if (player == null) {
                    commandSender.sendMessage(String.valueOf(ChatColor.RED) + "Player '" + stringArray[1] + "' is not online.");
                    return true;
                }
                f2.a(player);
                commandSender.sendMessage(String.valueOf(ChatColor.YELLOW) + "Sign probe sent to " + player.getName() + ". Watch for results...");
                return true;
            }
            commandSender.sendMessage(String.valueOf(ChatColor.YELLOW) + "=== Sign Probe Status ===");
            for (Player player : Bukkit.getOnlinePlayers()) {
                String string6 = f2.a(player.getUniqueId());
                boolean bl = f2.b(player.getUniqueId());
                if (string6 != null) {
                    commandSender.sendMessage(String.valueOf(ChatColor.RED) + player.getName() + String.valueOf(ChatColor.GRAY) + " \u2192 " + String.valueOf(ChatColor.RED) + string6 + " DETECTED");
                    continue;
                }
                if (bl) {
                    commandSender.sendMessage(String.valueOf(ChatColor.GREEN) + player.getName() + String.valueOf(ChatColor.GRAY) + " \u2192 clean");
                    continue;
                }
                commandSender.sendMessage(String.valueOf(ChatColor.GRAY) + player.getName() + " \u2192 not probed yet");
            }
            return true;
        }
        commandSender.sendMessage(String.valueOf(ChatColor.YELLOW) + "AntiESPFreecam v" + this.plugin.getDescription().getVersion());
        commandSender.sendMessage(String.valueOf(ChatColor.YELLOW) + "Usage: /" + string + " reload");
        commandSender.sendMessage(String.valueOf(ChatColor.YELLOW) + "Usage: /" + string + " bypass [player]");
        commandSender.sendMessage(String.valueOf(ChatColor.YELLOW) + "Usage: /" + string + " xray");
        commandSender.sendMessage(String.valueOf(ChatColor.YELLOW) + "Usage: /" + string + " brand [player]");
        commandSender.sendMessage(String.valueOf(ChatColor.YELLOW) + "Usage: /" + string + " probe [player]");
        return true;
    }

    public List<String> onTabComplete(CommandSender commandSender, Command command, String string, String[] stringArray) {
        if (stringArray.length == 1) {
            String string2 = stringArray[0].toLowerCase();
            ArrayList<String> arrayList = new ArrayList<String>();
            if ("reload".startsWith(string2) && commandSender.hasPermission("antiesp.reload")) {
                arrayList.add("reload");
            }
            if ("bypass".startsWith(string2) && commandSender.hasPermission("antiesp.bypass")) {
                arrayList.add("bypass");
            }
            if ("xray".startsWith(string2) && commandSender.hasPermission("antiesp.xray")) {
                arrayList.add("xray");
            }
            if ("brand".startsWith(string2) && commandSender.hasPermission("antiesp.notify")) {
                arrayList.add("brand");
            }
            if ("probe".startsWith(string2) && commandSender.hasPermission("antiesp.notify")) {
                arrayList.add("probe");
            }
            return arrayList;
        }
        if (stringArray.length == 2 && stringArray[0].equalsIgnoreCase("bypass") && commandSender.hasPermission("antiesp.bypass")) {
            String string3 = stringArray[1].toLowerCase();
            ArrayList<String> arrayList = new ArrayList<String>();
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (!player.getName().toLowerCase().startsWith(string3)) continue;
                arrayList.add(player.getName());
            }
            return arrayList;
        }
        if (stringArray.length == 2 && stringArray[0].equalsIgnoreCase("brand") && commandSender.hasPermission("antiesp.notify")) {
            String string4 = stringArray[1].toLowerCase();
            ArrayList<String> arrayList = new ArrayList<String>();
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (!player.getName().toLowerCase().startsWith(string4)) continue;
                arrayList.add(player.getName());
            }
            return arrayList;
        }
        if (stringArray.length == 2 && stringArray[0].equalsIgnoreCase("probe") && commandSender.hasPermission("antiesp.notify")) {
            String string5 = stringArray[1].toLowerCase();
            ArrayList<String> arrayList = new ArrayList<String>();
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (!player.getName().toLowerCase().startsWith(string5)) continue;
                arrayList.add(player.getName());
            }
            return arrayList;
        }
        return Collections.emptyList();
    }
}


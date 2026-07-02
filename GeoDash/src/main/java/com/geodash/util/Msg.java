package com.geodash.util;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class Msg {

    private static String prefix = "";

    private Msg() {
    }

    public static void setPrefix(String raw) {
        prefix = color(raw);
    }

    public static String color(String raw) {
        return ChatColor.translateAlternateColorCodes('&', raw);
    }

    public static void send(CommandSender to, String raw) {
        to.sendMessage(prefix + color(raw));
    }

    public static void sendRaw(CommandSender to, String raw) {
        to.sendMessage(color(raw));
    }

    public static void actionBar(Player player, String raw) {
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(color(raw)));
    }

    public static void title(Player player, String title, String subtitle, int in, int stay, int out) {
        player.sendTitle(color(title), color(subtitle), in, stay, out);
    }

    /** Formats milliseconds as m:ss.SSS */
    public static String time(long millis) {
        long minutes = millis / 60000;
        long seconds = (millis % 60000) / 1000;
        long ms = millis % 1000;
        return String.format("%d:%02d.%03d", minutes, seconds, ms);
    }
}

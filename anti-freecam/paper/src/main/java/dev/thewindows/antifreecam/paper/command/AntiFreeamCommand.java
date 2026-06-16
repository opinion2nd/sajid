package dev.thewindows.antifreecam.paper.command;

import dev.thewindows.antifreecam.common.detection.FreecamDetector;
import dev.thewindows.antifreecam.paper.AntiFreeamPlugin;
import dev.thewindows.antifreecam.paper.effect.VoidChunkInjector;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class AntiFreeamCommand implements CommandExecutor, TabCompleter {

    private final AntiFreeamPlugin plugin;
    private final FreecamDetector detector;
    private final VoidChunkInjector injector;

    public AntiFreeamCommand(AntiFreeamPlugin plugin, FreecamDetector detector, VoidChunkInjector injector) {
        this.plugin = plugin;
        this.detector = detector;
        this.injector = injector;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                              @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("antifreecam.admin")) {
            sender.sendMessage(Component.text("No permission.", NamedTextColor.RED));
            return true;
        }

        if (args.length == 0) {
            sendHelp(sender, label);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "status" -> sendStatus(sender);
            case "reload" -> {
                plugin.reloadAntiFreeamConfig();
                sender.sendMessage(Component.text("[AntiFreeam] Config reloaded.", NamedTextColor.GREEN));
            }
            case "check" -> {
                if (args.length < 2) { sender.sendMessage(Component.text("Usage: /" + label + " check <player>", NamedTextColor.YELLOW)); return true; }
                checkPlayer(sender, args[1]);
            }
            case "debug" -> {
                if (args.length < 2) { sender.sendMessage(Component.text("Usage: /" + label + " debug <player>", NamedTextColor.YELLOW)); return true; }
                debugPlayer(sender, args[1]);
            }
            case "flag" -> {
                if (args.length < 2) { sender.sendMessage(Component.text("Usage: /" + label + " flag <player>", NamedTextColor.YELLOW)); return true; }
                flagPlayer(sender, args[1], true);
            }
            case "unflag" -> {
                if (args.length < 2) { sender.sendMessage(Component.text("Usage: /" + label + " unflag <player>", NamedTextColor.YELLOW)); return true; }
                flagPlayer(sender, args[1], false);
            }
            case "whitelist" -> {
                if (args.length < 2) { sender.sendMessage(Component.text("Usage: /" + label + " whitelist <player>", NamedTextColor.YELLOW)); return true; }
                whitelistPlayer(sender, args[1], true);
            }
            case "unwhitelist" -> {
                if (args.length < 2) { sender.sendMessage(Component.text("Usage: /" + label + " unwhitelist <player>", NamedTextColor.YELLOW)); return true; }
                whitelistPlayer(sender, args[1], false);
            }
            default -> sendHelp(sender, label);
        }
        return true;
    }

    private void sendStatus(CommandSender sender) {
        long flagged = Bukkit.getOnlinePlayers().stream()
            .filter(p -> injector.hasVoidEffect(p.getUniqueId()))
            .count();
        sender.sendMessage(Component.text("[AntiFreeam] ", NamedTextColor.GOLD)
            .append(Component.text("Online: " + Bukkit.getOnlinePlayers().size() +
                " | Flagged: " + flagged, NamedTextColor.WHITE)));
    }

    private void checkPlayer(CommandSender sender, String name) {
        Player target = Bukkit.getPlayerExact(name);
        if (target == null) {
            sender.sendMessage(Component.text("Player not found: " + name, NamedTextColor.RED));
            return;
        }
        boolean flagged = injector.hasVoidEffect(target.getUniqueId());
        boolean whitelisted = detector.isWhitelisted(target.getUniqueId());
        sender.sendMessage(Component.text("[AntiFreeam] " + name + ": ",NamedTextColor.GOLD)
            .append(Component.text(
                flagged ? "FLAGGED" : "clean",
                flagged ? NamedTextColor.RED : NamedTextColor.GREEN))
            .append(Component.text(whitelisted ? " [whitelisted]" : "", NamedTextColor.GRAY)));
    }

    private void debugPlayer(CommandSender sender, String name) {
        Player target = Bukkit.getPlayerExact(name);
        if (target == null) {
            sender.sendMessage(Component.text("Player not found: " + name, NamedTextColor.RED));
            return;
        }
        sender.sendMessage(Component.text("[AntiFreeam] " + name + ": ", NamedTextColor.GOLD)
            .append(Component.text(detector.debugSnapshot(target.getUniqueId()), NamedTextColor.WHITE)));
    }

    private void whitelistPlayer(CommandSender sender, String name, boolean add) {
        Player target = Bukkit.getPlayerExact(name);
        if (target == null) {
            sender.sendMessage(Component.text("Player not found: " + name, NamedTextColor.RED));
            return;
        }
        UUID uuid = target.getUniqueId();
        if (add) {
            detector.whitelistPlayer(uuid);
            injector.removeVoidEffect(target);
            sender.sendMessage(Component.text("[AntiFreeam] " + name + " added to whitelist.", NamedTextColor.GREEN));
        } else {
            detector.unwhitelistPlayer(uuid);
            sender.sendMessage(Component.text("[AntiFreeam] " + name + " removed from whitelist.", NamedTextColor.YELLOW));
        }
    }

    private void flagPlayer(CommandSender sender, String name, boolean flag) {
        Player target = Bukkit.getPlayerExact(name);
        if (target == null) {
            sender.sendMessage(Component.text("Player not found: " + name, NamedTextColor.RED));
            return;
        }
        if (flag) {
            injector.applyVoidEffect(target);
            sender.sendMessage(Component.text("[AntiFreeam] Void effect applied to " + name + ". Check if they see darkness.", NamedTextColor.GREEN));
        } else {
            injector.removeVoidEffect(target);
            sender.sendMessage(Component.text("[AntiFreeam] Void effect removed from " + name + ".", NamedTextColor.YELLOW));
        }
    }

    private void sendHelp(CommandSender sender, String label) {
        sender.sendMessage(Component.text("[AntiFreeam] Commands:", NamedTextColor.GOLD));
        sender.sendMessage(Component.text("/" + label + " status", NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("/" + label + " check <player>", NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("/" + label + " debug <player>", NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("/" + label + " flag <player>", NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("/" + label + " unflag <player>", NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("/" + label + " whitelist <player>", NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("/" + label + " unwhitelist <player>", NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("/" + label + " reload", NamedTextColor.YELLOW));
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                       @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            return Arrays.asList("status", "check", "debug", "flag", "unflag", "whitelist", "unwhitelist", "reload");
        }
        if (args.length == 2 && !args[0].equalsIgnoreCase("status") && !args[0].equalsIgnoreCase("reload")) {
            return Bukkit.getOnlinePlayers().stream().map(Player::getName).toList();
        }
        return List.of();
    }
}

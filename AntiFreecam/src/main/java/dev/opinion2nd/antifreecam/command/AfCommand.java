package dev.opinion2nd.antifreecam.command;

import dev.opinion2nd.antifreecam.AntiFreecamPlugin;
import dev.opinion2nd.antifreecam.mask.MaskService;
import dev.opinion2nd.antifreecam.mask.PlayerMaskData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public final class AfCommand implements CommandExecutor, TabCompleter {

    private final AntiFreecamPlugin plugin;
    private final MaskService service;

    public AfCommand(AntiFreecamPlugin plugin, MaskService service) {
        this.plugin = plugin;
        this.service = service;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(ChatColor.AQUA + "AntiFreecam " + plugin.getDescription().getVersion()
                    + ChatColor.GRAY + " — /" + label + " <reload|bypass [player]>");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload" -> {
                if (!sender.hasPermission("antifreecam.reload")) {
                    sender.sendMessage(ChatColor.RED + "No permission.");
                    return true;
                }
                plugin.reloadConfiguration();
                sender.sendMessage(ChatColor.GREEN + "AntiFreecam configuration reloaded.");
            }
            case "bypass" -> {
                if (!sender.hasPermission("antifreecam.bypass")) {
                    sender.sendMessage(ChatColor.RED + "No permission.");
                    return true;
                }
                Player target = (args.length >= 2) ? Bukkit.getPlayerExact(args[1])
                        : (sender instanceof Player p ? p : null);
                if (target == null) {
                    sender.sendMessage(ChatColor.RED + "Player not found / specify a player name.");
                    return true;
                }
                PlayerMaskData data = service.getOrCreate(target);
                data.bypass = !data.bypass;
                sender.sendMessage(ChatColor.GREEN + "Masking bypass for " + target.getName()
                        + " is now " + (data.bypass ? "ON" : "OFF") + ".");
            }
            default -> sender.sendMessage(ChatColor.RED + "Unknown sub-command. Use reload | bypass.");
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("reload", "bypass").stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("bypass")) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(n -> n.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}

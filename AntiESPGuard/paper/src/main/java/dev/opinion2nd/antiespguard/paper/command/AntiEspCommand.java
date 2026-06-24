package dev.opinion2nd.antiespguard.paper.command;

import dev.opinion2nd.antiespguard.paper.AntiEspGuardPlugin;
import dev.opinion2nd.antiespguard.paper.mask.ChunkResender;
import dev.opinion2nd.antiespguard.paper.mask.MaskService;
import dev.opinion2nd.antiespguard.paper.mask.PlayerMaskData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public final class AntiEspCommand implements CommandExecutor, TabCompleter {

    private final AntiEspGuardPlugin plugin;
    private final MaskService service;
    private final ChunkResender resender;

    public AntiEspCommand(AntiEspGuardPlugin plugin, MaskService service, ChunkResender resender) {
        this.plugin = plugin;
        this.service = service;
        this.resender = resender;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(ChatColor.AQUA + "AntiESPGuard " + plugin.getDescription().getVersion()
                    + ChatColor.GRAY + " — /" + label + " <reload|bypass [player]|status>");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "status" -> {
                if (!sender.hasPermission("antiespguard.reload")) {
                    sender.sendMessage(ChatColor.RED + "No permission.");
                    return true;
                }
                sendStatus(sender);
            }
            case "reload" -> {
                if (!sender.hasPermission("antiespguard.reload")) {
                    sender.sendMessage(ChatColor.RED + "No permission.");
                    return true;
                }
                plugin.reloadConfiguration();
                sender.sendMessage(ChatColor.GREEN + "AntiESPGuard configuration reloaded.");
            }
            case "bypass" -> {
                if (!sender.hasPermission("antiespguard.bypass")) {
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
            default -> sender.sendMessage(ChatColor.RED + "Unknown sub-command. Use reload | bypass | status.");
        }
        return true;
    }

    /** Report server version + whether progressive reveal (chunk re-send) works. */
    private void sendStatus(CommandSender sender) {
        Player sample = (sender instanceof Player p) ? p
                : Bukkit.getOnlinePlayers().stream().findFirst().orElse(null);
        if (sample != null) {
            resender.ensureReady(sample); // force init so the result is meaningful
        }
        sender.sendMessage(ChatColor.AQUA + "AntiESPGuard " + plugin.getDescription().getVersion()
                + ChatColor.AQUA + " status");
        sender.sendMessage(ChatColor.GRAY + " Server: " + ChatColor.WHITE + Bukkit.getVersion());
        sender.sendMessage(ChatColor.GRAY + " API: " + ChatColor.WHITE + Bukkit.getBukkitVersion());
        sender.sendMessage(ChatColor.GRAY + " Progressive reveal: "
                + (resender.isBroken() ? ChatColor.RED : ChatColor.GREEN) + resender.diagnostics());
        if (sample == null) {
            sender.sendMessage(ChatColor.YELLOW
                    + " (a player must be online to test reveal — join, then run this again.)");
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return new ArrayList<>(List.of("reload", "bypass", "status")).stream()
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

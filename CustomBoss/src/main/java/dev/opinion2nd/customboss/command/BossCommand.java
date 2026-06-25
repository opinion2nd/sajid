package dev.opinion2nd.customboss.command;

import dev.opinion2nd.customboss.BossManager;
import dev.opinion2nd.customboss.CustomBoss;
import dev.opinion2nd.customboss.CustomBossPlugin;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class BossCommand implements CommandExecutor, TabCompleter {

    private static final List<String> SUBCOMMANDS = List.of("spawn", "killall", "list", "reload");
    private static final String PREFIX = ChatColor.GOLD + "[CustomBoss] " + ChatColor.RESET;

    private final CustomBossPlugin plugin;
    private final BossManager manager;

    public BossCommand(CustomBossPlugin plugin, BossManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            usage(sender, label);
            return true;
        }

        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "spawn" -> spawn(sender);
            case "killall" -> killAll(sender);
            case "list" -> list(sender);
            case "reload" -> reload(sender);
            default -> usage(sender, label);
        }
        return true;
    }

    private void spawn(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(PREFIX + ChatColor.RED + "Only players can spawn a boss.");
            return;
        }
        CustomBoss boss = manager.spawn(plugin.getSettings(), player.getLocation());
        if (boss == null) {
            player.sendMessage(PREFIX + ChatColor.RED + "Could not spawn the boss — check the entity-type in config.yml.");
            return;
        }
        player.sendMessage(PREFIX + ChatColor.GREEN + "Spawned "
                + ChatColor.translateAlternateColorCodes('&', boss.getSettings().displayName)
                + ChatColor.GREEN + ".");
    }

    private void killAll(CommandSender sender) {
        int removed = manager.killAll();
        sender.sendMessage(PREFIX + ChatColor.GREEN + "Removed " + removed + " boss(es).");
    }

    private void list(CommandSender sender) {
        sender.sendMessage(PREFIX + ChatColor.YELLOW + "Active bosses: " + manager.count());
    }

    private void reload(CommandSender sender) {
        plugin.reloadSettings();
        sender.sendMessage(PREFIX + ChatColor.GREEN + "Configuration reloaded. New settings apply to bosses spawned from now on.");
    }

    private void usage(CommandSender sender, String label) {
        sender.sendMessage(PREFIX + ChatColor.YELLOW + "Usage:");
        sender.sendMessage(ChatColor.GRAY + "/" + label + " spawn " + ChatColor.DARK_GRAY + "- spawn a boss at your location");
        sender.sendMessage(ChatColor.GRAY + "/" + label + " killall " + ChatColor.DARK_GRAY + "- remove all active bosses");
        sender.sendMessage(ChatColor.GRAY + "/" + label + " list " + ChatColor.DARK_GRAY + "- show active boss count");
        sender.sendMessage(ChatColor.GRAY + "/" + label + " reload " + ChatColor.DARK_GRAY + "- reload config.yml");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            String partial = args[0].toLowerCase(Locale.ROOT);
            List<String> matches = new ArrayList<>();
            for (String sub : SUBCOMMANDS) {
                if (sub.startsWith(partial)) {
                    matches.add(sub);
                }
            }
            return matches;
        }
        return List.of();
    }
}

package dev.opinion2nd.freecamguard.command;

import dev.opinion2nd.freecamguard.FreecamGuardPlugin;
import dev.opinion2nd.freecamguard.detect.SignProbeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

public final class FreecamGuardCommand implements CommandExecutor, TabCompleter {

    private final FreecamGuardPlugin plugin;

    public FreecamGuardCommand(FreecamGuardPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage("§c[FreecamGuard] §7Usage: /" + label + " <reload|status>");
            return true;
        }
        switch (args[0].toLowerCase()) {
            case "reload" -> {
                plugin.reloadEverything();
                sender.sendMessage("§c[FreecamGuard] §aConfig reloaded. Detecting: §f"
                        + String.join(", ", SignProbeListener.activeModNames(plugin)));
            }
            case "status" -> {
                List<String> mods = SignProbeListener.activeModNames(plugin);
                sender.sendMessage("§c[FreecamGuard] §7Sign probe: §f"
                        + (mods.isEmpty() ? "idle" : String.join(", ", mods)));
                sender.sendMessage("§7Auto-kick: §f"
                        + plugin.getConfig().getBoolean("modDetection.autoKick", true)
                        + " §7| Brand/channel kick: §f"
                        + plugin.getConfig().getBoolean("brandDetection.kickOnCheatChannel", true));
            }
            default -> sender.sendMessage("§c[FreecamGuard] §7Usage: /" + label + " <reload|status>");
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> out = new ArrayList<>();
            for (String option : Arrays.asList("reload", "status")) {
                if (option.startsWith(args[0].toLowerCase())) {
                    out.add(option);
                }
            }
            return out;
        }
        return List.of();
    }
}

package com.ultimatedungeon.commands.framework;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Base class for top-level plugin commands.
 *
 * <p>Handles sub-command dispatching, permission checking, and tab completion
 * automatically. Concrete commands register their sub-commands in their
 * constructor via {@link #register(ISubCommand)}.</p>
 */
public abstract class AbstractCommand implements CommandExecutor, TabCompleter {

    private final Map<String, ISubCommand> subCommands = new LinkedHashMap<>();
    private final CommandPermissionChecker permissionChecker;

    protected AbstractCommand(@NotNull final CommandPermissionChecker permissionChecker) {
        this.permissionChecker = permissionChecker;
    }

    protected void register(@NotNull final ISubCommand subCommand) {
        subCommands.put(subCommand.getName().toLowerCase(), subCommand);
    }

    @Override
    public boolean onCommand(
            @NotNull final CommandSender sender,
            @NotNull final Command command,
            @NotNull final String label,
            @NotNull final String[] args
    ) {
        if (args.length == 0) {
            sendUsage(sender);
            return true;
        }

        final ISubCommand sub = subCommands.get(args[0].toLowerCase());
        if (sub == null) {
            sendUsage(sender);
            return true;
        }

        if (!permissionChecker.hasPermission(sender, sub.getPermission())) {
            return true;
        }

        sub.execute(sender, Arrays.copyOfRange(args, 1, args.length));
        return true;
    }

    @Override
    public List<String> onTabComplete(
            @NotNull final CommandSender sender,
            @NotNull final Command command,
            @NotNull final String label,
            @NotNull final String[] args
    ) {
        if (args.length == 1) {
            final List<String> completions = new ArrayList<>();
            for (final Map.Entry<String, ISubCommand> entry : subCommands.entrySet()) {
                if (entry.getKey().startsWith(args[0].toLowerCase())
                        && permissionChecker.hasPermission(sender, entry.getValue().getPermission())) {
                    completions.add(entry.getKey());
                }
            }
            return completions;
        }

        final ISubCommand sub = subCommands.get(args[0].toLowerCase());
        if (sub != null && permissionChecker.hasPermission(sender, sub.getPermission())) {
            return sub.tabComplete(sender, Arrays.copyOfRange(args, 1, args.length));
        }

        return List.of();
    }

    protected abstract void sendUsage(@NotNull CommandSender sender);
}

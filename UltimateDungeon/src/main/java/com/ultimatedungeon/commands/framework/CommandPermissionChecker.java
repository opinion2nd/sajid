package com.ultimatedungeon.commands.framework;

import com.ultimatedungeon.core.PluginLogger;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

/** Validates permissions at every command entry point and notifies the sender on failure. */
public final class CommandPermissionChecker {

    private final PluginLogger logger;

    public CommandPermissionChecker(@NotNull final PluginLogger logger) {
        this.logger = logger;
    }

    public boolean hasPermission(
            @NotNull final CommandSender sender,
            @NotNull final String permission
    ) {
        if (sender.hasPermission(permission)) {
            return true;
        }
        // Notify sender — message text loaded from MessagesConfig in Phase 1.
        sender.sendMessage("§cYou do not have permission to do that.");
        return false;
    }
}

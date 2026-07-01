package com.ultimatedungeon.commands.subcommands.dungeon;

import com.ultimatedungeon.commands.framework.ISubCommand;
import com.ultimatedungeon.dungeon.instance.DungeonInstanceManager;
import com.ultimatedungeon.util.MiniMessageUtil;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/** {@code /dungeon admin <info>} — administrative controls. */
public final class DungeonAdminSubCommand implements ISubCommand {

    private final DungeonInstanceManager instanceManager;

    public DungeonAdminSubCommand(@NotNull final DungeonInstanceManager instanceManager) {
        this.instanceManager = instanceManager;
    }

    @Override @NotNull public String getName() { return "admin"; }
    @Override @NotNull public String getPermission() { return "dungeon.admin"; }

    @Override
    public void execute(@NotNull final CommandSender sender, @NotNull final String[] args) {
        final String action = args.length > 0 ? args[0].toLowerCase() : "info";
        if ("info".equals(action)) {
            MiniMessageUtil.send(sender, "<gold>Active dungeon instances: <white>"
                    + instanceManager.getActiveCount());
        } else {
            MiniMessageUtil.send(sender, "<yellow>Usage: /dungeon admin info");
        }
    }

    @Override @NotNull
    public List<String> tabComplete(@NotNull final CommandSender sender, @NotNull final String[] args) {
        return args.length == 1 ? List.of("info") : List.of();
    }
}

package com.ultimatedungeon.commands.subcommands.dungeon;

import com.ultimatedungeon.commands.framework.ISubCommand;
import com.ultimatedungeon.config.files.MessagesConfig;
import com.ultimatedungeon.dungeon.instance.DungeonInstanceManager;
import com.ultimatedungeon.dungeon.lifecycle.DungeonLauncher;
import com.ultimatedungeon.util.MiniMessageUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/** {@code /dungeon leave} — leaves the current dungeon and returns home. */
public final class DungeonLeaveSubCommand implements ISubCommand {

    private final DungeonLauncher launcher;
    private final DungeonInstanceManager instanceManager;
    private final MessagesConfig messages;

    public DungeonLeaveSubCommand(@NotNull final DungeonLauncher launcher,
                                  @NotNull final DungeonInstanceManager instanceManager,
                                  @NotNull final MessagesConfig messages) {
        this.launcher = launcher;
        this.instanceManager = instanceManager;
        this.messages = messages;
    }

    @Override @NotNull public String getName() { return "leave"; }
    @Override @NotNull public String getPermission() { return "dungeon.use"; }

    @Override
    public void execute(@NotNull final CommandSender sender, @NotNull final String[] args) {
        if (!(sender instanceof final Player player)) return;
        if (!instanceManager.isPlayerInDungeon(player)) {
            MiniMessageUtil.send(player, messages.getPrefix() + messages.getDungeonNotIn());
            return;
        }
        launcher.leave(player);
        MiniMessageUtil.send(player, messages.getPrefix() + messages.getDungeonLeaveSuccess());
    }

    @Override @NotNull
    public List<String> tabComplete(@NotNull final CommandSender sender, @NotNull final String[] args) {
        return List.of();
    }
}

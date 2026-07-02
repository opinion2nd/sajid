package com.ultimatedungeon.commands.subcommands.dungeon;

import com.ultimatedungeon.api.dungeon.IDungeonInstance;
import com.ultimatedungeon.commands.framework.ISubCommand;
import com.ultimatedungeon.config.files.DifficultyConfig;
import com.ultimatedungeon.dungeon.instance.DungeonInstance;
import com.ultimatedungeon.dungeon.instance.DungeonInstanceManager;
import com.ultimatedungeon.room.model.RoomData;
import com.ultimatedungeon.util.MiniMessageUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * {@code /dungeon join <level>} — joins a running dungeon of the given level
 * at its spawn room (e.g. to catch up with friends already inside).
 */
public final class DungeonJoinSubCommand implements ISubCommand {

    private final DungeonInstanceManager instanceManager;
    private final DifficultyConfig difficultyConfig;

    public DungeonJoinSubCommand(@NotNull final DungeonInstanceManager instanceManager,
                                 @NotNull final DifficultyConfig difficultyConfig) {
        this.instanceManager = instanceManager;
        this.difficultyConfig = difficultyConfig;
    }

    @Override @NotNull public String getName() { return "join"; }
    @Override @NotNull public String getPermission() { return "dungeon.use"; }

    @Override
    public void execute(@NotNull final CommandSender sender, @NotNull final String[] args) {
        if (!(sender instanceof final Player player)) {
            MiniMessageUtil.send(sender, "<red>Only players can join a dungeon.");
            return;
        }
        if (instanceManager.isPlayerInDungeon(player)) {
            MiniMessageUtil.send(player, "<red>You are already inside a dungeon.");
            return;
        }
        final String level = args.length > 0 ? args[0]
                : difficultyConfig.getPresetIds().stream().findFirst().orElse("level_1");

        for (final IDungeonInstance raw : instanceManager.getActiveInstances()) {
            if (!(raw instanceof final DungeonInstance instance) || !instance.isActive()) continue;
            if (!instance.getContext().getRequest().getDifficultyId().equalsIgnoreCase(level)) continue;
            final RoomData spawn = instance.getRoomGraph() != null
                    ? instance.getRoomGraph().getSpawnRoom() : null;
            if (spawn == null) continue;
            instanceManager.associatePlayer(player, instance.getInstanceId());
            player.teleport(spawn.getCentre());
            MiniMessageUtil.send(player, "<green>Joined a running <yellow>" + level + "</yellow> dungeon!");
            return;
        }
        MiniMessageUtil.send(player, "<red>No running dungeon found for level <yellow>" + level
                + "</yellow>. Start one with <white>/dungeon solo</white>.");
    }

    @Override
    @NotNull
    public List<String> tabComplete(@NotNull final CommandSender sender, @NotNull final String[] args) {
        if (args.length == 1) return new ArrayList<>(difficultyConfig.getPresetIds());
        return List.of();
    }
}

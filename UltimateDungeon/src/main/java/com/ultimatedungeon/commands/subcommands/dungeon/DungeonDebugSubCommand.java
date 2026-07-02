package com.ultimatedungeon.commands.subcommands.dungeon;

import com.ultimatedungeon.api.dungeon.IDungeonInstance;
import com.ultimatedungeon.commands.framework.ISubCommand;
import com.ultimatedungeon.dungeon.instance.DungeonInstance;
import com.ultimatedungeon.dungeon.instance.DungeonInstanceManager;
import com.ultimatedungeon.room.model.RoomData;
import com.ultimatedungeon.room.model.RoomGraph;
import com.ultimatedungeon.util.MiniMessageUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * {@code /dungeon debug} — admin diagnostics for the dungeon the sender is in:
 * lists every room (type, cleared state, position) and the corridor count, so
 * layout issues can be inspected live without opening the world in a viewer.
 */
public final class DungeonDebugSubCommand implements ISubCommand {

    private final DungeonInstanceManager instanceManager;

    public DungeonDebugSubCommand(@NotNull final DungeonInstanceManager instanceManager) {
        this.instanceManager = instanceManager;
    }

    @Override @NotNull public String getName() { return "debug"; }
    @Override @NotNull public String getPermission() { return "dungeon.admin"; }

    @Override
    public void execute(@NotNull final CommandSender sender, @NotNull final String[] args) {
        if (!(sender instanceof final Player player)) return;
        final IDungeonInstance raw = instanceManager.getInstanceForPlayer(player);
        if (!(raw instanceof final DungeonInstance instance) || instance.getRoomGraph() == null) {
            MiniMessageUtil.send(player, "<red>You are not inside a dungeon.");
            return;
        }
        final RoomGraph graph = instance.getRoomGraph();
        MiniMessageUtil.send(player, "<gold><bold>Dungeon layout</bold> <gray>("
                + graph.getRoomCount() + " rooms, " + graph.getConnections().size()
                + " corridors, " + graph.getBossRoomIds().size() + " boss room(s))");
        for (final RoomData room : graph.getRooms()) {
            final String state = room.isCleared() ? "<green>✔"
                    : room.isEntered() ? "<yellow>…" : "<dark_gray>·";
            MiniMessageUtil.send(player, state + " <white>" + room.getType()
                    + " <gray>@ " + room.getOrigin().getBlockX() + "," + room.getOrigin().getBlockZ()
                    + " <dark_gray>(" + room.getRoomId() + ")");
        }
    }

    @Override @NotNull
    public List<String> tabComplete(@NotNull final CommandSender sender, @NotNull final String[] args) {
        return List.of();
    }
}

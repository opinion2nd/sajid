package com.ultimatedungeon.listeners.arena;

import com.ultimatedungeon.api.dungeon.IDungeonInstance;
import com.ultimatedungeon.boss.arena.ArenaLockdownManager;
import com.ultimatedungeon.dungeon.instance.DungeonInstanceManager;
import com.ultimatedungeon.util.MiniMessageUtil;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

/** Blocks escape/teleport commands while a player is locked in a boss arena. */
public final class ArenaCommandBlockListener implements Listener {

    private static final Set<String> BLOCKED = Set.of(
            "/tp", "/teleport", "/tpa", "/tpaccept", "/tpahere", "/tphere",
            "/spawn", "/home", "/homes", "/warp", "/warps", "/back",
            "/ehome", "/espawn", "/ewarp", "/etp", "/rtp", "/wild", "/lobby", "/hub");

    private final ArenaLockdownManager lockdown;
    private final DungeonInstanceManager instanceManager;

    public ArenaCommandBlockListener(@NotNull final ArenaLockdownManager lockdown,
                                     @NotNull final DungeonInstanceManager instanceManager) {
        this.lockdown = lockdown;
        this.instanceManager = instanceManager;
    }

    @EventHandler
    public void onCommand(@NotNull final PlayerCommandPreprocessEvent event) {
        if (event.getPlayer().hasPermission("dungeon.bypass")) return;
        final IDungeonInstance instance = instanceManager.getInstanceForPlayer(event.getPlayer());
        if (instance == null || !lockdown.isLocked(instance.getInstanceId())) return;

        final String root = event.getMessage().split(" ")[0].toLowerCase();
        if (BLOCKED.contains(root)) {
            event.setCancelled(true);
            MiniMessageUtil.send(event.getPlayer(), "<red>You cannot escape the arena while the boss lives!");
        }
    }
}

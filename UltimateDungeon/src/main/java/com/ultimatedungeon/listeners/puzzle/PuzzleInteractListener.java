package com.ultimatedungeon.listeners.puzzle;

import com.ultimatedungeon.api.dungeon.IDungeonInstance;
import com.ultimatedungeon.dungeon.instance.DungeonInstanceManager;
import com.ultimatedungeon.puzzle.engine.PuzzleEngine;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.jetbrains.annotations.NotNull;

/** Routes block interactions in a dungeon to that instance's active puzzle. */
public final class PuzzleInteractListener implements Listener {

    private final PuzzleEngine puzzleEngine;
    private final DungeonInstanceManager instanceManager;

    public PuzzleInteractListener(@NotNull final PuzzleEngine puzzleEngine,
                                  @NotNull final DungeonInstanceManager instanceManager) {
        this.puzzleEngine = puzzleEngine;
        this.instanceManager = instanceManager;
    }

    @EventHandler
    public void onInteract(@NotNull final PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK && event.getAction() != Action.LEFT_CLICK_BLOCK) {
            return;
        }
        final IDungeonInstance instance = instanceManager.getInstanceForPlayer(event.getPlayer());
        if (instance == null) return;
        if (puzzleEngine.hasActivePuzzle(instance.getInstanceId())) {
            puzzleEngine.handleInteract(instance.getInstanceId(), event.getPlayer());
        }
    }
}

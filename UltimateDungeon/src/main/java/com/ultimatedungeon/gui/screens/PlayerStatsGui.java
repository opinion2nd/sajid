package com.ultimatedungeon.gui.screens;

import com.ultimatedungeon.gui.framework.AbstractGui;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Personal statistics screen.
 *
 * <p>The {@code /dungeon stats} command provides the same information in chat;
 * this GUI hook is retained for layout configuration in {@code gui.yml}.</p>
 */
public final class PlayerStatsGui extends AbstractGui {

    public PlayerStatsGui(@NotNull final Player viewer) {
        super(viewer);
    }

    @Override public void open() { viewer.performCommand("dungeon stats"); }
    @Override public void refresh() { }
    @Override public void handleClick(final int slot) { }
}

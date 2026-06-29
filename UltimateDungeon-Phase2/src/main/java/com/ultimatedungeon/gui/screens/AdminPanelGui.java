package com.ultimatedungeon.gui.screens;

import com.ultimatedungeon.gui.framework.AbstractGui;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * AdminPanelGui — GUI screen implementation.
 * <p>Layout and items configured in gui.yml. Implemented in Milestone 5.</p>
 */
public final class AdminPanelGui extends AbstractGui {

    public AdminPanelGui(@NotNull final Player viewer) {
        super(viewer);
    }

    @Override public void open() {}
    @Override public void refresh() {}
    @Override public void handleClick(final int slot) {}
}

package com.ultimatedungeon.gui.screens;

import com.ultimatedungeon.gui.framework.AbstractGui;
import com.ultimatedungeon.gui.framework.GuiServices;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/** Standalone theme preview; delegates to the solo theme picker. */
public final class ThemeSelectGui extends AbstractGui {

    private final GuiServices services;

    public ThemeSelectGui(@NotNull final Player viewer, @NotNull final GuiServices services) {
        super(viewer);
        this.services = services;
    }

    @Override public void open() { new SoloDungeonGui(viewer, services).open(); }
    @Override public void refresh() { }
    @Override public void handleClick(final int slot) { }
}

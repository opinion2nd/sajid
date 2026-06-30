package com.ultimatedungeon.gui.framework;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

/**
 * Base class for all UltimateDungeon GUI screens.
 *
 * <p>Provides the common contract: open, close, refresh, and click handling.
 * Every concrete screen extends this class and registers itself with
 * {@link GuiManager} so the {@link com.ultimatedungeon.listeners.gui.GuiClickListener}
 * can route clicks correctly.</p>
 *
 * <p><strong>Item theft prevention:</strong> All click events in any
 * UltimateDungeon GUI must be cancelled. This is enforced globally in
 * {@link com.ultimatedungeon.listeners.gui.GuiClickListener}.</p>
 */
public abstract class AbstractGui {

    protected final Player viewer;
    protected Inventory inventory;

    protected AbstractGui(@NotNull final Player viewer) {
        this.viewer = viewer;
    }

    /** Builds and opens the GUI for the viewer. */
    public abstract void open();

    /** Closes the GUI for the viewer. */
    public void close() {
        if (viewer.getOpenInventory().getTopInventory().equals(inventory)) {
            viewer.closeInventory();
        }
    }

    /** Re-renders changed slots without rebuilding the entire inventory. */
    public abstract void refresh();

    /** Handles a click event routed from {@link com.ultimatedungeon.listeners.gui.GuiClickListener}. */
    public abstract void handleClick(int slot);

    @NotNull
    public Inventory getInventory() {
        return inventory;
    }

    @NotNull
    public Player getViewer() {
        return viewer;
    }
}

package com.ultimatedungeon.listeners.gui;

import com.ultimatedungeon.gui.framework.AbstractGui;
import com.ultimatedungeon.gui.framework.GuiManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Routes clicks in UltimateDungeon GUIs to their screen and cancels every click
 * to prevent item theft, then clears tracking when the GUI closes.
 */
public final class GuiClickListener implements Listener {

    private final GuiManager guiManager;

    public GuiClickListener(@NotNull final GuiManager guiManager) {
        this.guiManager = guiManager;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onClick(@NotNull final InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof final Player player)) return;
        final AbstractGui gui = guiManager.getOpenGui(player);
        if (gui == null) return;
        if (!event.getInventory().equals(gui.getInventory())) return;
        event.setCancelled(true); // never allow item movement in plugin GUIs
        gui.handleClick(event.getRawSlot());
    }

    @EventHandler
    public void onClose(@NotNull final InventoryCloseEvent event) {
        if (event.getPlayer() instanceof final Player player) {
            final AbstractGui gui = guiManager.getOpenGui(player);
            if (gui != null && event.getInventory().equals(gui.getInventory())) {
                guiManager.unregister(player);
            }
        }
    }
}

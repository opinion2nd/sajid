package com.ultimatedungeon.listeners.gui;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Central GUI click handler.
 *
 * <p>Routes all inventory click events to the appropriate GUI screen handler
 * and prevents item theft by cancelling all events in UltimateDungeon GUIs.</p>
 */
public final class GuiClickListener implements Listener {

    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryClick(@NotNull final InventoryClickEvent event) {
        // Milestone 5: check if inventory belongs to a plugin GUI; route to handler.
    }
}

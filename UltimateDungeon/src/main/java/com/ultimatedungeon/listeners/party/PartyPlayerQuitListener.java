package com.ultimatedungeon.listeners.party;

import com.ultimatedungeon.party.manager.PartyManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Handles party cleanup when a player disconnects from the server.
 *
 * <p>Delegates to {@link PartyManager#handleDisconnect(org.bukkit.entity.Player)}
 * which decides whether to remove the player from their party or preserve
 * the slot for reconnect (when they are mid-dungeon).</p>
 */
public final class PartyPlayerQuitListener implements Listener {

    private final PartyManager partyManager;

    public PartyPlayerQuitListener(@NotNull final PartyManager partyManager) {
        this.partyManager = partyManager;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(@NotNull final PlayerQuitEvent event) {
        partyManager.handleDisconnect(event.getPlayer());
    }
}

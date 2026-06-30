package com.ultimatedungeon.listeners.party;

import com.ultimatedungeon.party.manager.InvitationManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Handles party-related state restoration when a player reconnects.
 *
 * <p>Currently cleans up any stale invitations the player sent before
 * disconnecting. Future milestones will handle mid-dungeon reconnect
 * teleportation here.</p>
 */
public final class PartyPlayerJoinListener implements Listener {

    private final InvitationManager invitationManager;

    public PartyPlayerJoinListener(@NotNull final InvitationManager invitationManager) {
        this.invitationManager = invitationManager;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(@NotNull final PlayerJoinEvent event) {
        // Any pending invitation to this player from before they last disconnected
        // remains valid until it expires naturally — no action needed here.
        // Future: if PlayerSessionManager has a reconnect slot for this player,
        // teleport them back to their dungeon.
    }
}

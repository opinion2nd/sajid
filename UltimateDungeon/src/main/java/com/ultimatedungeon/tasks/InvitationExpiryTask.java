package com.ultimatedungeon.tasks;

import com.ultimatedungeon.party.manager.InvitationManager;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

/**
 * Scheduled repeating task that purges expired party invitations.
 *
 * <p>Runs every 20 ticks (1 second) on the main thread.
 * Delegates to {@link InvitationManager#purgeExpired()} which handles
 * notifications and cleanup.</p>
 */
public final class InvitationExpiryTask extends BukkitRunnable {

    private final InvitationManager invitationManager;

    public InvitationExpiryTask(@NotNull final InvitationManager invitationManager) {
        this.invitationManager = invitationManager;
    }

    @Override
    public void run() {
        invitationManager.purgeExpired();
    }
}

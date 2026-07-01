package com.ultimatedungeon.services;

import com.ultimatedungeon.api.dungeon.DungeonGenerationRequest;
import com.ultimatedungeon.config.files.MessagesConfig;
import com.ultimatedungeon.core.PluginLogger;
import com.ultimatedungeon.dungeon.instance.DungeonInstanceManager;
import com.ultimatedungeon.dungeon.lifecycle.DungeonLauncher;
import com.ultimatedungeon.managers.CooldownManager;
import com.ultimatedungeon.theme.registry.ThemeRegistry;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

/**
 * Validates entry conditions and orchestrates dungeon launches.
 *
 * <p>This is the single public entry point used by commands and GUIs. It checks
 * the player is not already in a dungeon, is not on entry cooldown, and that the
 * requested theme and difficulty are valid before delegating to
 * {@link DungeonLauncher}.</p>
 */
public final class DungeonLaunchService {

    private static final String ENTRY_COOLDOWN_KEY = "dungeon_entry";
    private static final long ENTRY_COOLDOWN_MS = 5_000L;

    private final DungeonLauncher        launcher;
    private final DungeonInstanceManager instanceManager;
    private final CooldownManager        cooldowns;
    private final DifficultyService      difficultyService;
    private final ThemeRegistry          themeRegistry;
    private final NotificationService    notifications;
    private final MessagesConfig         messages;
    private final PluginLogger           logger;

    public DungeonLaunchService(@NotNull final DungeonLauncher launcher,
                                @NotNull final DungeonInstanceManager instanceManager,
                                @NotNull final CooldownManager cooldowns,
                                @NotNull final DifficultyService difficultyService,
                                @NotNull final ThemeRegistry themeRegistry,
                                @NotNull final NotificationService notifications,
                                @NotNull final MessagesConfig messages,
                                @NotNull final PluginLogger logger) {
        this.launcher = launcher;
        this.instanceManager = instanceManager;
        this.cooldowns = cooldowns;
        this.difficultyService = difficultyService;
        this.themeRegistry = themeRegistry;
        this.notifications = notifications;
        this.messages = messages;
        this.logger = logger;
    }

    /** Validates and launches a solo dungeon for one player. */
    public boolean launchSolo(@NotNull final Player player,
                              @NotNull final String themeId,
                              @NotNull final String difficultyId) {
        if (!validate(player, themeId, difficultyId)) return false;
        cooldowns.setCooldown(player, ENTRY_COOLDOWN_KEY, ENTRY_COOLDOWN_MS);
        final DungeonGenerationRequest request = new DungeonGenerationRequest(
                player.getUniqueId(), themeId, difficultyId, false, null);
        return launcher.launch(request, List.of(player));
    }

    /** Validates and launches a party dungeon for a leader and members. */
    public boolean launchParty(@NotNull final Player leader,
                               @NotNull final List<Player> members,
                               @Nullable final UUID partyId,
                               @NotNull final String themeId,
                               @NotNull final String difficultyId) {
        if (!validate(leader, themeId, difficultyId)) return false;
        for (final Player m : members) {
            if (instanceManager.isPlayerInDungeon(m)) {
                notifications.chat(leader, "<red>" + m.getName() + " is already in a dungeon.");
                return false;
            }
        }
        members.forEach(m -> cooldowns.setCooldown(m, ENTRY_COOLDOWN_KEY, ENTRY_COOLDOWN_MS));
        final DungeonGenerationRequest request = new DungeonGenerationRequest(
                leader.getUniqueId(), themeId, difficultyId, true, partyId);
        return launcher.launch(request, List.copyOf(members));
    }

    private boolean validate(@NotNull final Player player,
                             @NotNull final String themeId,
                             @NotNull final String difficultyId) {
        if (instanceManager.isPlayerInDungeon(player)) {
            notifications.chat(player, messages.getDungeonAlreadyIn());
            return false;
        }
        if (cooldowns.isOnCooldown(player, ENTRY_COOLDOWN_KEY)) {
            notifications.chat(player, "<red>Please wait before starting another dungeon.");
            return false;
        }
        if (themeRegistry.getTheme(themeId) == null) {
            notifications.chat(player, "<red>Unknown theme: " + themeId);
            return false;
        }
        if (!difficultyService.isValid(difficultyId)) {
            notifications.chat(player, "<red>Unknown difficulty: " + difficultyId);
            return false;
        }
        return true;
    }
}

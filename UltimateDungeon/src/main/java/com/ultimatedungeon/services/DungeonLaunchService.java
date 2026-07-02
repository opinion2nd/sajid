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
    private final DungeonKeyService      keyService;
    private final PluginLogger           logger;

    public DungeonLaunchService(@NotNull final DungeonLauncher launcher,
                                @NotNull final DungeonInstanceManager instanceManager,
                                @NotNull final CooldownManager cooldowns,
                                @NotNull final DifficultyService difficultyService,
                                @NotNull final ThemeRegistry themeRegistry,
                                @NotNull final NotificationService notifications,
                                @NotNull final MessagesConfig messages,
                                @NotNull final DungeonKeyService keyService,
                                @NotNull final PluginLogger logger) {
        this.launcher = launcher;
        this.instanceManager = instanceManager;
        this.cooldowns = cooldowns;
        this.difficultyService = difficultyService;
        this.themeRegistry = themeRegistry;
        this.notifications = notifications;
        this.messages = messages;
        this.keyService = keyService;
        this.logger = logger;
    }

    /** Validates and launches a solo dungeon for one player. */
    public boolean launchSolo(@NotNull final Player player,
                              @NotNull final String themeId,
                              @NotNull final String difficultyId) {
        // The theme is fixed by the dungeon level (level 1 = first theme … level 5
        // = fifth), so each level is a visually distinct dungeon.
        final String theme = themeForLevel(difficultyId);
        if (!validate(player, theme, difficultyId)) return false;
        if (!keyService.consumeForEntry(player, difficultyService.level(difficultyId))) return false;
        cooldowns.setCooldown(player, ENTRY_COOLDOWN_KEY, ENTRY_COOLDOWN_MS);
        final DungeonGenerationRequest request = new DungeonGenerationRequest(
                player.getUniqueId(), theme, difficultyId, false, null);
        return launcher.launch(request, List.of(player));
    }

    /** Resolves the fixed theme id for the given difficulty's level. */
    @NotNull
    private String themeForLevel(@NotNull final String difficultyId) {
        final java.util.List<com.ultimatedungeon.api.theme.ITheme> themes =
                new java.util.ArrayList<>(themeRegistry.getAllThemes());
        if (themes.isEmpty()) return "ancient_ruins";
        final int level = difficultyService.level(difficultyId);
        final int idx = Math.min(themes.size() - 1, Math.max(0, level - 1));
        return themes.get(idx).getThemeId();
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
        // Only the leader needs to spend a key for the party.
        if (!keyService.consumeForEntry(leader, difficultyService.level(difficultyId))) return false;
        members.forEach(m -> cooldowns.setCooldown(m, ENTRY_COOLDOWN_KEY, ENTRY_COOLDOWN_MS));
        final DungeonGenerationRequest request = new DungeonGenerationRequest(
                leader.getUniqueId(), themeForLevel(difficultyId), difficultyId, true, partyId);
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

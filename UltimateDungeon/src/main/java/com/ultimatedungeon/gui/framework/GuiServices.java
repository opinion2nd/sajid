package com.ultimatedungeon.gui.framework;

import com.ultimatedungeon.config.files.DifficultyConfig;
import com.ultimatedungeon.dungeon.instance.DungeonInstanceManager;
import com.ultimatedungeon.party.manager.InvitationManager;
import com.ultimatedungeon.party.manager.PartyManager;
import com.ultimatedungeon.party.manager.ReadyCheckManager;
import com.ultimatedungeon.theme.registry.ThemeRegistry;
import org.jetbrains.annotations.NotNull;

/**
 * Immutable bundle of the services GUI screens need, so screens can open one
 * another without threading every dependency through their constructors.
 */
public final class GuiServices {

    private final GuiManager guiManager;
    private final ThemeRegistry themeRegistry;
    private final DifficultyConfig difficultyConfig;
    private final PartyManager partyManager;
    private final DungeonInstanceManager instanceManager;
    private final InvitationManager invitationManager;
    private final ReadyCheckManager readyCheckManager;

    public GuiServices(@NotNull final GuiManager guiManager,
                       @NotNull final ThemeRegistry themeRegistry,
                       @NotNull final DifficultyConfig difficultyConfig,
                       @NotNull final PartyManager partyManager,
                       @NotNull final DungeonInstanceManager instanceManager,
                       @NotNull final InvitationManager invitationManager,
                       @NotNull final ReadyCheckManager readyCheckManager) {
        this.guiManager = guiManager;
        this.themeRegistry = themeRegistry;
        this.difficultyConfig = difficultyConfig;
        this.partyManager = partyManager;
        this.instanceManager = instanceManager;
        this.invitationManager = invitationManager;
        this.readyCheckManager = readyCheckManager;
    }

    @NotNull public GuiManager guiManager() { return guiManager; }
    @NotNull public ThemeRegistry themeRegistry() { return themeRegistry; }
    @NotNull public DifficultyConfig difficultyConfig() { return difficultyConfig; }
    @NotNull public PartyManager partyManager() { return partyManager; }
    @NotNull public DungeonInstanceManager instanceManager() { return instanceManager; }
    @NotNull public InvitationManager invitationManager() { return invitationManager; }
    @NotNull public ReadyCheckManager readyCheckManager() { return readyCheckManager; }
}

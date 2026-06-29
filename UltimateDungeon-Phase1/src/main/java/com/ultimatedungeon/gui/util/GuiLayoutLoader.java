package com.ultimatedungeon.gui.util;

import org.jetbrains.annotations.NotNull;

/** Loads GUI layout definitions from gui.yml for a given screen identifier. */
public final class GuiLayoutLoader {

    private GuiLayoutLoader() {}

    @NotNull
    public static GuiLayoutLoader create() {
        return new GuiLayoutLoader();
    }

    public int getSize(@NotNull final String screenId) {
        // Milestone 5: read from GuiConfig.
        return 54;
    }

    @NotNull
    public String getTitle(@NotNull final String screenId) {
        // Milestone 5: read from MessagesConfig.
        return screenId;
    }
}

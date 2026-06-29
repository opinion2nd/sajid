package com.ultimatedungeon.gui.framework;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.jetbrains.annotations.NotNull;

/** Routes click events from the GUI listener to the correct GuiItem action. */
public final class GuiClickHandler {

    private GuiClickHandler() {}

    /** Immutable context object passed to click action consumers. */
    public static final class ClickContext {
        private final Player player;
        private final int slot;
        private final ClickType clickType;

        public ClickContext(
                @NotNull final Player player,
                final int slot,
                @NotNull final ClickType clickType
        ) {
            this.player = player;
            this.slot = slot;
            this.clickType = clickType;
        }

        @NotNull public Player getPlayer() { return player; }
        public int getSlot() { return slot; }
        @NotNull public ClickType getClickType() { return clickType; }
    }
}

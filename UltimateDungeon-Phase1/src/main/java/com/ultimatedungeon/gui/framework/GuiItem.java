package com.ultimatedungeon.gui.framework;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

/**
 * A single clickable item within a GUI screen.
 *
 * <p>Combines an {@link ItemStack} with an optional click action, so
 * GUI screens can declaratively define their content and behaviour
 * without branching on slot numbers in click handlers.</p>
 */
public final class GuiItem {

    private final ItemStack itemStack;
    private final Consumer<GuiClickHandler.ClickContext> clickAction;

    public GuiItem(
            @NotNull final ItemStack itemStack,
            @Nullable final Consumer<GuiClickHandler.ClickContext> clickAction
    ) {
        this.itemStack = itemStack;
        this.clickAction = clickAction;
    }

    /** Creates a display-only item with no click action. */
    public GuiItem(@NotNull final ItemStack itemStack) {
        this(itemStack, null);
    }

    public void onClick(@NotNull final GuiClickHandler.ClickContext context) {
        if (clickAction != null) {
            clickAction.accept(context);
        }
    }

    @NotNull public ItemStack getItemStack() { return itemStack; }
    public boolean hasClickAction() { return clickAction != null; }
}

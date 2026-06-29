package com.ultimatedungeon.gui.framework;

import com.ultimatedungeon.core.PluginLogger;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks all currently open GUI screens per player.
 *
 * <p>Used by {@link com.ultimatedungeon.listeners.gui.GuiClickListener}
 * to route click events to the correct screen instance.</p>
 */
public final class GuiManager {

    private final PluginLogger logger;
    private final Map<UUID, AbstractGui> openGuis = new ConcurrentHashMap<>();

    public GuiManager(@NotNull final PluginLogger logger) {
        this.logger = logger;
    }

    public void register(@NotNull final Player player, @NotNull final AbstractGui gui) {
        openGuis.put(player.getUniqueId(), gui);
        logger.debug("Registered GUI for player: " + player.getName());
    }

    public void unregister(@NotNull final Player player) {
        openGuis.remove(player.getUniqueId());
    }

    @Nullable
    public AbstractGui getOpenGui(@NotNull final Player player) {
        return openGuis.get(player.getUniqueId());
    }

    public boolean hasOpenGui(@NotNull final Player player) {
        return openGuis.containsKey(player.getUniqueId());
    }

    public void closeAll() {
        openGuis.values().forEach(AbstractGui::close);
        openGuis.clear();
    }
}

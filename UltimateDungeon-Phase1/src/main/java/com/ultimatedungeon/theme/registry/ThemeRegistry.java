package com.ultimatedungeon.theme.registry;

import com.ultimatedungeon.api.theme.ITheme;
import com.ultimatedungeon.api.theme.IThemeRegistry;
import com.ultimatedungeon.core.PluginLogger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.*;

/** Holds all registered dungeon theme definitions. */
public final class ThemeRegistry implements IThemeRegistry {

    private final PluginLogger logger;
    private final Map<String, ITheme> themes = new LinkedHashMap<>();

    public ThemeRegistry(@NotNull final PluginLogger logger) {
        this.logger = logger;
    }

    @Override
    public void register(@NotNull final ITheme theme) {
        themes.put(theme.getThemeId(), theme);
        logger.debug("Registered theme: " + theme.getThemeId());
    }

    @Override
    @Nullable
    public ITheme getTheme(@NotNull final String themeId) {
        return themes.get(themeId);
    }

    @Override
    @NotNull
    public Collection<ITheme> getAllThemes() {
        return Collections.unmodifiableCollection(themes.values());
    }
}

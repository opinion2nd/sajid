package com.ultimatedungeon.api.theme;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.Collection;

/** Contract for dungeon theme registration and lookup. */
public interface IThemeRegistry {
    void register(@NotNull ITheme theme);
    @Nullable ITheme getTheme(@NotNull String themeId);
    @NotNull Collection<ITheme> getAllThemes();
}

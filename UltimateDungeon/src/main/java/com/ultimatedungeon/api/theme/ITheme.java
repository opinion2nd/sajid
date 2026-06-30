package com.ultimatedungeon.api.theme;

import org.jetbrains.annotations.NotNull;
import java.util.List;

/** Contract for a dungeon theme definition. */
public interface ITheme {
    @NotNull String getThemeId();
    @NotNull String getDisplayName();
    @NotNull List<String> getMonsterPool();
    @NotNull List<String> getBossPool();
}

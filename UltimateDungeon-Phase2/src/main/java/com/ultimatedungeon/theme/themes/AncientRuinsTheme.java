package com.ultimatedungeon.theme.themes;

import com.ultimatedungeon.api.theme.ITheme;
import org.jetbrains.annotations.NotNull;
import java.util.List;

/** AncientRuinsTheme — dungeon theme definition. Visual identity implemented in Milestone 2. */
public final class AncientRuinsTheme implements ITheme {

    @Override @NotNull public String getThemeId() { return "AncientRuinsTheme"; }
    @Override @NotNull public String getDisplayName() { return "AncientRuinsTheme"; }
    @Override @NotNull public List<String> getMonsterPool() { return List.of(); }
    @Override @NotNull public List<String> getBossPool() { return List.of(); }
}

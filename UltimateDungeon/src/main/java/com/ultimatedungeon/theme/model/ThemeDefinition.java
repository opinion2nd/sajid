package com.ultimatedungeon.theme.model;

import com.ultimatedungeon.api.theme.ITheme;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Complete definition of a dungeon theme — implements {@link ITheme} with
 * full palette, ambience, and monster pool.
 */
public final class ThemeDefinition implements ITheme {

    private final String           themeId;
    private final String           displayName;
    private final ThemeBlockPalette palette;
    private final ThemeAmbience    ambience;
    private final ThemeMonsterPool monsterPool;

    public ThemeDefinition(
            @NotNull final String           themeId,
            @NotNull final String           displayName,
            @NotNull final ThemeBlockPalette palette,
            @NotNull final ThemeAmbience    ambience,
            @NotNull final ThemeMonsterPool monsterPool
    ) {
        this.themeId     = themeId;
        this.displayName = displayName;
        this.palette     = palette;
        this.ambience    = ambience;
        this.monsterPool = monsterPool;
    }

    @Override @NotNull public String getThemeId()     { return themeId;     }
    @Override @NotNull public String getDisplayName() { return displayName; }

    @Override @NotNull
    public List<String> getMonsterPool() { return monsterPool.getMonsterIds(); }

    @Override @NotNull
    public List<String> getBossPool()    { return monsterPool.getBossIds();    }

    @NotNull public ThemeBlockPalette getPalette()     { return palette;     }
    @NotNull public ThemeAmbience     getAmbience()    { return ambience;    }
    @NotNull public ThemeMonsterPool  getMonsterPool2(){ return monsterPool; }
}

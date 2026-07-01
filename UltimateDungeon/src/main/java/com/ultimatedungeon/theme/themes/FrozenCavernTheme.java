package com.ultimatedungeon.theme.themes;

import com.ultimatedungeon.theme.model.*;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.jetbrains.annotations.NotNull;
import java.util.List;

/**
 * Theme: Frozen Cavern
 * Ice blocks, blizzard effects, frost monsters.
 */
public final class FrozenCavernTheme extends ThemeDefinition {
    public FrozenCavernTheme() {
        super("frozen_cavern", "Frozen Cavern",
            new ThemeBlockPalette(
                Material.PACKED_ICE, Material.BLUE_ICE,
                Material.SNOW_BLOCK, Material.ICE,
                Material.PACKED_ICE),
            new ThemeAmbience(Sound.BLOCK_POWDER_SNOW_STEP, 0.4f, 0.8f,
                List.of(Particle.SNOWFLAKE, Particle.CLOUD)),
            new ThemeMonsterPool(
                List.of("common_monster_3", "elite_monster_2"),
                List.of("boss_3")));
    }
}

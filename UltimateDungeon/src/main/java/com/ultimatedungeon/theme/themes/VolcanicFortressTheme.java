package com.ultimatedungeon.theme.themes;

import com.ultimatedungeon.theme.model.*;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.jetbrains.annotations.NotNull;
import java.util.List;

/**
 * Theme: Volcanic Fortress
 * Lava flows, heat haze, fire-based enemies.
 */
public final class VolcanicFortressTheme extends ThemeDefinition {
    public VolcanicFortressTheme() {
        super("volcanic_fortress", "Volcanic Fortress",
            new ThemeBlockPalette(
                Material.NETHER_BRICKS, Material.MAGMA_BLOCK,
                Material.BLACKSTONE, Material.NETHER_BRICKS,
                Material.BASALT),
            new ThemeAmbience(Sound.BLOCK_LAVA_AMBIENT, 0.6f, 1.0f,
                List.of(Particle.FLAME, Particle.LAVA)),
            new ThemeMonsterPool(
                List.of("common_monster_5", "elite_monster_1"),
                List.of("boss_6")));
    }
}

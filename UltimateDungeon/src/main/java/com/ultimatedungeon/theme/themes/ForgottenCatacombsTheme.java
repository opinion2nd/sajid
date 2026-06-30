package com.ultimatedungeon.theme.themes;

import com.ultimatedungeon.theme.model.*;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.jetbrains.annotations.NotNull;
import java.util.List;

/**
 * Theme: Forgotten Catacombs
 * Deepslate, bone blocks, dark lighting, death ambience.
 */
public final class ForgottenCatacombsTheme extends ThemeDefinition {
    public ForgottenCatacombsTheme() {
        super("forgotten_catacombs", "Forgotten Catacombs",
            new ThemeBlockPalette(
                Material.DEEPSLATE_BRICKS, Material.BONE_BLOCK,
                Material.CRACKED_DEEPSLATE_BRICKS, Material.DEEPSLATE_TILES,
                Material.DEEPSLATE),
            new ThemeAmbience(Sound.BLOCK_ROOTED_DIRT_PLACE, 0.3f, 0.6f,
                List.of(Particle.LARGE_SMOKE, Particle.WITCH)),
            new ThemeMonsterPool(
                List.of("common_monster_1", "rare_monster_1"),
                List.of("boss_7", "boss_8")));
    }
}

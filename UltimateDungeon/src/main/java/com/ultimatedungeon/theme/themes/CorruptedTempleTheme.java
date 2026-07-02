package com.ultimatedungeon.theme.themes;

import com.ultimatedungeon.theme.model.*;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.jetbrains.annotations.NotNull;
import java.util.List;

/**
 * Theme: Corrupted Temple
 * Purpur and blackstone, void energy, demon enemies.
 * Layout: symmetric axis — a grand processional nave with mirrored side chapels.
 */
public final class CorruptedTempleTheme extends ThemeDefinition {
    public CorruptedTempleTheme() {
        super("corrupted_temple", "Corrupted Temple",
            new ThemeBlockPalette(
                Material.PURPUR_BLOCK, Material.BLACKSTONE,
                Material.CRYING_OBSIDIAN, Material.PURPUR_BLOCK,
                Material.BLACKSTONE),
            new ThemeAmbience(Sound.BLOCK_SOUL_SAND_PLACE, 0.5f, 0.7f,
                List.of(Particle.SOUL_FIRE_FLAME, Particle.PORTAL)),
            new ThemeMonsterPool(
                List.of("common_monster_4", "elite_monster_3"),
                List.of("zharok")),
            LayoutStyle.SYMMETRIC_AXIS);
    }
}

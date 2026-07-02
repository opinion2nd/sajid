package com.ultimatedungeon.theme.themes;

import com.ultimatedungeon.theme.model.*;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.jetbrains.annotations.NotNull;
import java.util.List;

/**
 * Theme: Ancient Ruins
 * Crumbling stone, overgrown vegetation, lost civilisation.
 * Layout: hub-and-spoke — a ruined central plaza with collapsed wings radiating outward.
 */
public final class AncientRuinsTheme extends ThemeDefinition {
    public AncientRuinsTheme() {
        super("ancient_ruins", "Ancient Ruins",
            new ThemeBlockPalette(
                Material.COBBLESTONE, Material.MOSSY_COBBLESTONE,
                Material.MOSSY_STONE_BRICKS, Material.STONE_BRICKS,
                Material.CRACKED_STONE_BRICKS),
            new ThemeAmbience(Sound.BLOCK_CAVE_VINES_PLACE, 0.5f, 1.0f,
                List.of(Particle.FALLING_DUST, Particle.SMOKE)),
            new ThemeMonsterPool(
                List.of("common_monster_1", "common_monster_2", "elite_monster_1"),
                List.of("tharok", "sylvara")),
            LayoutStyle.HUB_AND_SPOKE);
    }
}

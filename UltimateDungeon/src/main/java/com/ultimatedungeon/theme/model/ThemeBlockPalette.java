package com.ultimatedungeon.theme.model;

import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

/**
 * Immutable set of materials that define the visual identity of a dungeon theme.
 *
 * <p>Used by {@link com.ultimatedungeon.dungeon.generation.RoomPlacer} and
 * {@link com.ultimatedungeon.dungeon.generation.DecorationPainter} to paint
 * walls, floors, ceilings, and accent blocks.</p>
 */
public final class ThemeBlockPalette {

    private final Material primary;   // main wall material
    private final Material secondary; // secondary wall / variation
    private final Material accent;    // decorative highlights
    private final Material floor;     // floor surface
    private final Material ceiling;   // ceiling surface
    private final Material air;       // always AIR — corridor / room interior

    public ThemeBlockPalette(
            @NotNull final Material primary,
            @NotNull final Material secondary,
            @NotNull final Material accent,
            @NotNull final Material floor,
            @NotNull final Material ceiling
    ) {
        this.primary   = primary;
        this.secondary = secondary;
        this.accent    = accent;
        this.floor     = floor;
        this.ceiling   = ceiling;
        this.air       = Material.AIR;
    }

    @NotNull public Material getPrimary()   { return primary;   }
    @NotNull public Material getSecondary() { return secondary; }
    @NotNull public Material getAccent()    { return accent;    }
    @NotNull public Material getFloor()     { return floor;     }
    @NotNull public Material getCeiling()   { return ceiling;   }
    @NotNull public Material getAir()       { return air;       }
}

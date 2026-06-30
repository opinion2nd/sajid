package com.ultimatedungeon.theme.model;

import org.bukkit.Particle;
import org.bukkit.Sound;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Immutable ambience configuration for a dungeon theme.
 * Carries the ambient sound and the list of particle effects.
 */
public final class ThemeAmbience {

    private final Sound        ambientSound;
    private final float        ambientVolume;
    private final float        ambientPitch;
    private final List<Particle> particles;

    public ThemeAmbience(
            @NotNull final Sound        ambientSound,
            final float                 ambientVolume,
            final float                 ambientPitch,
            @NotNull final List<Particle> particles
    ) {
        this.ambientSound  = ambientSound;
        this.ambientVolume = ambientVolume;
        this.ambientPitch  = ambientPitch;
        this.particles     = List.copyOf(particles);
    }

    @NotNull public Sound          getAmbientSound()   { return ambientSound;  }
    public float                   getAmbientVolume()  { return ambientVolume; }
    public float                   getAmbientPitch()   { return ambientPitch;  }
    @NotNull public List<Particle> getParticles()      { return particles;     }
}

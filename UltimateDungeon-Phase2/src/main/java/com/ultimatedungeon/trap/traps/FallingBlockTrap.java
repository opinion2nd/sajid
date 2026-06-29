package com.ultimatedungeon.trap.traps;

import com.ultimatedungeon.api.trap.ITrap;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

/** FallingBlockTrap — trap implementation. Milestone 4. */
public final class FallingBlockTrap implements ITrap {
    @Override @NotNull public String getTrapId() { return "FallingBlockTrap"; }
    @Override public void place(@NotNull final Location location) {}
    @Override public void activate() {}
    @Override public void reset() {}
    @Override public boolean isActive() { return false; }
}

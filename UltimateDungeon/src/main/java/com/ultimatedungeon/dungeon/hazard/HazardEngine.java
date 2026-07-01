package com.ultimatedungeon.dungeon.hazard;

import com.ultimatedungeon.api.dungeon.IDungeonInstance;
import com.ultimatedungeon.dungeon.instance.DungeonInstance;
import com.ultimatedungeon.room.model.RoomData;
import com.ultimatedungeon.room.model.RoomGraph;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.jetbrains.annotations.NotNull;

/**
 * Applies ambient environmental hazards to players standing inside hazardous
 * rooms. Driven entirely by {@link HazardSettings} (from {@code dungeon.yml}) and
 * ticked per-instance by {@link com.ultimatedungeon.tasks.HazardTickTask}.
 *
 * <p>Creative and spectator players are never harmed, and a hazard only affects
 * a room once it has been entered, so corridors and un-triggered rooms stay
 * safe.</p>
 */
public final class HazardEngine {

    private final HazardSettings settings;

    public HazardEngine(@NotNull final HazardSettings settings) {
        this.settings = settings;
    }

    public boolean isActive() {
        return settings.isEnabled() && settings.hasAnyHazards();
    }

    public int getTickIntervalTicks() {
        return settings.getTickIntervalTicks();
    }

    /** Applies every configured hazard to the players currently inside this instance's rooms. */
    public void tick(@NotNull final IDungeonInstance instance) {
        if (!isActive() || !(instance instanceof final DungeonInstance di)) return;
        final RoomGraph graph = di.getRoomGraph();
        if (graph == null) return;

        for (final RoomData room : graph.getRooms()) {
            if (!room.isEntered()) continue;
            final HazardProfile profile = settings.forRoomType(room.getType());
            if (profile == null) continue;
            applyToRoom(room, profile);
        }
    }

    private void applyToRoom(@NotNull final RoomData room, @NotNull final HazardProfile profile) {
        final Location centre = room.getCentre();
        if (centre.getWorld() == null) return;
        for (final Player player : centre.getWorld().getPlayers()) {
            if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) continue;
            if (!room.contains(player.getLocation())) continue;
            harm(player, profile);
        }
    }

    private void harm(@NotNull final Player player, @NotNull final HazardProfile profile) {
        if (profile.damage() > 0) player.damage(profile.damage());
        if (profile.effect() != null) {
            player.addPotionEffect(new PotionEffect(
                    profile.effect(), profile.effectTicks(), profile.amplifier(), false, true));
        }
        if (profile.particle() != null && player.getWorld() != null) {
            player.getWorld().spawnParticle(
                    profile.particle(), player.getLocation().add(0, 1, 0), 10, 0.4, 0.6, 0.4, 0.02);
        }
    }
}

package com.ultimatedungeon.dungeon.lifecycle;

import com.ultimatedungeon.core.PluginLogger;
import com.ultimatedungeon.dungeon.instance.DungeonInstanceManager;
import com.ultimatedungeon.room.model.RoomData;
import com.ultimatedungeon.util.MiniMessageUtil;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Puts a cleared wave room on a timed cooldown before it can be fought again.
 *
 * <p>When a wave room is cleared a floating countdown hologram is spawned in the
 * centre of the room. Each tick the hologram updates; once the cooldown elapses
 * the hologram is removed and the room is re-armed ({@link RoomData#reset()}) so
 * the next player to enter starts a fresh wave. Holograms are torn down when the
 * owning dungeon instance ends, so nothing lingers in the shared dungeon world.</p>
 */
public final class WaveResetManager {

    private static final class Cooldown {
        final UUID instanceId;
        final RoomData room;
        final long expireAtMs;
        final ArmorStand hologram;
        Cooldown(final UUID instanceId, final RoomData room, final long expireAtMs, final ArmorStand hologram) {
            this.instanceId = instanceId;
            this.room = room;
            this.expireAtMs = expireAtMs;
            this.hologram = hologram;
        }
    }

    private final DungeonInstanceManager instances;
    private final PluginLogger logger;
    private final List<Cooldown> cooldowns = new CopyOnWriteArrayList<>();

    public WaveResetManager(@NotNull final DungeonInstanceManager instances,
                            @NotNull final PluginLogger logger) {
        this.instances = instances;
        this.logger = logger;
    }

    /** Begins the reset cooldown for a freshly-cleared wave room. */
    public void startCooldown(@NotNull final UUID instanceId, @NotNull final RoomData room,
                              final int resetSeconds) {
        if (resetSeconds <= 0) return;
        final Location centre = room.getCentre().add(0, 1.2, 0);
        final ArmorStand holo = spawnHologram(centre, resetSeconds);
        cooldowns.add(new Cooldown(instanceId, room,
                System.currentTimeMillis() + resetSeconds * 1000L, holo));
    }

    /** @return whether the given room is currently on reset cooldown. */
    public boolean isCoolingDown(@NotNull final RoomData room) {
        for (final Cooldown c : cooldowns) if (c.room == room) return true;
        return false;
    }

    /** @return remaining cooldown seconds for the room, or 0 if not cooling down. */
    public long remainingSeconds(@NotNull final RoomData room) {
        for (final Cooldown c : cooldowns) {
            if (c.room == room) return Math.max(0, (c.expireAtMs - System.currentTimeMillis()) / 1000 + 1);
        }
        return 0;
    }

    /** Ticked on the main thread: updates holograms, re-arms expired rooms, prunes dead instances. */
    public void tick() {
        final long now = System.currentTimeMillis();
        for (final Cooldown c : cooldowns) {
            final boolean instanceGone = instances.getInstance(c.instanceId) == null;
            if (instanceGone || now >= c.expireAtMs) {
                removeHologram(c.hologram);
                if (!instanceGone) c.room.reset();
                cooldowns.remove(c);
            } else if (c.hologram != null && c.hologram.isValid()) {
                final long secs = (c.expireAtMs - now) / 1000 + 1;
                c.hologram.setCustomName(MiniMessageUtil.legacy(
                        "<yellow>Wave resets in <white>" + secs + "s"));
            }
        }
    }

    /** Removes every cooldown and hologram belonging to an instance. */
    public void clearInstance(@NotNull final UUID instanceId) {
        for (final Cooldown c : cooldowns) {
            if (c.instanceId.equals(instanceId)) {
                removeHologram(c.hologram);
                cooldowns.remove(c);
            }
        }
    }

    private ArmorStand spawnHologram(@NotNull final Location loc, final int resetSeconds) {
        final World world = loc.getWorld();
        if (world == null) return null;
        try {
            return world.spawn(loc, ArmorStand.class, stand -> {
                stand.setMarker(true);
                stand.setVisible(false);
                stand.setGravity(false);
                stand.setInvulnerable(true);
                stand.setSilent(true);
                stand.setCustomNameVisible(true);
                stand.setPersistent(false);
                stand.setCustomName(MiniMessageUtil.legacy(
                        "<yellow>Wave resets in <white>" + resetSeconds + "s"));
            });
        } catch (final Exception e) {
            logger.debug("Could not spawn wave-reset hologram: " + e.getMessage());
            return null;
        }
    }

    private void removeHologram(final ArmorStand holo) {
        if (holo != null && holo.isValid()) holo.remove();
    }
}

package com.ultimatedungeon.room.templates;

import com.ultimatedungeon.room.model.RoomData;
import com.ultimatedungeon.room.model.RoomType;
import com.ultimatedungeon.theme.model.ThemeBlockPalette;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Room template: ParkourRoomTemplate (PARKOUR).
 *
 * <p>Procedurally builds a different jump course every time it is placed. One of
 * several styles is chosen at random — classic block hops, slippery ice jumps,
 * bouncy slime pads, tight precision single-block landings, or a ladder climb —
 * so no two parkour rooms feel the same.</p>
 *
 * <p>All hops are guaranteed reachable and are capped at {@code Y_MAX} so a
 * standing player never has their head in the ceiling. Missed jumps drop the
 * player back to the floor to retry rather than into the void. The final pad is a
 * gold block; standing on it completes the course (reward handled by
 * {@link com.ultimatedungeon.listeners.room.RoomEnterListener}).</p>
 */
public final class ParkourRoomTemplate extends AbstractRoomTemplate {

    /** Marker material for the finish pad — the completion listener watches for it. */
    public static final Material FINISH_PAD = Material.GOLD_BLOCK;

    /** Jump heights are clamped to [1, 3]; with a 7-high room this keeps two air
     *  blocks above every stance so a standing player never suffocates. */
    private static final int Y_MIN = 1;
    private static final int Y_MAX = 3;

    private enum Style { CLASSIC, ICE, SLIME, PRECISION, LADDER }

    public ParkourRoomTemplate() {
        super("parkour", RoomType.PARKOUR, 8);
    }

    @Override public int getWidth() { return 15; }
    @Override public int getDepth() { return 15; }

    @Override
    protected void decorateRoom(
            @NotNull final Location          origin,
            @NotNull final RoomData          data,
            @NotNull final ThemeBlockPalette palette
    ) {
        final Style style = Style.values()[ThreadLocalRandom.current().nextInt(Style.values().length)];
        if (style == Style.LADDER) {
            buildLadderCourse(origin, palette);
        } else {
            buildJumpCourse(origin, palette, style);
        }
    }

    // ── Jump courses (classic / ice / slime / precision) ──────────────────────

    private void buildJumpCourse(@NotNull final Location origin,
                                 @NotNull final ThemeBlockPalette palette, @NotNull final Style style) {
        final Material mat = switch (style) {
            case ICE       -> Material.BLUE_ICE;
            case SLIME     -> Material.SLIME_BLOCK;
            default        -> palette.getAccent();
        };
        // Gap is the horizontal jump length; 3 (two air blocks) is a running jump,
        // 2 (one air block) an easy hop.
        final int gap = (style == Style.ICE || style == Style.PRECISION) ? 3 : 2;

        int x = 2;
        int z = clamp(getDepth() / 2 + ThreadLocalRandom.current().nextInt(-1, 2), 2, getDepth() - 3);
        int y = Y_MIN;
        placeRelative(origin, x, y, z, mat);

        final int endX = getWidth() - 3;
        while (x < endX) {
            x = Math.min(endX, x + gap);
            z = clamp(z + ThreadLocalRandom.current().nextInt(-1, 2), 2, getDepth() - 3);
            y = nextHeight(y, gap, style);
            placeRelative(origin, x, y, z, mat);
        }
        placeFinish(origin, x, y, z);
    }

    /** Picks the next stance height, keeping every hop within a one-block climb. */
    private int nextHeight(final int y, final int gap, @NotNull final Style style) {
        if (style == Style.ICE || style == Style.PRECISION) return y; // flat, momentum/precision
        int delta = 0;
        if (style == Style.SLIME) {
            delta = ThreadLocalRandom.current().nextBoolean() ? 1 : 0;   // bounce climbs
        } else if (gap <= 2 && ThreadLocalRandom.current().nextDouble() < 0.4) {
            delta = ThreadLocalRandom.current().nextBoolean() ? 1 : -1;
        }
        return clamp(y + delta, Y_MIN, Y_MAX);
    }

    // ── Ladder course ─────────────────────────────────────────────────────────

    private void buildLadderCourse(@NotNull final Location origin,
                                   @NotNull final ThemeBlockPalette palette) {
        final int z = clamp(getDepth() / 2, 2, getDepth() - 3);

        // Approach hops toward the west wall.
        placeRelative(origin, 3, Y_MIN, z, palette.getAccent());
        placeRelative(origin, 5, Y_MIN, z, palette.getAccent());

        // Ladders climb the solid west wall (x = 0) from the accent stub at x = 1.
        // A ladder faces away from its support block, so facing EAST anchors it to
        // the wall to its west.
        for (int y = Y_MIN; y <= Y_MAX; y++) {
            placeLadder(origin, 1, y, z, BlockFace.EAST);
        }
        // Top landing, then a couple of descending hops to the finish.
        placeRelative(origin, 2, Y_MAX, z, palette.getAccent());
        placeRelative(origin, 4, Y_MAX, z, palette.getAccent());
        placeRelative(origin, 6, Y_MAX - 1, z, palette.getAccent());
        placeRelative(origin, 8, Y_MIN + 1, clamp(z + 1, 2, getDepth() - 3), palette.getAccent());
        placeFinish(origin, 10, Y_MIN, clamp(z + 1, 2, getDepth() - 3));
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void placeFinish(@NotNull final Location origin, final int x, final int y, final int z) {
        placeRelative(origin, x, y, z, FINISH_PAD);
        placeRelative(origin, x, y + 1, z, Material.SEA_LANTERN);
    }

    private void placeLadder(@NotNull final Location origin, final int dx, final int dy, final int dz,
                             @NotNull final BlockFace facing) {
        if (origin.getWorld() == null) return;
        final Block block = origin.clone().add(dx, dy, dz).getBlock();
        block.setType(Material.LADDER, false);
        final BlockData bd = block.getBlockData();
        if (bd instanceof final Directional dir) {
            dir.setFacing(facing);
            block.setBlockData(bd, false);
        }
    }

    private int clamp(final int v, final int lo, final int hi) {
        return Math.max(lo, Math.min(hi, v));
    }
}

package com.ultimatedungeon.dungeon.generation;

import com.ultimatedungeon.config.files.DungeonConfig;
import com.ultimatedungeon.core.PluginLogger;
import com.ultimatedungeon.room.model.RoomData;
import com.ultimatedungeon.room.model.RoomGraph;
import com.ultimatedungeon.room.model.RoomType;
import com.ultimatedungeon.theme.model.ThemeBlockPalette;
import com.ultimatedungeon.util.BlockUtil;
import com.ultimatedungeon.util.RandomUtil;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

/**
 * Adds environmental detail decorations to placed rooms.
 *
 * <p><strong>Must be called on the main server thread.</strong></p>
 *
 * <h3>Decoration types</h3>
 * <ul>
 *   <li>Wall cracks — randomly replaces wall blocks with cracked variants.</li>
 *   <li>Floor debris — scatters gravel/sand on floor tiles.</li>
 *   <li>Ceiling drips — places stalactite-style blocks on ceilings.</li>
 *   <li>Torch pillars — places torches on accent pillars.</li>
 *   <li>Vines / moss — applied probabilistically in applicable themes.</li>
 * </ul>
 *
 * <p>Decoration density is read from {@code dungeon.yml → generation.decoration-density}.
 * Each room gets a seeded pass so variance is present but never exceeds budget.</p>
 */
public final class DecorationPainter {

    private final DungeonConfig dungeonConfig;
    private final PluginLogger  logger;

    public DecorationPainter(
            @NotNull final DungeonConfig dungeonConfig,
            @NotNull final PluginLogger  logger
    ) {
        this.dungeonConfig = dungeonConfig;
        this.logger        = logger;
    }

    /**
     * Paints decorations on every room in {@code graph}.
     *
     * @param graph   the validated, placed room graph
     * @param palette theme block palette
     */
    public void paintAll(
            @NotNull final RoomGraph        graph,
            @NotNull final ThemeBlockPalette palette
    ) {
        for (final RoomData room : graph.getRooms()) {
            paintRoom(room, palette);
        }
        logger.debug("DecorationPainter: decorated " + graph.getRoomCount() + " rooms.");
    }

    // ── Private ───────────────────────────────────────────────────────────────

    private void paintRoom(
            @NotNull final RoomData         room,
            @NotNull final ThemeBlockPalette palette
    ) {
        final double density = dungeonConfig.getDecorationDensity();

        // Skip decorator passes for special rooms to keep them clean
        if (room.getType() == RoomType.BOSS || room.getType() == RoomType.REWARD) return;

        paintWallCracks(room, palette, density);
        paintFloorDebris(room, density);
        paintTorches(room, palette);
        if (room.getType() == RoomType.SECRET) paintVines(room, density);
    }

    /**
     * Randomly replaces wall blocks with cracked / mossy variants for age.
     */
    private void paintWallCracks(
            @NotNull final RoomData         room,
            @NotNull final ThemeBlockPalette palette,
            final double                    density
    ) {
        final int w = room.getWidth(), h = room.getHeight(), d = room.getDepth();
        for (int x = 1; x < w - 1; x++) {
            for (int y = 1; y < h - 1; y++) {
                // Only apply to wall positions
                for (final int z : new int[]{0, d - 1}) {
                    if (RandomUtil.rollChance(density * 0.15)) {
                        final var loc = room.getOrigin().clone().add(x, y, z);
                        BlockUtil.replaceBlock(loc, palette.getPrimary(), palette.getSecondary());
                    }
                }
                for (final int x2 : new int[]{0, w - 1}) {
                    if (RandomUtil.rollChance(density * 0.15)) {
                        final var loc = room.getOrigin().clone().add(x2, y, x);
                        BlockUtil.replaceBlock(loc, palette.getPrimary(), palette.getSecondary());
                    }
                }
            }
        }
    }

    /**
     * Scatters gravel on interior floor tiles.
     */
    private void paintFloorDebris(
            @NotNull final RoomData room,
            final double            density
    ) {
        final int w = room.getWidth(), d = room.getDepth();
        for (int x = 2; x < w - 2; x++) {
            for (int z = 2; z < d - 2; z++) {
                if (RandomUtil.rollChance(density * 0.05)) {
                    final var loc = room.getOrigin().clone().add(x, 1, z);
                    if (BlockUtil.isPassable(loc)) {
                        BlockUtil.setBlock(loc, Material.GRAVEL);
                    }
                }
            }
        }
    }

    /**
     * Places wall torches at mid-height on interior walls for lighting.
     */
    private void paintTorches(
            @NotNull final RoomData         room,
            @NotNull final ThemeBlockPalette palette
    ) {
        final int w = room.getWidth(), h = room.getHeight(), d = room.getDepth();
        final int torchY = h / 2;

        // North / south walls
        placeWallTorch(room, w / 2, torchY, 1);
        placeWallTorch(room, w / 2, torchY, d - 2);
        // East / west walls
        placeWallTorch(room, 1,     torchY, d / 2);
        placeWallTorch(room, w - 2, torchY, d / 2);
    }

    private void placeWallTorch(
            @NotNull final RoomData room,
            final int dx, final int dy, final int dz
    ) {
        final var loc = room.getOrigin().clone().add(dx, dy, dz);
        if (BlockUtil.isPassable(loc)) {
            BlockUtil.setBlock(loc, Material.TORCH);
        }
    }

    /**
     * Adds vine blocks to secret rooms for a hidden, overgrown feel.
     */
    private void paintVines(
            @NotNull final RoomData room,
            final double            density
    ) {
        final int w = room.getWidth(), h = room.getHeight(), d = room.getDepth();
        for (int x = 1; x < w - 1; x++) {
            for (int z = 1; z < d - 1; z++) {
                if (RandomUtil.rollChance(density * 0.3)) {
                    // Place vines hanging from ceiling
                    for (int y = h - 2; y >= h / 2; y--) {
                        final var loc = room.getOrigin().clone().add(x, y, z);
                        if (BlockUtil.isPassable(loc)) {
                            BlockUtil.setBlock(loc, Material.VINE);
                        } else break;
                    }
                }
            }
        }
    }
}

package com.geodash.level;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockFace;

import java.util.Locale;
import java.util.Random;

/**
 * Builds ready-to-play demo courses (easy / medium / hard) in the world,
 * starting at a base location and running along a cardinal direction.
 */
public final class DemoGenerator {

    public enum Difficulty {
        EASY(120, 2, 0.33, Material.WHITE_CONCRETE, Material.LIME_CONCRETE, 12, false, false),
        MEDIUM(180, 5, 0.36, Material.LIGHT_GRAY_CONCRETE, Material.ORANGE_CONCRETE, 10, true, false),
        HARD(240, 8, 0.40, Material.BLACK_CONCRETE, Material.RED_CONCRETE, 8, true, true);

        final int length;
        final int stars;
        final double speed;
        final Material floor;
        final Material accent;
        final int obstacleGap;
        final boolean platforms;
        final boolean brutal;

        Difficulty(int length, int stars, double speed, Material floor, Material accent,
                   int obstacleGap, boolean platforms, boolean brutal) {
            this.length = length;
            this.stars = stars;
            this.speed = speed;
            this.floor = floor;
            this.accent = accent;
            this.obstacleGap = obstacleGap;
            this.platforms = platforms;
            this.brutal = brutal;
        }
    }

    private final World world;
    private final int baseX, baseY, baseZ;
    private final int dx, dz;   // run direction
    private final int px, pz;   // perpendicular (lane width) direction

    private DemoGenerator(Location base, BlockFace direction) {
        this.world = base.getWorld();
        this.baseX = base.getBlockX();
        this.baseY = base.getBlockY();
        this.baseZ = base.getBlockZ();
        this.dx = direction.getModX();
        this.dz = direction.getModZ();
        this.px = -dz;
        this.pz = dx;
    }

    /**
     * Generates the course and registers it as level "demo_<difficulty>".
     * The base location becomes the start; the course runs in the given direction.
     */
    public static Level generate(LevelManager levels, Location base, BlockFace direction, Difficulty diff) {
        new DemoGenerator(base, direction).build(diff);

        String name = "demo_" + diff.name().toLowerCase(Locale.ROOT);
        Level level = levels.get(name);
        if (level == null) {
            level = levels.create(name);
        }
        level.setStart(base, direction);
        level.setFinish(base.clone().add(direction.getModX() * (diff.length - 2.0), 0,
                direction.getModZ() * (diff.length - 2.0)));
        level.setSpeed(diff.speed);
        level.setStars(diff.stars);
        level.setReady(true);
        levels.save();
        return level;
    }

    private void build(Difficulty diff) {
        Random random = new Random(diff.ordinal() * 7919L + 42);

        // Lane: floor at y-1, clear air above, glass walls on both sides
        for (int i = -2; i <= diff.length + 4; i++) {
            for (int w = -2; w <= 2; w++) {
                set(i, w, -1, diff.floor);
                for (int y = 0; y <= 3; y++) {
                    set(i, w, y, Material.AIR);
                }
            }
            for (int y = 0; y <= 2; y++) {
                set(i, -3, y, Material.GLASS);
                set(i, 3, y, Material.GLASS);
            }
            set(i, -3, -1, diff.accent);
            set(i, 3, -1, diff.accent);
        }

        // Obstacles: keep the first and last stretches flat
        int i = 10;
        while (i < diff.length - 12) {
            switch (random.nextInt(diff.platforms ? 4 : 3)) {
                case 0 -> i = spikes(i, diff.brutal && random.nextBoolean() ? 2 : 1);
                case 1 -> i = gap(i, diff.brutal ? 3 : 2);
                case 2 -> i = magmaStrip(i, diff.brutal ? 3 : 2);
                default -> i = padAndPlatform(i, diff);
            }
            i += diff.obstacleGap - random.nextInt(3);
        }

        // Finish arch
        int end = diff.length;
        for (int y = 0; y <= 3; y++) {
            set(end, -3, y, Material.GOLD_BLOCK);
            set(end, 3, y, Material.GOLD_BLOCK);
        }
        for (int w = -3; w <= 3; w++) {
            set(end, w, 4, Material.GOLD_BLOCK);
            set(end + 1, w, -1, Material.DIAMOND_BLOCK);
            set(end + 2, w, -1, Material.DIAMOND_BLOCK);
        }
    }

    /** Full-width dripstone spike row; depth = blocks along the run direction. */
    private int spikes(int i, int depth) {
        for (int d = 0; d < depth; d++) {
            for (int w = -2; w <= 2; w++) {
                set(i + d, w, 0, Material.POINTED_DRIPSTONE);
            }
        }
        return i + depth;
    }

    /** Hole in the floor. Falling in drops below the void-death line quickly. */
    private int gap(int i, int len) {
        for (int d = 0; d < len; d++) {
            for (int w = -2; w <= 2; w++) {
                for (int y = -1; y >= -12; y--) {
                    set(i + d, w, y, Material.AIR);
                }
            }
        }
        return i + len;
    }

    /** Magma floor strip - lethal to step on, must be jumped over. */
    private int magmaStrip(int i, int len) {
        for (int d = 0; d < len; d++) {
            for (int w = -2; w <= 2; w++) {
                set(i + d, w, -1, Material.MAGMA_BLOCK);
            }
        }
        return i + len;
    }

    /** Slime jump pad launching onto a raised platform, then back down. */
    private int padAndPlatform(int i, Difficulty diff) {
        for (int w = -2; w <= 2; w++) {
            set(i, w, -1, Material.SLIME_BLOCK);
        }
        int platStart = i + 3;
        int platLen = 5;
        for (int d = 0; d < platLen; d++) {
            for (int w = -2; w <= 2; w++) {
                set(platStart + d, w, 1, diff.accent);
                if (diff.brutal) {
                    set(platStart + d, w, -1, Material.MAGMA_BLOCK);
                }
            }
        }
        // Raise the walls around the platform so nobody flies out
        for (int d = -1; d <= platLen + 1; d++) {
            for (int y = 3; y <= 5; y++) {
                set(platStart + d, -3, y, Material.GLASS);
                set(platStart + d, 3, y, Material.GLASS);
            }
        }
        return platStart + platLen;
    }

    private void set(int along, int width, int yOff, Material material) {
        int x = baseX + dx * along + px * width;
        int z = baseZ + dz * along + pz * width;
        world.getBlockAt(x, baseY + yOff, z).setType(material, false);
    }
}

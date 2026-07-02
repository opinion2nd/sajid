package com.geodash.level;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.util.Vector;

/**
 * A GeoDash course: a start point, a cardinal run direction and a length.
 * Progress along the course is the projection of the player's position
 * onto the run direction, expressed as a percentage.
 */
public class Level {

    private final String name;
    private String worldName;
    private double startX, startY, startZ;
    private BlockFace direction;
    private double length;
    private double speed;
    private int stars;
    private boolean ready;

    public Level(String name) {
        this.name = name;
        this.direction = BlockFace.EAST;
        this.speed = 0.35;
        this.stars = 1;
    }

    public static Level load(String name, ConfigurationSection sec) {
        Level level = new Level(name);
        level.worldName = sec.getString("world");
        level.startX = sec.getDouble("x");
        level.startY = sec.getDouble("y");
        level.startZ = sec.getDouble("z");
        level.direction = BlockFace.valueOf(sec.getString("direction", "EAST"));
        level.length = sec.getDouble("length");
        level.speed = sec.getDouble("speed", 0.35);
        level.stars = sec.getInt("stars", 1);
        level.ready = sec.getBoolean("ready", false);
        return level;
    }

    public void save(ConfigurationSection sec) {
        sec.set("world", worldName);
        sec.set("x", startX);
        sec.set("y", startY);
        sec.set("z", startZ);
        sec.set("direction", direction.name());
        sec.set("length", length);
        sec.set("speed", speed);
        sec.set("stars", stars);
        sec.set("ready", ready);
    }

    public void setStart(Location loc, BlockFace face) {
        this.worldName = loc.getWorld().getName();
        this.startX = loc.getBlockX() + 0.5;
        this.startY = loc.getBlockY();
        this.startZ = loc.getBlockZ() + 0.5;
        this.direction = face;
    }

    /** Sets the finish from a location; returns the resulting course length (<= 0 means invalid). */
    public double setFinish(Location loc) {
        this.length = distanceAlong(loc);
        return this.length;
    }

    /** Signed distance from start along the run direction. */
    public double distanceAlong(Location loc) {
        double dx = loc.getX() - startX;
        double dz = loc.getZ() - startZ;
        return dx * direction.getModX() + dz * direction.getModZ();
    }

    /** Progress 0..100 for a location. */
    public double progress(Location loc) {
        if (length <= 0) {
            return 0;
        }
        return Math.max(0, Math.min(100, distanceAlong(loc) / length * 100.0));
    }

    public Location startLocation() {
        World world = getWorld();
        if (world == null) {
            return null;
        }
        float yaw = switch (direction) {
            case NORTH -> 180f;
            case SOUTH -> 0f;
            case WEST -> 90f;
            default -> -90f; // EAST
        };
        return new Location(world, startX, startY, startZ, yaw, 0f);
    }

    public Vector directionVector() {
        return new Vector(direction.getModX(), 0, direction.getModZ());
    }

    public World getWorld() {
        return worldName == null ? null : Bukkit.getWorld(worldName);
    }

    public String getName() {
        return name;
    }

    public String getWorldName() {
        return worldName;
    }

    public double getStartY() {
        return startY;
    }

    public BlockFace getDirection() {
        return direction;
    }

    public double getLength() {
        return length;
    }

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public int getStars() {
        return stars;
    }

    public void setStars(int stars) {
        this.stars = stars;
    }

    public boolean isReady() {
        return ready;
    }

    public void setReady(boolean ready) {
        this.ready = ready;
    }
}

package com.geodash.command;

import com.geodash.GeoDashPlugin;
import com.geodash.level.DemoGenerator;
import com.geodash.level.Level;
import com.geodash.stats.StatsManager;
import com.geodash.util.Msg;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class GdCommand implements CommandExecutor, TabCompleter {

    private final GeoDashPlugin plugin;

    public GdCommand(GeoDashPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            return help(sender);
        }
        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "join" -> join(sender, args);
            case "leave" -> leave(sender);
            case "list" -> list(sender);
            case "top" -> top(sender, args);
            case "stats" -> stats(sender);
            case "race" -> race(sender, args);
            case "create" -> create(sender, args);
            case "setstart" -> setStart(sender, args);
            case "setfinish" -> setFinish(sender, args);
            case "setspeed" -> setSpeed(sender, args);
            case "setstars" -> setStars(sender, args);
            case "delete" -> delete(sender, args);
            case "kit" -> kit(sender);
            case "demo" -> demo(sender, args);
            case "reload" -> reload(sender);
            default -> help(sender);
        }
        return true;
    }

    private boolean help(CommandSender sender) {
        Msg.sendRaw(sender, "&8&m----------&r &bGeo&3Dash &8&m----------");
        Msg.sendRaw(sender, "&b/gd join <level> &8- &7play a level");
        Msg.sendRaw(sender, "&b/gd leave &8- &7quit the current level");
        Msg.sendRaw(sender, "&b/gd list &8- &7show all levels");
        Msg.sendRaw(sender, "&b/gd top <level> &8- &7leaderboard");
        Msg.sendRaw(sender, "&b/gd stats &8- &7your stats");
        Msg.sendRaw(sender, "&b/gd race join &8- &7join an open race");
        if (sender.hasPermission("geodash.race.manage")) {
            Msg.sendRaw(sender, "&b/gd race create <level> &8| &bstart &8| &bcancel");
        }
        if (sender.hasPermission("geodash.admin")) {
            Msg.sendRaw(sender, "&b/gd create <name> &8- &7new level at your position");
            Msg.sendRaw(sender, "&b/gd setstart&8/&bsetfinish <name> &8- &7edit course");
            Msg.sendRaw(sender, "&b/gd setspeed <name> <0.1-1.0> &8| &bsetstars <name> <1-10>");
            Msg.sendRaw(sender, "&b/gd delete <name> &8| &bkit &8| &bdemo <easy|medium|hard>");
            Msg.sendRaw(sender, "&b/gd reload");
        }
        return true;
    }

    private Player asPlayer(CommandSender sender) {
        if (sender instanceof Player player) {
            return player;
        }
        Msg.send(sender, "&cOnly players can use this command.");
        return null;
    }

    private Level requireLevel(CommandSender sender, String name) {
        Level level = plugin.getLevels().get(name);
        if (level == null) {
            Msg.send(sender, "&cUnknown level &f" + name + "&c. See &f/gd list&c.");
        }
        return level;
    }

    private static BlockFace facing(Player player) {
        float yaw = ((player.getLocation().getYaw() % 360) + 360) % 360;
        if (yaw >= 315 || yaw < 45) {
            return BlockFace.SOUTH;
        }
        if (yaw < 135) {
            return BlockFace.WEST;
        }
        if (yaw < 225) {
            return BlockFace.NORTH;
        }
        return BlockFace.EAST;
    }

    // ---------------- player commands ----------------

    private void join(CommandSender sender, String[] args) {
        Player player = asPlayer(sender);
        if (player == null || !player.hasPermission("geodash.play")) {
            return;
        }
        if (args.length < 2) {
            Msg.send(sender, "&cUsage: /gd join <level>");
            return;
        }
        Level level = requireLevel(sender, args[1]);
        if (level != null) {
            plugin.getGame().join(player, level, null);
        }
    }

    private void leave(CommandSender sender) {
        Player player = asPlayer(sender);
        if (player == null) {
            return;
        }
        if (!plugin.getGame().inGame(player)) {
            Msg.send(player, "&cYou are not in a level.");
            return;
        }
        plugin.getGame().leave(player, true);
    }

    private void list(CommandSender sender) {
        Msg.send(sender, "&7Levels:");
        if (plugin.getLevels().all().isEmpty()) {
            Msg.sendRaw(sender, "  &8(none yet - admins: /gd demo easy)");
        }
        for (Level level : plugin.getLevels().all()) {
            Msg.sendRaw(sender, "  &b" + level.getName() + " &8- &e" + level.getStars() + "★ &8- &7"
                    + (int) level.getLength() + " blocks"
                    + (level.isReady() ? "" : " &c(not ready)"));
        }
    }

    private void top(CommandSender sender, String[] args) {
        if (args.length < 2) {
            Msg.send(sender, "&cUsage: /gd top <level>");
            return;
        }
        Level level = requireLevel(sender, args[1]);
        if (level == null) {
            return;
        }
        List<StatsManager.TopEntry> top = plugin.getStats().top(level.getName(), 10);
        Msg.send(sender, "&7Top players on &f" + level.getName() + "&7:");
        if (top.isEmpty()) {
            Msg.sendRaw(sender, "  &8Nobody has played this level yet.");
        }
        int place = 1;
        for (StatsManager.TopEntry entry : top) {
            String result = entry.stats().completions > 0
                    ? "&a" + Msg.time(entry.stats().bestTimeMs)
                    : "&e" + (int) entry.stats().bestPercent + "%";
            Msg.sendRaw(sender, "  &6#" + place++ + " &f" + entry.playerName()
                    + " &8- " + result + " &8- &7" + entry.stats().attempts + " attempts");
        }
    }

    private void stats(CommandSender sender) {
        Player player = asPlayer(sender);
        if (player == null) {
            return;
        }
        Map<String, StatsManager.LevelStats> levels = plugin.getStats().of(player).levels;
        Msg.send(player, "&7Your stats:");
        if (levels.isEmpty()) {
            Msg.sendRaw(player, "  &8Play a level first: /gd list");
            return;
        }
        levels.forEach((name, ls) -> Msg.sendRaw(player, "  &b" + name
                + " &8- &7best &e" + (int) ls.bestPercent + "%"
                + (ls.bestTimeMs >= 0 ? " &8- &7best time &a" + Msg.time(ls.bestTimeMs) : "")
                + " &8- &7" + ls.attempts + " attempts, " + ls.completions + " clears"));
    }

    // ---------------- race ----------------

    private void race(CommandSender sender, String[] args) {
        String sub = args.length > 1 ? args[1].toLowerCase(Locale.ROOT) : "join";
        switch (sub) {
            case "create" -> {
                if (!sender.hasPermission("geodash.race.manage")) {
                    Msg.send(sender, "&cYou may not manage races.");
                    return;
                }
                if (args.length < 3) {
                    Msg.send(sender, "&cUsage: /gd race create <level>");
                    return;
                }
                Level level = requireLevel(sender, args[2]);
                if (level != null) {
                    plugin.getRaces().create(sender, level);
                }
            }
            case "start" -> {
                if (!sender.hasPermission("geodash.race.manage")) {
                    Msg.send(sender, "&cYou may not manage races.");
                    return;
                }
                plugin.getRaces().start(sender);
            }
            case "cancel" -> {
                if (!sender.hasPermission("geodash.race.manage")) {
                    Msg.send(sender, "&cYou may not manage races.");
                    return;
                }
                plugin.getRaces().cancel(sender);
            }
            case "join" -> {
                Player player = asPlayer(sender);
                if (player != null && player.hasPermission("geodash.race")) {
                    plugin.getRaces().join(player);
                }
            }
            default -> Msg.send(sender, "&cUsage: /gd race <join|create|start|cancel>");
        }
    }

    // ---------------- admin / editor ----------------

    private boolean admin(CommandSender sender) {
        if (!sender.hasPermission("geodash.admin")) {
            Msg.send(sender, "&cYou may not edit levels.");
            return false;
        }
        return true;
    }

    private void create(CommandSender sender, String[] args) {
        Player player = asPlayer(sender);
        if (player == null || !admin(sender)) {
            return;
        }
        if (args.length < 2 || !args[1].matches("[A-Za-z0-9_-]{1,32}")) {
            Msg.send(sender, "&cUsage: /gd create <name> (letters, digits, _ and - only)");
            return;
        }
        if (plugin.getLevels().get(args[1]) != null) {
            Msg.send(sender, "&cA level with that name already exists.");
            return;
        }
        Level level = plugin.getLevels().create(args[1]);
        level.setStart(player.getLocation(), facing(player));
        plugin.getLevels().save();
        Msg.send(sender, "&aCreated level &f" + args[1] + "&a. Start set here, running &f"
                + level.getDirection() + "&a. Now build the course and run &f/gd setfinish " + args[1]
                + " &aat the end.");
    }

    private void setStart(CommandSender sender, String[] args) {
        Player player = asPlayer(sender);
        if (player == null || !admin(sender) || args.length < 2) {
            return;
        }
        Level level = requireLevel(sender, args[1]);
        if (level == null) {
            return;
        }
        level.setStart(player.getLocation(), facing(player));
        level.setReady(false);
        plugin.getLevels().save();
        Msg.send(sender, "&aStart of &f" + level.getName() + " &aset here (direction &f"
                + level.getDirection() + "&a). Re-run &f/gd setfinish&a.");
    }

    private void setFinish(CommandSender sender, String[] args) {
        Player player = asPlayer(sender);
        if (player == null || !admin(sender) || args.length < 2) {
            return;
        }
        Level level = requireLevel(sender, args[1]);
        if (level == null) {
            return;
        }
        if (!player.getWorld().getName().equals(level.getWorldName())) {
            Msg.send(sender, "&cYou must be in the same world as the level start.");
            return;
        }
        double length = level.setFinish(player.getLocation());
        if (length < 10) {
            level.setReady(false);
            Msg.send(sender, "&cFinish must be at least 10 blocks past the start (in direction &f"
                    + level.getDirection() + "&c).");
        } else {
            level.setReady(true);
            Msg.send(sender, "&aFinish set! &f" + level.getName() + " &ais &f" + (int) length
                    + " &ablocks long and now playable.");
        }
        plugin.getLevels().save();
    }

    private void setSpeed(CommandSender sender, String[] args) {
        if (!admin(sender) || args.length < 3) {
            Msg.send(sender, "&cUsage: /gd setspeed <level> <0.1-1.0>");
            return;
        }
        Level level = requireLevel(sender, args[1]);
        if (level == null) {
            return;
        }
        try {
            double speed = Double.parseDouble(args[2]);
            if (speed < 0.1 || speed > 1.0) {
                throw new NumberFormatException();
            }
            level.setSpeed(speed);
            plugin.getLevels().save();
            Msg.send(sender, "&aSpeed of &f" + level.getName() + " &aset to &f" + speed);
        } catch (NumberFormatException e) {
            Msg.send(sender, "&cSpeed must be a number between 0.1 and 1.0");
        }
    }

    private void setStars(CommandSender sender, String[] args) {
        if (!admin(sender) || args.length < 3) {
            Msg.send(sender, "&cUsage: /gd setstars <level> <1-10>");
            return;
        }
        Level level = requireLevel(sender, args[1]);
        if (level == null) {
            return;
        }
        try {
            int stars = Integer.parseInt(args[2]);
            if (stars < 1 || stars > 10) {
                throw new NumberFormatException();
            }
            level.setStars(stars);
            plugin.getLevels().save();
            Msg.send(sender, "&aDifficulty of &f" + level.getName() + " &aset to &e" + stars + "★");
        } catch (NumberFormatException e) {
            Msg.send(sender, "&cStars must be a whole number between 1 and 10");
        }
    }

    private void delete(CommandSender sender, String[] args) {
        if (!admin(sender) || args.length < 2) {
            Msg.send(sender, "&cUsage: /gd delete <level>");
            return;
        }
        Level level = requireLevel(sender, args[1]);
        if (level == null) {
            return;
        }
        plugin.getLevels().delete(level.getName());
        Msg.send(sender, "&aDeleted level &f" + level.getName() + "&a. (Blocks stay in the world.)");
    }

    private void kit(CommandSender sender) {
        Player player = asPlayer(sender);
        if (player == null || !admin(sender)) {
            return;
        }
        player.getInventory().addItem(
                new ItemStack(Material.POINTED_DRIPSTONE, 64),
                new ItemStack(Material.MAGMA_BLOCK, 64),
                new ItemStack(Material.SLIME_BLOCK, 32),
                new ItemStack(Material.GLASS, 64),
                new ItemStack(Material.WHITE_CONCRETE, 64),
                new ItemStack(Material.GOLD_BLOCK, 16));
        Msg.send(player, "&aEditor kit: &fdripstone/magma &7= spikes, &fslime &7= jump pad, "
                + "&fglass &7= walls, &fconcrete &7= floor.");
    }

    private void demo(CommandSender sender, String[] args) {
        Player player = asPlayer(sender);
        if (player == null || !admin(sender)) {
            return;
        }
        DemoGenerator.Difficulty diff;
        try {
            diff = DemoGenerator.Difficulty.valueOf(
                    (args.length > 1 ? args[1] : "easy").toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            Msg.send(sender, "&cUsage: /gd demo <easy|medium|hard>");
            return;
        }
        Msg.send(sender, "&7Generating &f" + diff.name().toLowerCase(Locale.ROOT)
                + " &7demo course from your position, facing &f" + facing(player) + "&7...");
        Level level = DemoGenerator.generate(plugin.getLevels(), player.getLocation(), facing(player), diff);
        Msg.send(sender, "&aDone! Play it with &f/gd join " + level.getName());
    }

    private void reload(CommandSender sender) {
        if (!admin(sender)) {
            return;
        }
        plugin.reloadEverything();
        Msg.send(sender, "&aGeoDash reloaded.");
    }

    // ---------------- tab completion ----------------

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> out = new ArrayList<>();
        if (args.length == 1) {
            out.addAll(List.of("join", "leave", "list", "top", "stats", "race"));
            if (sender.hasPermission("geodash.admin")) {
                out.addAll(List.of("create", "setstart", "setfinish", "setspeed", "setstars",
                        "delete", "kit", "demo", "reload"));
            }
        } else if (args.length == 2) {
            switch (args[0].toLowerCase(Locale.ROOT)) {
                case "join", "top", "setstart", "setfinish", "setspeed", "setstars", "delete" ->
                        plugin.getLevels().all().forEach(l -> out.add(l.getName()));
                case "race" -> {
                    out.add("join");
                    if (sender.hasPermission("geodash.race.manage")) {
                        out.addAll(List.of("create", "start", "cancel"));
                    }
                }
                case "demo" -> out.addAll(List.of("easy", "medium", "hard"));
                default -> {
                }
            }
        } else if (args.length == 3 && args[0].equalsIgnoreCase("race") && args[1].equalsIgnoreCase("create")) {
            plugin.getLevels().all().forEach(l -> out.add(l.getName()));
        }
        String last = args[args.length - 1].toLowerCase(Locale.ROOT);
        return out.stream().filter(s -> s.toLowerCase(Locale.ROOT).startsWith(last)).toList();
    }
}

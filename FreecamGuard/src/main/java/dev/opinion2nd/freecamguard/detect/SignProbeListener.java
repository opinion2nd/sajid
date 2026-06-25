package dev.opinion2nd.freecamguard.detect;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.nbt.NBT;
import com.github.retrooper.packetevents.protocol.nbt.NBTByte;
import com.github.retrooper.packetevents.protocol.nbt.NBTCompound;
import com.github.retrooper.packetevents.protocol.nbt.NBTList;
import com.github.retrooper.packetevents.protocol.nbt.NBTString;
import com.github.retrooper.packetevents.protocol.nbt.NBTType;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.world.blockentity.BlockEntityTypes;
import com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import com.github.retrooper.packetevents.protocol.world.states.type.StateTypes;
import com.github.retrooper.packetevents.util.Vector3i;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientUpdateSign;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerBlockChange;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerBlockEntityData;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerOpenSignEditor;
import dev.opinion2nd.freecamguard.FreecamGuardPlugin;
import dev.opinion2nd.freecamguard.SchedulerUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Detects client-side mods with a "sign probe".
 *
 * <p>On join the player is shown a fake, server-only sign editor whose lines are
 * <i>translation keys</i> (for example {@code key.freecam.toggle}). A vanilla
 * client cannot translate an unknown key, so it sends the raw key straight back
 * when the editor closes. A client that ships the matching mod replaces the key
 * with its localized label — and that difference is the tell. The fake sign is
 * never placed in the world; the original block is restored immediately.
 */
public final class SignProbeListener implements Listener {

    /** {displayName, translationKey} for every mod we know how to probe. */
    private static final String[][] KNOWN_MODS = {
            {"Freecam", "key.freecam.toggle"},
            {"Meteor Client", "key.meteor-client.open-gui"},
            {"Wurst", "key.wurst.zoom"},
    };

    private final FreecamGuardPlugin plugin;
    private final boolean legacyJsonSignText;
    private final String[][] activeMods;

    private final Map<UUID, Vector3i> probePosition = new ConcurrentHashMap<>();
    private final Map<UUID, Integer> originalBlockId = new ConcurrentHashMap<>();

    private PacketListenerAbstract packetListener;

    public SignProbeListener(FreecamGuardPlugin plugin) {
        this.plugin = plugin;
        this.legacyJsonSignText = usesLegacyJsonSignText();
        this.activeMods = buildActiveMods(plugin);
    }

    /** Names of the mods currently being probed (respecting the config). */
    public static List<String> activeModNames(FreecamGuardPlugin plugin) {
        List<String> names = new ArrayList<>();
        for (String[] mod : buildActiveMods(plugin)) {
            names.add(mod[0]);
        }
        return names;
    }

    private static String[][] buildActiveMods(FreecamGuardPlugin plugin) {
        List<String[]> active = new ArrayList<>();
        for (String[] mod : KNOWN_MODS) {
            if (plugin.getConfig().getBoolean("modDetection.detect." + mod[0], true)) {
                active.add(mod);
            }
            if (active.size() == 4) {
                break; // a sign only has four lines
            }
        }
        return active.toArray(new String[0][]);
    }

    public void register() {
        if (activeMods.length == 0) {
            plugin.getLogger().info("[SignProbe] No mods enabled in config — sign probe is idle.");
            return;
        }
        warmUpRegistries();
        packetListener = new PacketListenerAbstract(PacketListenerPriority.HIGHEST) {
            @Override
            public void onPacketReceive(PacketReceiveEvent event) {
                if (event.getPacketType() == PacketType.Play.Client.UPDATE_SIGN) {
                    handleSignResponse(event);
                }
            }
        };
        PacketEvents.getAPI().getEventManager().registerListener(packetListener);
    }

    public void shutdown() {
        if (packetListener != null) {
            PacketEvents.getAPI().getEventManager().unregisterListener(packetListener);
            packetListener = null;
        }
        // Restore any sign still showing to an online player before we drop state.
        for (UUID uuid : new ArrayList<>(probePosition.keySet())) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline()) {
                restoreBlock(player, uuid);
            }
        }
        probePosition.clear();
        originalBlockId.clear();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event) {
        if (activeMods.length == 0) {
            return;
        }
        if (!plugin.getConfig().getBoolean("modDetection.probeOnJoin", true)) {
            return;
        }
        Player player = event.getPlayer();
        if (player.hasPermission("freecamguard.bypass")) {
            return;
        }
        // Skip Bedrock players (Geyser/Floodgate) — they have a v0 UUID and no
        // Java translation keys, so the probe is meaningless for them.
        if (player.getUniqueId().version() == 0) {
            return;
        }
        long delay = plugin.getConfig().getLong("modDetection.probeDelayTicks", 40L);
        SchedulerUtil.runEntityLater(plugin, player, () -> {
            if (player.isOnline()) {
                sendProbe(player);
            }
        }, delay);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        probePosition.remove(uuid);
        originalBlockId.remove(uuid);
    }

    private void sendProbe(Player player) {
        if (!player.isOnline()) {
            return;
        }
        UUID uuid = player.getUniqueId();
        Location loc = player.getLocation();
        int y = Math.max(loc.getWorld().getMinHeight(), loc.getBlockY() - 2);
        Vector3i pos = new Vector3i(loc.getBlockX(), y, loc.getBlockZ());

        Block block = loc.getWorld().getBlockAt(pos.getX(), pos.getY(), pos.getZ());
        originalBlockId.put(uuid, globalIdOf(block));
        probePosition.put(uuid, pos);

        int signId = WrappedBlockState.getDefaultState(StateTypes.OAK_SIGN).getGlobalId();
        PacketEvents.getAPI().getPlayerManager()
                .sendPacket(player, new WrapperPlayServerBlockChange(pos, signId));
        PacketEvents.getAPI().getPlayerManager()
                .sendPacket(player, new WrapperPlayServerBlockEntityData(pos, BlockEntityTypes.SIGN, buildSignNbt()));
        plugin.getLogger().info("[SignProbe] Probing " + player.getName() + " at " + pos.getX()
                + "," + pos.getY() + "," + pos.getZ() + " — waiting for them to close the sign.");

        // How long (in ticks) to keep waiting for the client to submit the sign
        // before giving up. Generous by default so a player reading the sign is
        // still caught.
        long timeout = plugin.getConfig().getLong("modDetection.probeTimeoutTicks", 300L);

        // Open the editor a tick later, then arm a timeout that cleans up if the
        // client never answers.
        SchedulerUtil.runEntityLater(plugin, player, () -> {
            if (!player.isOnline() || !probePosition.containsKey(uuid)) {
                return;
            }
            PacketEvents.getAPI().getPlayerManager()
                    .sendPacket(player, new WrapperPlayServerOpenSignEditor(pos, true));
            SchedulerUtil.runEntityLater(plugin, player, () -> {
                if (probePosition.containsKey(uuid)) {
                    restoreBlock(player, uuid);
                    probePosition.remove(uuid);
                    originalBlockId.remove(uuid);
                    boolean strict = plugin.getConfig().getBoolean("modDetection.kickOnNoResponse", true);
                    if (strict && player.isOnline()) {
                        plugin.getLogger().warning("[SignProbe] " + player.getName()
                                + " never closed the probe sign — kicking (strict mode).");
                        String raw = plugin.getConfig().getString("modDetection.noResponseKickMessage",
                                "&cYou must close the verification sign to play here.");
                        kickPlayer(player, buildKickComponent(raw, "Unknown", player.getName()));
                    } else {
                        plugin.getLogger().info("[SignProbe] " + player.getName()
                                + " never closed the sign within the timeout — no detection.");
                    }
                }
            }, timeout);
        }, 1L);
    }

    private void handleSignResponse(PacketReceiveEvent event) {
        Player player = (Player) event.getPlayer();
        if (player == null) {
            return;
        }
        UUID uuid = player.getUniqueId();
        Vector3i expected = probePosition.get(uuid);
        if (expected == null) {
            return;
        }
        WrapperPlayClientUpdateSign wrapper = new WrapperPlayClientUpdateSign(event);
        Vector3i got = wrapper.getBlockPosition();
        if (expected.getX() != got.getX() || expected.getY() != got.getY() || expected.getZ() != got.getZ()) {
            return;
        }
        // This was our probe — swallow it so it never reaches the server logic.
        event.setCancelled(true);
        restoreBlock(player, uuid);
        probePosition.remove(uuid);
        originalBlockId.remove(uuid);

        String[] lines = wrapper.getTextLines();
        plugin.getLogger().info("[SignProbe] Got sign back from " + player.getName()
                + ": " + java.util.Arrays.toString(lines));
        String detected = null;
        int count = Math.min(activeMods.length, 4);
        for (int i = 0; i < count; i++) {
            String line = (lines != null && i < lines.length && lines[i] != null) ? lines[i] : "";
            // Untouched key == vanilla / mod absent. Anything else == translated.
            if (!line.isEmpty() && !line.equals(activeMods[i][1])) {
                detected = activeMods[i][0];
                break;
            }
        }
        if (detected != null) {
            onDetected(player, detected);
        }
    }

    private void onDetected(Player player, String mod) {
        plugin.getLogger().warning("[SignProbe] DETECTED " + mod + " on " + player.getName()
                + " — autoKick=" + plugin.getConfig().getBoolean("modDetection.autoKick", true));
        if (plugin.getConfig().getBoolean("modDetection.notifyAdmins", true)) {
            String alert = "§c[FreecamGuard] §e" + player.getName() + " §7was detected using §c" + mod;
            SchedulerUtil.runGlobal(plugin, () -> {
                for (Player online : Bukkit.getOnlinePlayers()) {
                    if (online.hasPermission("freecamguard.notify")) {
                        online.sendMessage(alert);
                    }
                }
                Bukkit.getConsoleSender().sendMessage(alert);
            });
        }
        if (plugin.getConfig().getBoolean("modDetection.autoKick", true)) {
            String raw = plugin.getConfig().getString("modDetection.kickMessage",
                    "&cYou are using a disallowed mod ({mod})!");
            kickPlayer(player, buildKickComponent(raw, mod, player.getName()));
        }
    }

    /** Build a kick-screen component: replace placeholders, \n, and '&' codes. */
    private Component buildKickComponent(String raw, String mod, String playerName) {
        String text = raw.replace("{mod}", mod)
                .replace("{player}", playerName)
                .replace("\\n", "\n");
        return LegacyComponentSerializer.legacyAmpersand().deserialize(text);
    }

    /** Kick on the player's region/main thread, as soon as possible. */
    private void kickPlayer(Player player, Component message) {
        SchedulerUtil.runEntityLater(plugin, player, () -> {
            if (player.isOnline()) {
                player.kick(message);
            }
        }, 1L);
    }

    private void restoreBlock(Player player, UUID uuid) {
        Vector3i pos = probePosition.get(uuid);
        Integer id = originalBlockId.get(uuid);
        if (pos == null || id == null || !player.isOnline()) {
            return;
        }
        PacketEvents.getAPI().getPlayerManager()
                .sendPacket(player, new WrapperPlayServerBlockChange(pos, id));
    }

    private NBTCompound buildSignNbt() {
        NBTCompound root = new NBTCompound();
        root.setTag("front_text", buildTextSide(true));
        root.setTag("back_text", buildTextSide(false));
        root.setTag("is_waxed", new NBTByte((byte) 0));
        return root;
    }

    /** Build one face of the sign. The front carries the probe keys. */
    private NBTCompound buildTextSide(boolean front) {
        NBTCompound side = new NBTCompound();
        int count = front ? Math.min(activeMods.length, 4) : 0;
        if (legacyJsonSignText) {
            NBTList<NBTString> messages = new NBTList<>(NBTType.STRING);
            for (int i = 0; i < count; i++) {
                messages.addTag(new NBTString("{\"translate\":\"" + activeMods[i][1] + "\"}"));
            }
            for (int i = count; i < 4; i++) {
                messages.addTag(new NBTString("{\"text\":\"\"}"));
            }
            side.setTag("messages", messages);
        } else {
            NBTList<NBTCompound> messages = new NBTList<>(NBTType.COMPOUND);
            for (int i = 0; i < count; i++) {
                NBTCompound line = new NBTCompound();
                line.setTag("translate", new NBTString(activeMods[i][1]));
                messages.addTag(line);
            }
            for (int i = count; i < 4; i++) {
                NBTCompound line = new NBTCompound();
                line.setTag("text", new NBTString(""));
                messages.addTag(line);
            }
            side.setTag("messages", messages);
        }
        side.setTag("has_glowing_text", new NBTByte((byte) 0));
        side.setTag("color", new NBTString("black"));
        return side;
    }

    private int globalIdOf(Block block) {
        try {
            return WrappedBlockState.getByString(block.getBlockData().getAsString()).getGlobalId();
        } catch (Exception e) {
            return 0;
        }
    }

    private void warmUpRegistries() {
        try {
            WrappedBlockState.getDefaultState(StateTypes.OAK_SIGN).getGlobalId();
            WrappedBlockState.getByString("minecraft:stone").getGlobalId();
        } catch (Throwable t) {
            plugin.getLogger().warning("[SignProbe] registry warm-up failed (non-fatal): " + t);
        }
    }

    /**
     * Sign text NBT was serialized as JSON strings up to 1.21.4 and as plain
     * compounds from 1.21.5 onwards. Older releases (&lt; 1.21) also use the JSON
     * form.
     */
    private static boolean usesLegacyJsonSignText() {
        try {
            String[] parts = Bukkit.getMinecraftVersion().split("\\.");
            int major = Integer.parseInt(parts[0]);
            int minor = parts.length > 1 ? Integer.parseInt(parts[1]) : 0;
            int patch = parts.length > 2 ? Integer.parseInt(parts[2]) : 0;
            if (major != 1) {
                return false;
            }
            if (minor != 21) {
                return minor < 21;
            }
            return patch < 5;
        } catch (Exception e) {
            return false;
        }
    }
}

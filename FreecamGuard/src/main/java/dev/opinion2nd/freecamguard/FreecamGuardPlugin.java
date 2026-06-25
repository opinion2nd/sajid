package dev.opinion2nd.freecamguard;

import com.github.retrooper.packetevents.PacketEvents;
import dev.opinion2nd.freecamguard.command.FreecamGuardCommand;
import dev.opinion2nd.freecamguard.detect.BrandChannelListener;
import dev.opinion2nd.freecamguard.detect.SignProbeListener;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * FreecamGuard — original anti-freecam / cheat-client plugin.
 *
 * <p>It does exactly two things and nothing else:
 * <ul>
 *     <li>Detects the Freecam mod and the Meteor / Wurst cheat clients with a
 *     sign-probe trick and kicks the player.</li>
 *     <li>Watches the client brand and registered plugin channels to catch the
 *     same mods even when the sign probe is dodged.</li>
 * </ul>
 * There is no X-ray feature and no minimap detection.
 */
public final class FreecamGuardPlugin extends JavaPlugin {

    private SignProbeListener signProbe;
    private BrandChannelListener brandChannel;

    @Override
    public void onLoad() {
        PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this));
        PacketEvents.getAPI().getSettings().checkForUpdates(false).bStats(false);
        PacketEvents.getAPI().load();
    }

    @Override
    public void onEnable() {
        PacketEvents.getAPI().init();
        saveDefaultConfig();

        FreecamGuardCommand command = new FreecamGuardCommand(this);
        PluginCommand pluginCommand = getCommand("freecamguard");
        if (pluginCommand != null) {
            pluginCommand.setExecutor(command);
            pluginCommand.setTabCompleter(command);
        }

        start();
        getLogger().info("FreecamGuard enabled (Folia=" + SchedulerUtil.isFolia()
                + "). Detecting: " + String.join(", ", SignProbeListener.activeModNames(this)) + ".");
    }

    @Override
    public void onDisable() {
        stop();
        PacketEvents.getAPI().terminate();
    }

    /** (Re)create and register the detection listeners from the current config. */
    public void start() {
        stop();
        signProbe = new SignProbeListener(this);
        brandChannel = new BrandChannelListener(this);
        Bukkit.getPluginManager().registerEvents(signProbe, this);
        Bukkit.getPluginManager().registerEvents(brandChannel, this);
        signProbe.register();
        brandChannel.register();
    }

    /** Tear down the detection listeners. */
    public void stop() {
        if (signProbe != null) {
            signProbe.shutdown();
            org.bukkit.event.HandlerList.unregisterAll(signProbe);
            signProbe = null;
        }
        if (brandChannel != null) {
            brandChannel.shutdown();
            org.bukkit.event.HandlerList.unregisterAll(brandChannel);
            brandChannel = null;
        }
    }

    public void reloadEverything() {
        reloadConfig();
        start();
    }
}

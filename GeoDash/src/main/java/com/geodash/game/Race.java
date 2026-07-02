package com.geodash.game;

import com.geodash.level.Level;
import com.geodash.util.Msg;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * A multiplayer race: everyone runs the same level at once,
 * finish order decides the podium.
 */
public class Race {

    public enum State {LOBBY, COUNTDOWN, RUNNING, DONE}

    private final Level level;
    private final Set<UUID> players = new LinkedHashSet<>();
    private final List<String> finishOrder = new ArrayList<>();
    private State state = State.LOBBY;

    public Race(Level level) {
        this.level = level;
    }

    public Level getLevel() {
        return level;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public boolean isRunning() {
        return state == State.RUNNING;
    }

    public Set<UUID> getPlayers() {
        return players;
    }

    public void addPlayer(Player player) {
        players.add(player.getUniqueId());
    }

    /** Returns the finishing place (1 = winner). */
    public synchronized int playerFinished(Player player, long timeMs) {
        if (!players.remove(player.getUniqueId())) {
            return 0;
        }
        finishOrder.add(player.getName());
        int place = finishOrder.size();
        broadcastToWorld("&6#" + place + " &b" + player.getName()
                + " &7finished &f" + level.getName() + " &7in &a" + Msg.time(timeMs) + "&7!");
        if (players.isEmpty()) {
            state = State.DONE;
        }
        return place;
    }

    public synchronized void playerLeft(Player player) {
        players.remove(player.getUniqueId());
        if (state == State.RUNNING && players.isEmpty()) {
            state = State.DONE;
        }
    }

    public List<String> getFinishOrder() {
        return finishOrder;
    }

    private void broadcastToWorld(String message) {
        org.bukkit.Bukkit.broadcastMessage(Msg.color("&8[&bRace&8] &7" + Msg.color(message)));
    }
}

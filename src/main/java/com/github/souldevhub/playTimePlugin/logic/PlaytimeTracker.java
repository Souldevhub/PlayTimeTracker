package com.github.souldevhub.playTimePlugin.logic;


import com.github.souldevhub.playTimePlugin.PlayTimePlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlaytimeTracker {
    
    private final PlayTimePlugin plugin;
    private final DataHandler dataHandler;

    private final Map<UUID, Long> sessionTimes = new HashMap<>();

    public PlaytimeTracker(PlayTimePlugin plugin, DataHandler dataHandler) {
        this.plugin = plugin;
        this.dataHandler = dataHandler;
    }

    public void startTrackingTask() {
        new BukkitRunnable() {
            int flushCounter = 0;
            @Override
            public void run() {

                for (Player player : Bukkit.getOnlinePlayers()) {
                    UUID uuid = player.getUniqueId();
                    long previous = sessionTimes.getOrDefault(uuid, 0L);
                    long newTime = previous + 60;
                    sessionTimes.put(uuid, newTime);
                }
                flushCounter++;
                // Every 5 minutes (5 * 60 sec / 60 sec interval = 5 runs), flush sessions to disk
                if (flushCounter >= 5) {
                    flushAllSessionTimes();
                    flushCounter = 0;
                }
            }
        }.runTaskTimer(plugin, 20 * 60, 20 * 60);
    }

    public void onPlayerJoin(UUID uuid) {
        sessionTimes.putIfAbsent(uuid, 0L);
    }

    public void onPlayerQuit(UUID uuid) {
        Long sessionTime = sessionTimes.get(uuid);
        if (sessionTime != null && sessionTime > 0) {
            dataHandler.addPlaytime(uuid, sessionTime);
            dataHandler.savePlaytime(uuid);
            sessionTimes.remove(uuid);
        }
    }

    public long getTotalPlaytime(UUID uuid) {
        long stored = dataHandler.getPlaytime(uuid);
        long session = sessionTimes.getOrDefault(uuid, 0L);
        return stored + session;
    }

    public void flushAllSessionTimes() {
        for (Map.Entry<UUID, Long> entry : sessionTimes.entrySet()) {
            UUID uuid = entry.getKey();
            long sessionTime = entry.getValue();
            if (sessionTime > 0) {
                dataHandler.addPlaytime(uuid, sessionTime);
                dataHandler.savePlaytime(uuid);
                sessionTimes.put(uuid, 0L);
            }
        }
    }

}

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
    private final long intervalTicks = 20*60;

    public PlaytimeTracker(PlayTimePlugin plugin, DataHandler dataHandler) {
        this.plugin = plugin;
        this.dataHandler = dataHandler;
    }

    public void startTrackingTask() {
        new BukkitRunnable() {
            @Override
            public void run() {

                for (Player player : Bukkit.getOnlinePlayers()) {
                    UUID uuid = player.getUniqueId();
                    long previous = sessionTimes.getOrDefault(uuid, 0L);
                    long newTime = previous + 60_000;
                    sessionTimes.put(uuid, newTime);

                    dataHandler.addPlaytime(uuid, 60_000);
                }
            }
        }.runTaskTimer(plugin, intervalTicks, intervalTicks);
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

    public long getTotalPlaytimem(UUID uuid) {
        long stored = dataHandler.getPlaytime(uuid);
        long session = sessionTimes.getOrDefault(uuid, 0L);
        return stored + session;
    }
}

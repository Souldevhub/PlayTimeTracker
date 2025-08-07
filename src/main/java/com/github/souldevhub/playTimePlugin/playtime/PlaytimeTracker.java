package com.github.souldevhub.playTimePlugin.playtime;

import com.github.souldevhub.playTimePlugin.PlaytimePulse;
import com.github.souldevhub.playTimePlugin.data.DataHandler;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PlaytimeTracker {

    private final PlaytimePulse plugin;
    private final DataHandler dataHandler;
    
    // Interaction tracking for AFK protection
    private final Map<UUID, List<Long>> playerInteractions = new HashMap<>();

    private final Map<UUID, Long> sessionTimes = new HashMap<>();

    private static final long TRACK_INTERVAL_SECONDS = 1;
    private static final int FLUSH_INTERVAL_RUNS = 60;
    
    // AFK protection settings
    private static final int DEFAULT_INTERACTION_THRESHOLD = 2;
    private static final long DEFAULT_TIME_WINDOW_MINUTES = 5;
    private int interactionThreshold = DEFAULT_INTERACTION_THRESHOLD;
    private long timeWindowMillis = DEFAULT_TIME_WINDOW_MINUTES * 60 * 1000;

    public PlaytimeTracker(PlaytimePulse plugin, DataHandler dataHandler) {
        this.plugin = plugin;
        this.dataHandler = dataHandler;
    }

    public long getCurrentSessionPlaytime(UUID uuid) {
        return sessionTimes.getOrDefault(uuid, 0L);
    }

    public void startTrackingTask() {
        new BukkitRunnable() {
            int flushCounter = 0;

            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    UUID uuid = player.getUniqueId();
                    
                    // Check if the player is active (has enough recent interactions)
                    if (isActivePlayer(uuid)) {
                        long previous = sessionTimes.getOrDefault(uuid, 0L);
                        long newTime = previous + TRACK_INTERVAL_SECONDS;
                        sessionTimes.put(uuid, newTime);
                    }
                    
                    // Clean up old interaction timestamps
                    cleanupOldInteractions(uuid);
                }

                flushCounter++;

                if (flushCounter >= FLUSH_INTERVAL_RUNS) {
                    flushAllSessionTimes();
                    flushCounter = 0;
                }
            }
        }.runTaskTimer(plugin, 20L * TRACK_INTERVAL_SECONDS, 20L * TRACK_INTERVAL_SECONDS);
    }

    public void onPlayerJoin(UUID uuid) {
        sessionTimes.putIfAbsent(uuid, 0L);
        playerInteractions.putIfAbsent(uuid, new ArrayList<>());
    }

    public void onPlayerQuit(UUID uuid) {
        Long sessionTime = sessionTimes.get(uuid);
        if (sessionTime != null && sessionTime > 0) {
            dataHandler.addPlaytime(uuid, sessionTime);
            dataHandler.savePlaytime(uuid);
            sessionTimes.remove(uuid);
        }
        
        // Clear interaction history
        playerInteractions.remove(uuid);
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
    
    // AFK protection methods
    
    /**
     * Records a player interaction timestamp
     * @param uuid Player UUID
     */
    public void recordInteraction(UUID uuid) {
        playerInteractions.computeIfAbsent(uuid, k -> new ArrayList<>()).add(System.currentTimeMillis());
    }
    
    /**
     * Checks if a player is active based on recent interactions
     * @param uuid Player UUID
     * @return true if a player has enough recent interactions, false otherwise
     */
    private boolean isActivePlayer(UUID uuid) {
        List<Long> interactions = playerInteractions.get(uuid);
        if (interactions == null || interactions.isEmpty()) {
            return false;
        }
        
        long currentTime = System.currentTimeMillis();
        long windowStart = currentTime - timeWindowMillis;
        
        // Count interactions within the time window
        long recentInteractions = interactions.stream()
                .filter(timestamp -> timestamp >= windowStart)
                .count();
                
        return recentInteractions >= interactionThreshold;
    }
    
    /**
     * Cleans up old interaction timestamps to prevent memory leaks
     * @param uuid Player UUID
     */
    private void cleanupOldInteractions(UUID uuid) {
        List<Long> interactions = playerInteractions.get(uuid);
        if (interactions == null || interactions.isEmpty()) {
            return;
        }
        
        long currentTime = System.currentTimeMillis();
        long windowStart = currentTime - timeWindowMillis;
        
        // Remove old interactions
        interactions.removeIf(timestamp -> timestamp < windowStart);
    }
    
    /**
     * Configures AFK protection settings
     * @param interactionThreshold Minimum number of interactions required
     * @param timeWindowMinutes Time window in minutes to check for interactions
     */
    public void configureAFKProtection(int interactionThreshold, long timeWindowMinutes) {
        this.interactionThreshold = interactionThreshold > 0 ? interactionThreshold : DEFAULT_INTERACTION_THRESHOLD;
        this.timeWindowMillis = timeWindowMinutes > 0 ? timeWindowMinutes * 60 * 1000 : DEFAULT_TIME_WINDOW_MINUTES * 60 * 1000;
        plugin.getLogger().info("AFK Protection configured: " + interactionThreshold + " interactions in " + timeWindowMinutes + " minutes");
    }
}
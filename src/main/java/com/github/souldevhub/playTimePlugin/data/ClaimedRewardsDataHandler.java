package com.github.souldevhub.playTimePlugin.data;

import com.github.souldevhub.playTimePlugin.PlaytimePulse;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ClaimedRewardsDataHandler {
    
    private final PlaytimePulse plugin;
    private final File file;
    private final YamlConfiguration config;
    private final Map<UUID, List<String>> claimedCache = new HashMap<>();

    public ClaimedRewardsDataHandler(PlaytimePulse plugin) {
        this.plugin = plugin;
        
        // Initialize claimed rewards systems
        this.file = new File(plugin.getDataFolder(), "claimed_rewards.yml");
        if (!file.exists()) {
            try {
                if (!file.createNewFile()) {
                    plugin.getLogger().warning("Couldn't create claimed_rewards.yml file. Please check permissions.");
                }
            } catch (IOException e) {
                plugin.getLogger().severe("Error during file creation: " + e.getMessage());
            }
        }
        this.config = YamlConfiguration.loadConfiguration(file);
        loadAllClaimedRewards();
    }

    public List<String> getClaimedRewards(UUID uuid) {
        return claimedCache.getOrDefault(uuid, new ArrayList<>());
    }

    /**
     * Resets all claimed rewards for a player.
     * @param uuid Player UUID
     */
    public void resetClaimedRewards(UUID uuid) {
        claimedCache.remove(uuid);
        config.set(uuid.toString(), null);
        saveFile();
    }

    
    /**
     * Saves claimed rewards for a specific player and updated the cache
     * @param uuid Player UUID
     * @param claimed List of claimed rewards
     */
    public void saveClaimedRewards(UUID uuid, List<String> claimed) {
        claimedCache.put(uuid, claimed);
        config.set(uuid.toString(), claimed);
        saveFile();
    }

    private void loadAllClaimedRewards() {
        for (String key : config.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(key);
                List<String> claimed = config.getStringList(key);
                claimedCache.put(uuid, claimed);
            } catch (IllegalArgumentException ignored) {}
        }
    }

    private void saveFile() {
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save claimed_rewards.yml: " + e.getMessage());
        }
    }
}
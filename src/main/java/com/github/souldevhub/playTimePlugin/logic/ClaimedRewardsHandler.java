package com.github.souldevhub.playTimePlugin.logic;

import com.github.souldevhub.playTimePlugin.PlayTimePlugin;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class ClaimedRewardsHandler {
    private final PlayTimePlugin plugin;
    private final File file;
    private final YamlConfiguration config;
    private final Map<UUID, List<String>> claimedCache = new HashMap<>();

    public ClaimedRewardsHandler(PlayTimePlugin plugin) {
        this.plugin = plugin;
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
        loadAll();
    }


    public List<String> getClaimedRewards(UUID uuid) {
        return claimedCache.getOrDefault(uuid, new ArrayList<>());
    }

    public void addClaimedReward(UUID uuid, String rewardId) {
        List<String> claimed = claimedCache.computeIfAbsent(uuid, k -> new ArrayList<>());
        if (!claimed.contains(rewardId)) {
            claimed.add(rewardId);
            config.set(uuid.toString(), claimed);
            saveFile();
        }
    }

    /**
     * Resets all claimed rewards for the specified player
     * @param uuid Player's UUID
     */
    public void resetClaimedRewards(UUID uuid) {
        claimedCache.remove(uuid);
        config.set(uuid.toString(), null);
        saveFile();
    }

    private void loadAll() {
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


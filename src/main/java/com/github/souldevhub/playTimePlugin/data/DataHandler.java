package com.github.souldevhub.playTimePlugin.data;

import com.github.souldevhub.playTimePlugin.PlaytimePulse;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DataHandler {

    private final PlaytimePulse plugin;
    private final File file;
    private final FileConfiguration config;

    private final Map<UUID, Long> playtimeCache = new HashMap<>();

    public DataHandler(PlaytimePulse plugin){
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "playtime.yml");

        if (!file.exists()) {
            plugin.saveResource("playtime.yml", false);
        }

        this.config = YamlConfiguration.loadConfiguration(file);
        loadAll();
    }

    public void addPlaytime(UUID uuid, long sessionTime) {
        long current = playtimeCache.getOrDefault(uuid, 0L);
        playtimeCache.put(uuid, current + sessionTime);
    }

    public long getPlaytime(UUID uuid) {
        return playtimeCache.getOrDefault(uuid, 0L);
    }

    public void savePlaytime(UUID uuid) {
        long time = playtimeCache.getOrDefault(uuid, 0L);
        config.set(uuid.toString(), time);
        saveFile();
    }

    public int getPlaytimeSeconds(UUID uuid) {
        return (int) (getPlaytime(uuid) % 60);
    }

    public int getPlaytimeMinutes(UUID uuid) {
        return (int) ((getPlaytime(uuid) / 60) % 60);
    }

    public long getSavedPlaytime(UUID uuid) {
        return playtimeCache.getOrDefault(uuid, 0L);
    }

    public int getPlaytimeHours(UUID uuid) {
        return (int) ((getPlaytime(uuid) / 3600) % 24);
    }

    public int getPlaytimeDays(UUID uuid) {
        return (int) (getPlaytime(uuid) / 86400);
    }

    public void saveAll() {
        for (Map.Entry<UUID, Long> entry : playtimeCache.entrySet()) {
            config.set(entry.getKey().toString(),entry.getValue());
        }
        saveFile();
    }

    public Map<UUID, Long> getAllPlaytimes() {
        return new HashMap<>(playtimeCache);
    }

    private void loadAll() {
        for (String key : config.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(key);
                long time = config.getLong(key);
                playtimeCache.put(uuid, time);
            } catch (IllegalArgumentException ignored) {
                plugin.getLogger().warning("Invalid UUID in playtime.yml " + key);
            }
        }
    }

    private void saveFile() {
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save playtime.yml: " + e.getMessage());
        }
    }

}
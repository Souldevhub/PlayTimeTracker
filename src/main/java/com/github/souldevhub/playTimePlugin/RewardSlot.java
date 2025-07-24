package com.github.souldevhub.playTimePlugin;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.Sound;
import java.util.List;
import org.bukkit.plugin.java.JavaPlugin;

public record RewardSlot(
        String id,
        String name,
        Material material,
        String headId,
        int slot,
        List<String> lore,
        List<String> commands,
        long requiredPlaytime, // in seconds
        String claimSound
) {
    public RewardSlot {
        lore = lore != null ? List.copyOf(lore) : List.of();
        commands = commands != null ? List.copyOf(commands) : List.of();
    }

    /**
     * Default volume for all sounds
     * @return Fixed sound volume value
     */
    public float soundVolume() {
        return 1.0f;
    }

    /**
     * Default pitch for all sounds
     * @return Fixed sound pitch value
     */
    public float soundPitch() {
        return 1.0f;
    }

    public Sound getSound(JavaPlugin plugin) {
        if (claimSound == null || claimSound.isEmpty()) {
            return Registry.SOUNDS.get(NamespacedKey.minecraft("entity.player.levelup"));
        }

        try {
            String soundKey = claimSound.toLowerCase();

            if (!soundKey.contains(":")) {
                soundKey = "minecraft:" + soundKey;
            }

            NamespacedKey key = NamespacedKey.fromString(soundKey);
            Sound sound = null;
            if (key != null) {
                sound = Registry.SOUNDS.get(key);
            }

            if (sound != null) {
                return sound;
            }

            plugin.getLogger().warning("[PlayTimePulse] Could not find sound: " + claimSound);
        } catch (Exception e) {
            plugin.getLogger().warning("[PlayTimePulse] Error parsing sound: " + claimSound + ". Error: " + e.getMessage());
        }

        return Registry.SOUNDS.get(NamespacedKey.minecraft("entity.player.levelup"));
    }
}

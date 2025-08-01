package com.github.souldevhub.playTimePlugin.rewards;

import org.bukkit.Material;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class RewardSlot {
    private final String id;
    private final String name;
    private final Material material;
    private final String headId;
    private final int slot;
    private final List<String> lore;
    private final List<String> commands;
    private final long requiredPlaytime;
    private final String claimSound;
    private final float soundVolume;
    private final float soundPitch;
    private final int page;

    private RewardSlot(Builder builder) {
        this.id = builder.id;
        this.name = builder.name;
        this.material = builder.material;
        this.headId = builder.headId;
        this.slot = builder.slot;
        this.lore = builder.lore != null ? builder.lore : Collections.emptyList();
        this.commands = builder.commands != null ? builder.commands : Collections.emptyList();
        this.requiredPlaytime = builder.requiredPlaytime;
        this.claimSound = builder.claimSound;
        this.soundVolume = builder.soundVolume;
        this.soundPitch = builder.soundPitch;
        this.page = builder.page;
    }

    public String id() {
        return id;
    }

    public String name() {
        return name;
    }

    public Material material() {
        return material;
    }

    public String headId() {
        return headId;
    }

    public int slot() {
        return slot;
    }

    public List<String> lore() {
        return lore;
    }

    public List<String> commands() {
        return commands;
    }

    public long requiredPlaytime() {
        return requiredPlaytime;
    }

    public int page() {
        return page;
    }



    public float soundVolume() {
        return soundVolume;
    }

    public float soundPitch() {
        return soundPitch;
    }

    public static class Builder {
        private String id;
        private String name;
        private Material material;
        private String headId;
        private int slot;
        private List<String> lore;
        private List<String> commands;
        private long requiredPlaytime;
        private String claimSound;
        private float soundVolume = 1.0f;
        private float soundPitch = 1.0f;
        private int page;

        public Builder setId(String id) {
            this.id = id;
            return this;
        }

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder setMaterial(Material material) {
            this.material = material;
            return this;
        }

        public Builder setHeadId(String headId) {
            this.headId = headId;
            return this;
        }

        public Builder setSlot(int slot) {
            this.slot = slot;
            return this;
        }

        public Builder setLore(List<String> lore) {
            this.lore = lore;
            return this;
        }

        public Builder setCommands(List<String> commands) {
            this.commands = commands;
            return this;
        }

        public Builder setRequiredPlaytime(long requiredPlaytime) {
            this.requiredPlaytime = requiredPlaytime;
            return this;
        }

        public Builder setClaimSound(String claimSound) {
            this.claimSound = claimSound;
            return this;
        }

        public Builder setSoundVolume(float soundVolume) {
            this.soundVolume = soundVolume;
            return this;
        }

        public Builder setSoundPitch(float soundPitch) {
            this.soundPitch = soundPitch;
            return this;
        }

        public Builder setPage(int page) {
            this.page = page;
            return this;
        }

        public RewardSlot build() {
            if (id == null || id.isEmpty()) throw new IllegalStateException("id must not be null or empty");
            if (material == null) throw new IllegalStateException("material must not be null");
            return new RewardSlot(this);
        }
    }

    @SuppressWarnings("unchecked")
    public static RewardSlot fromMap(Map<?, ?> map) {
        if (map == null) return null;

        try {
            String id = map.get("id") != null ? map.get("id").toString() : null;
            String name = map.get("name") != null ? map.get("name").toString() : null;
            String matName = map.get("material") != null ? map.get("material").toString() : null;
            
            if (id == null || id.isEmpty() || matName == null || matName.isEmpty()) return null;

            Material material = Material.valueOf(matName.toUpperCase());
            
            String headId = map.get("headId") != null ? map.get("headId").toString() : null;
            int slot = map.get("slot") instanceof Number n ? n.intValue() : 0;
            List<String> lore = map.get("lore") instanceof List<?> l ? (List<String>) l : Collections.emptyList();
            List<String> commands = map.get("commands") instanceof List<?> l ? (List<String>) l : Collections.emptyList();
            long requiredPlaytime = map.get("requiredPlaytime") instanceof Number n ? n.longValue() : 0L;
            String claimSound = map.get("claimSound") != null ? map.get("claimSound").toString() : null;
            float soundVolume = map.get("soundVolume") instanceof Number n ? n.floatValue() : 1.0f;
            float soundPitch = map.get("soundPitch") instanceof Number n ? n.floatValue() : 1.0f;
            int page = map.get("page") instanceof Number n ? n.intValue() : 0;

            return new Builder()
                    .setId(id)
                    .setName(name)
                    .setMaterial(material)
                    .setHeadId(headId)
                    .setSlot(slot)
                    .setLore(lore)
                    .setCommands(commands)
                    .setRequiredPlaytime(requiredPlaytime)
                    .setClaimSound(claimSound)
                    .setSoundVolume(soundVolume)
                    .setSoundPitch(soundPitch)
                    .setPage(page)
                    .build();

        } catch (IllegalArgumentException | NullPointerException e) {
            return null;
        }
    }

    public org.bukkit.Sound getSound(org.bukkit.plugin.java.JavaPlugin plugin) {
        if (claimSound == null || claimSound.isEmpty()) {
            return org.bukkit.Sound.ENTITY_PLAYER_LEVELUP;
        }
        
        try {
            // Try to parse as NamespacedKey first (new format)
            org.bukkit.NamespacedKey key = org.bukkit.NamespacedKey.fromString(claimSound.toLowerCase());
            if (key != null) {
                org.bukkit.Sound sound = org.bukkit.Registry.SOUNDS.get(key);
                if (sound != null) {
                    return sound;
                }
            }
            // If that fails, try to match by iterating through registry
            // Using the recommended approach without deprecated methods
            for (org.bukkit.Sound sound : org.bukkit.Registry.SOUNDS) {
                // Compare using the key's string representation
                if (sound.toString().equalsIgnoreCase(claimSound)) {
                    return sound;
                }
            }
            // If still not found, log the warning and use default
            plugin.getLogger().warning("Invalid sound: " + claimSound + ", using default");
            return org.bukkit.Sound.ENTITY_PLAYER_LEVELUP;
        } catch (Exception e) {
            plugin.getLogger().warning("Error while parsing sound: " + claimSound + ", using default. Error: " + e.getMessage());
            return org.bukkit.Sound.ENTITY_PLAYER_LEVELUP;
        }
    }

}
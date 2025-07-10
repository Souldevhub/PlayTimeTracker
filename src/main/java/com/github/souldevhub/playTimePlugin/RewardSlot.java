// RewardSlot.java
package com.github.souldevhub.playTimePlugin;

import org.bukkit.Material;
import java.util.Collections;
import java.util.List;

public class RewardSlot {
    private final String id;
    private final String name;
    private final Material material;
    private final int slot;
    private final List<String> lore;
    private final List<String> commands;
    private final long requiredPlaytime; // in seconds

    public RewardSlot(String id, String name, Material material, int slot, List<String> lore, List<String> commands, long requiredPlaytime) {
        this.id = id;
        this.name = name;
        this.material = material;
        this.slot = slot;
        this.lore = lore != null ? lore : Collections.emptyList();
        this.commands = commands != null ? commands : Collections.emptyList();
        this.requiredPlaytime = requiredPlaytime;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public Material getMaterial() { return material; }
    public int getSlot() { return slot; }
    public List<String> getLore() { return lore; }
    public List<String> getCommands() { return commands; }
    public long getRequiredPlaytime() { return requiredPlaytime; }
}
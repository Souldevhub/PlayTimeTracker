// RewardSlot.java
package com.github.souldevhub.playTimePlugin;

import org.bukkit.Material;

import java.util.List;

public record RewardSlot(
    String id,
    String name,
    Material material,
    String headId,
    int slot,
    List<String> lore,
    List<String> commands,
    long requiredPlaytime // in seconds
) {
    public RewardSlot {
        lore = lore != null ? List.copyOf(lore) : List.of();
        commands = commands != null ? List.copyOf(commands) : List.of();
    }
}
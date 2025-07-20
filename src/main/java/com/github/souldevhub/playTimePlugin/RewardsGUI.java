package com.github.souldevhub.playTimePlugin;

import com.github.souldevhub.playTimePlugin.logic.ClaimedRewardsHandler;
import com.github.souldevhub.playTimePlugin.logic.PlaytimeTracker;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class RewardsGUI {
    private static JavaPlugin plugin;

    public static void init(JavaPlugin plugin) {
        RewardsGUI.plugin = plugin;
    }

    public static void open(Player player, PlaytimeTracker tracker, ClaimedRewardsHandler claimedHandler) {
        if (plugin == null) {
            throw new IllegalStateException("RewardsGUI not initialized! Call init() first.");
        }
        long playtime = tracker.getTotalPlaytime(player.getUniqueId());
        player.openInventory(PlayTimeConfig.getInstance(plugin).getRewardsGUIForPlayer(
                playtime,
                claimedHandler.getClaimedRewards(player.getUniqueId())
        ));
    }
}
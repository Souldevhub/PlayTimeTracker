package com.github.souldevhub.playTimePlugin;

import com.github.souldevhub.playTimePlugin.logic.ClaimedRewardsHandler;
import com.github.souldevhub.playTimePlugin.logic.PlaytimeTracker;
import org.bukkit.entity.Player;

public class RewardsGUI {
    public static void open(Player player, PlaytimeTracker tracker, ClaimedRewardsHandler claimedHandler) {
        long playtime = tracker.getTotalPlaytime(player.getUniqueId());
        player.openInventory(PlayTimeConfig.getInstance().getRewardsGUIForPlayer(
                playtime,
                claimedHandler.getClaimedRewards(player.getUniqueId())
        ));
    }
}

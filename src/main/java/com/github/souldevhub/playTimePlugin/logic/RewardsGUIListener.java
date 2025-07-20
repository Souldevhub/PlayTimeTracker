package com.github.souldevhub.playTimePlugin.logic;

import com.github.souldevhub.playTimePlugin.PlayTimeConfig;
import com.github.souldevhub.playTimePlugin.RewardSlot;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.plugin.java.JavaPlugin;

public class RewardsGUIListener implements Listener {

    private final PlaytimeTracker tracker;
    private final ClaimedRewardsHandler claimedHandler;
    private final JavaPlugin plugin;

    public RewardsGUIListener(PlaytimeTracker tracker, ClaimedRewardsHandler claimedHandler, JavaPlugin plugin) {
        this.tracker = tracker;
        this.claimedHandler = claimedHandler;
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        // Use Adventure Component for title comparison
        if (!event.getView().title().equals(Component.text("Rewards"))) return;

        event.setCancelled(true);

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        Player player = (Player) event.getWhoClicked();
        long playtime = tracker.getTotalPlaytime(player.getUniqueId());
        var claimedIds = claimedHandler.getClaimedRewards(player.getUniqueId());

        // Find the reward by slot (not by material or lore)
        int clickedSlot = event.getRawSlot();
        RewardSlot matchedReward = null;
        for (RewardSlot slot : PlayTimeConfig.getInstance(plugin).getRewardSlots()) {
            if (slot.slot() == clickedSlot) {
                matchedReward = slot;
                break;
            }
        }
        if (matchedReward == null) {
            player.sendMessage("§cYou cannot claim this reward.");
            return;
        }

        boolean claimed = claimedIds.contains(matchedReward.id());
        boolean enoughPlaytime = playtime >= matchedReward.requiredPlaytime();

        // Only allow claim if not claimed and enough playtime and the clicked item is the reward's original material
        if (!claimed && enoughPlaytime && clicked.getType() == matchedReward.material()) {
            for (String cmd : matchedReward.commands()) {
                String parsed = cmd.replace("%player%", player.getName());
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), parsed);
            }
            claimedHandler.addClaimedReward(player.getUniqueId(), matchedReward.id());
            Component rewardName = LegacyComponentSerializer.legacyAmpersand().deserialize(matchedReward.name());
            player.sendMessage(Component.text("You have successfully claimed the reward: ", NamedTextColor.GREEN).append(rewardName));
            player.closeInventory();
            return;
        }
        player.sendMessage("§cYou cannot claim this reward.");
    }
}
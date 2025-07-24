package com.github.souldevhub.playTimePlugin.logic;

import com.github.souldevhub.playTimePlugin.PlayTimeConfig;
import com.github.souldevhub.playTimePlugin.RewardSlot;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
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


        if (!claimed && enoughPlaytime && clicked.getType() == matchedReward.material()) {
            for (String cmd : matchedReward.commands()) {
                String parsed = cmd.replace("%player%", player.getName());
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), parsed);
            }
            claimedHandler.addClaimedReward(player.getUniqueId(), matchedReward.id());

            // Play custom reward sound if configured
            try {
                // Get the sound from the reward configuration
                org.bukkit.Sound bukkitSound = matchedReward.getSound(plugin);

                // Directly play the sound using Bukkit API
                player.playSound(player.getLocation(), bukkitSound, matchedReward.soundVolume(), matchedReward.soundPitch());

                plugin.getLogger().info("Playing sound for reward: " + matchedReward.id());
            } catch (Exception e) {
                // Fallback to default sound if any error occurs
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
                plugin.getLogger().warning("Error playing sound for reward: " + matchedReward.id() + ". " + e.getMessage());
            }




            Component rewardName = LegacyComponentSerializer.legacyAmpersand().deserialize(matchedReward.name());
            player.sendMessage(Component.text("You have successfully claimed the reward: ", NamedTextColor.GREEN).append(rewardName));
            player.closeInventory();
            return;
        }
        player.sendMessage("§cYou cannot claim this reward.");
        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.8f, 1.0f);
    }
}
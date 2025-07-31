package com.github.souldevhub.playTimePlugin.listeners;

import com.github.souldevhub.playTimePlugin.PlaytimePulse;
import com.github.souldevhub.playTimePlugin.rewards.RewardSlot;
import com.github.souldevhub.playTimePlugin.config.PlaytimeConfig;
import com.github.souldevhub.playTimePlugin.constants.GUIConstants;
import com.github.souldevhub.playTimePlugin.data.ClaimedRewardsDataHandler;
import com.github.souldevhub.playTimePlugin.rewards.gui.PageAwareHolder;
import com.github.souldevhub.playTimePlugin.rewards.gui.RewardsGUI;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class RewardsGUIListener implements Listener {

    private final PlaytimePulse plugin;
    private final NamespacedKey navButtonTypeKey;
    private final NamespacedKey rewardIdKey;
    private final ClaimedRewardsDataHandler claimedRewardsDataHandler;

    public RewardsGUIListener(JavaPlugin plugin) {
        this.plugin = (PlaytimePulse) plugin;
        this.claimedRewardsDataHandler = new ClaimedRewardsDataHandler(this.plugin);

        navButtonTypeKey = new NamespacedKey(plugin, "nav_button_type");
        rewardIdKey = new NamespacedKey(plugin, "reward_id");
    }

    @EventHandler(priority = org.bukkit.event.EventPriority.HIGHEST)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        // Check clicked inventory specifically (more precise)
        if (event.getClickedInventory() == null ||
                !(event.getClickedInventory().getHolder() instanceof RewardsGUI.RewardsGUIHolder)) {
            return;
        }

        event.setCancelled(true); // Cancel all interactions by default
        
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType().isAir()) {
            //plugin.getLogger().info("Clicked item is null or air, cancelling");
            return;
        }

        if (!clicked.hasItemMeta()) {
            //plugin.getLogger().info("Clicked item has no metadata, cancelling");
            // Cancel interaction with items that don't have metadata
            return;
        }

        ItemMeta meta = clicked.getItemMeta();
        PersistentDataContainer data = meta.getPersistentDataContainer();


        // Check if this is a navigation button
        if (data.has(navButtonTypeKey, PersistentDataType.STRING)) {
            //plugin.getLogger().info("Clicked navigation button");
            String buttonType = data.get(navButtonTypeKey, PersistentDataType.STRING);
            //plugin.getLogger().info("Navigation button type: " + buttonType);
            if (buttonType == null) {
                //plugin.getLogger().info("Navigation button type is null, returning");
                return;
            }

            switch (buttonType) {
                case GUIConstants.NAV_PREV -> 
                    RewardsGUI.openPreviousPage(plugin, player, plugin.getPlaytimeTracker(), this);
                case GUIConstants.NAV_NEXT -> 
                    RewardsGUI.openNextPage(plugin, player, plugin.getPlaytimeTracker(), this);
                case GUIConstants.NAV_CLOSE -> 
                    player.closeInventory();
            }
            return;
        }

        // Check if this is a reward item
        if (data.has(rewardIdKey, PersistentDataType.STRING)) {
            //plugin.getLogger().info("Clicked reward item");
            String rewardId = data.get(rewardIdKey, PersistentDataType.STRING);
            //plugin.getLogger().info("Reward ID: " + rewardId);
            if (rewardId == null || rewardId.trim().isEmpty()) {
                //plugin.getLogger().info("Reward ID is null or empty, returning");
                return;
            }

            UUID uuid = player.getUniqueId();
            List<String> claimed = new ArrayList<>(claimedRewardsDataHandler.getClaimedRewards(uuid)); // Create a copy to modify
            //plugin.getLogger().info("Player has claimed rewards: " + claimed.size());

            if (claimed.contains(rewardId)) {
                //plugin.getLogger().info("Reward already claimed");
                sendAlreadyClaimedMessage(player);
                refreshGUI(player);
                return;
            }

            java.util.Optional<RewardSlot> rewardOpt = PlaytimeConfig.getInstance(plugin).getRewardSlotById(rewardId);
            if (rewardOpt.isEmpty()) {
                //plugin.getLogger().info("Reward not found in config");
                player.sendMessage(net.kyori.adventure.text.Component.text("This reward no longer exists!").color(net.kyori.adventure.text.format.NamedTextColor.RED));
                return;
            }

            RewardSlot reward = rewardOpt.get();
            long playtime = plugin.getPlaytimeTracker().getTotalPlaytime(uuid);
            //plugin.getLogger().info("Player playtime: " + playtime + ", Required playtime: " + reward.requiredPlaytime());

            if (playtime < reward.requiredPlaytime()) {
                //plugin.getLogger().info("Player doesn't have enough playtime");
                player.sendMessage(net.kyori.adventure.text.Component.text("You don't have enough playtime to claim this reward!").color(net.kyori.adventure.text.format.NamedTextColor.RED));
                refreshGUI(player);
                return;
            }

            //plugin.getLogger().info("Claiming reward for player");
            // Prepare all commands with player name substitution
            List<String> processedCommands = new ArrayList<>();
            for (String cmd : reward.commands()) {
                processedCommands.add(cmd.replace("%player%", player.getName()));
            }
            
            // Execute all commands with strict transactional behavior
            boolean allCommandsSuccessful = true;
            for (String cmd : processedCommands) {
                //plugin.getLogger().info("Executing command: " + cmd);
                try {
                    boolean success = plugin.getServer().dispatchCommand(
                            plugin.getServer().getConsoleSender(),
                            cmd
                    );
                    if (!success) {
                        plugin.getLogger().warning("Failed to execute command for player " + player.getName() + ": " + cmd);
                        allCommandsSuccessful = false;
                        // Break on first failure for transactional behavior
                        break;
                    }
                } catch (Exception e) {
                    plugin.getLogger().severe("Error executing command for player " + player.getName() + ": " + cmd);
                    plugin.getLogger().severe("Error message: " + e.getMessage());
                    allCommandsSuccessful = false;
                    // Break on first exception for transactional behavior
                    break;
                }
            }

            // Play sound feedback - only if all commands were attempted
            player.playSound(player.getLocation(), reward.getSound(plugin), reward.soundVolume(), reward.soundPitch());

            // Update claimed rewards regardless of success to prevent spamming
            // But provide different feedback based on whether commands succeeded
            claimed.add(rewardId);
            // Update the cache with the modified list
            claimedRewardsDataHandler.saveClaimedRewards(uuid, claimed);
            
            if (allCommandsSuccessful) {
                player.sendMessage(net.kyori.adventure.text.Component.text("Reward claimed successfully!").color(net.kyori.adventure.text.format.NamedTextColor.GREEN));
                refreshGUI(player); // Refresh GUI first to show the updated state
                player.closeInventory(); // Then close the inventory
            } else {
                player.sendMessage(net.kyori.adventure.text.Component.text("Reward processed with errors! Some commands failed - please contact an admin.").color(net.kyori.adventure.text.format.NamedTextColor.YELLOW));
                plugin.getLogger().warning("Reward " + rewardId + " processed for " + player.getName() + " but some commands failed");
                refreshGUI(player); // Refresh GUI first to show the updated state
                player.closeInventory(); // Then close the inventory
            }
        }

        /* For all other items (static items like fillers, placeholders, etc.)
        if (data.has(staticItemKey, PersistentDataType.BOOLEAN)) {
            //plugin.getLogger().info("Clicked static item, cancelling");
            // Cancel interaction with static items
            return;
        }*/

        // Cancel interaction with any other items
        //plugin.getLogger().info("Clicked unknown item, cancelling");
    }

    private void sendAlreadyClaimedMessage(Player player) {
        player.sendMessage(net.kyori.adventure.text.Component.text("You have already claimed this reward!").color(net.kyori.adventure.text.format.NamedTextColor.RED));
    }

    private void refreshGUI(Player player) {
        int currentPage = player.getOpenInventory().getTopInventory()
                .getHolder() instanceof PageAwareHolder holder ? holder.getPage() : 0;
        RewardsGUI.open(player, plugin.getPlaytimeTracker(), this, currentPage);
    }

    @EventHandler(priority = org.bukkit.event.EventPriority.HIGHEST)
    public void onInventoryDrag(InventoryDragEvent event) {
        if (event.getView().getTopInventory().getHolder() instanceof RewardsGUI.RewardsGUIHolder) {
            event.setCancelled(true);
        }
    }

    
    // Public methods for accessing claimed rewards data
    public List<String> getClaimedRewards(UUID uuid) {
        return claimedRewardsDataHandler.getClaimedRewards(uuid);
    }

    public void resetClaimedRewards(UUID uuid) {
        claimedRewardsDataHandler.resetClaimedRewards(uuid);
    }
}
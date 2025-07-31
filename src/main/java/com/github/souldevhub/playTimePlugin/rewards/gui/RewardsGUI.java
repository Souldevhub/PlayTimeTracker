package com.github.souldevhub.playTimePlugin.rewards.gui;

import com.github.souldevhub.playTimePlugin.config.PlaytimeConfig;
import com.github.souldevhub.playTimePlugin.playtime.PlaytimeTracker;
import com.github.souldevhub.playTimePlugin.listeners.RewardsGUIListener;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.HashSet;
import java.util.Set;

public class RewardsGUI {
    private static JavaPlugin plugin;
    private static NamespacedKey staticItemKey;

    public static void init(JavaPlugin plugin) {
        RewardsGUI.plugin = plugin;
        staticItemKey = new NamespacedKey(plugin, "static_item");
        //plugin.getLogger().info("RewardsGUI initialized");
    }

    public static class RewardsGUIHolder extends PageAwareHolder {
        public RewardsGUIHolder(int page, Inventory inventory) {
            super(page, inventory);
        }
    }

    /**
     * Get the total number of pages based on rewards configuration
     * @param config The PlaytimeConfig instance
     * @return The total number of pages
     */
    public static int getTotalPages(PlaytimeConfig config) {
        return config.getTotalPages();
    }

    public static void open(Player player, PlaytimeTracker tracker, RewardsGUIListener rewardsGUIListener, int page) {
        if (plugin == null) {
            throw new IllegalStateException("RewardsGUI not initialized! Call init() first.");
        }
        
        // Validate page number to prevent opening empty pages
        PlaytimeConfig config = PlaytimeConfig.getInstance(plugin);
        int totalPages = getTotalPages(config);
        
        // Don't open if page is invalid
        if (page < 0 || page >= totalPages) {
            player.closeInventory();
            return;
        }
        
        long playtime = tracker.getTotalPlaytime(player.getUniqueId());
        Set<String> claimedRewardsSet = new HashSet<>(rewardsGUIListener.getClaimedRewards(player.getUniqueId()));
        player.openInventory(config.getRewardsGUIForPlayer(playtime, claimedRewardsSet, page));
    }

    public static void open(Player player, PlaytimeTracker tracker, RewardsGUIListener rewardsGUIListener) {
        open(player, tracker, rewardsGUIListener, 0);
    }

    public static void openNextPage(JavaPlugin plugin, Player player, PlaytimeTracker tracker, RewardsGUIListener rewardsGUIListener) {
        int currentPage = getPageFrom(player);
        PlaytimeConfig config = PlaytimeConfig.getInstance(plugin);
        int totalPages = getTotalPages(config);
        
        // Only go to the next page if it exists
        if (currentPage < totalPages - 1) {
            open(player, tracker, rewardsGUIListener, currentPage + 1);
        }
    }

    public static void openPreviousPage(JavaPlugin plugin, Player player, PlaytimeTracker tracker, RewardsGUIListener rewardsGUIListener) {
        int currentPage = getPageFrom(player);
        // Only go to the previous page if not on the first page
        if (currentPage > 0) {
            open(player, tracker, rewardsGUIListener, currentPage - 1);
        }
    }

    private static int getPageFrom(Player player) {
        return player.getOpenInventory().getTopInventory()
                .getHolder() instanceof PageAwareHolder holder ? holder.getPage() : 0;
    }

    /**
     * Marks an item as unmovable in the GUI
     * @param meta The ItemMeta to mark as unmovable
     */
    public static void markUnmovable(ItemMeta meta) {
        if (meta != null && staticItemKey != null) {
            meta.getPersistentDataContainer().set(staticItemKey, PersistentDataType.BOOLEAN, true);
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS);
        }
    }
}
package com.github.souldevhub.playTimePlugin.config;

import com.github.souldevhub.playTimePlugin.PlaytimePulse;
import com.github.souldevhub.playTimePlugin.constants.GUIConstants;
import com.github.souldevhub.playTimePlugin.rewards.RewardSlot;
import com.github.souldevhub.playTimePlugin.rewards.gui.NavigationButton;
import com.github.souldevhub.playTimePlugin.rewards.gui.RewardsGUI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class PlaytimeConfig {
    private static PlaytimeConfig instance;
    private final JavaPlugin plugin;
    private FileConfiguration config;
    private final List<RewardSlot> rewardSlots = new ArrayList<>();
    public static final String NAV_PREV = GUIConstants.NAV_PREV;
    public static final String NAV_NEXT = GUIConstants.NAV_NEXT;
    public static final String NAV_CLOSE = GUIConstants.NAV_CLOSE;

    // Translation Settings
    private String statusText = "Status:";
    private String claimedText = "Claimed";
    private String readyToClaimText = "Ready to Claim";
    private String notClaimedText = "Not Claimed";
    private String requiredTimeText = "Required Time:";
    private String yourTimeText = "Your Time:";
    private String alreadyClaimedText = "Already claimed";
    private String rewardNotFoundText = "Reward not found";
    private String noRewardsAvailableText = "No rewards available";
    private String readyToClaimStatusText = "Ready to claim";

    private int guiSize = 54;
    private Material fillUnusedSlots = Material.BLACK_STAINED_GLASS_PANE;
    private boolean navigationEnabled = true;
    private Material prevPageMaterial = Material.ARROW;
    private String prevPageName = "&b← Previous Page";
    private String prevPageHeadId = null;
    private Material nextPageMaterial = Material.ARROW;
    private String nextPageName = "&bNext Page →";
    private String nextPageHeadId = null;
    private Material closeMaterial = Material.BARRIER;
    private String closeName = "&c✖ Close";
    private String closeHeadId = null;
    private final NamespacedKey rewardIdKey;
    private final NamespacedKey navButtonTypeKey;

    private PlaytimeConfig(JavaPlugin plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
        
        // Only show detailed logs if debug mode is enabled
        boolean isDebug = false;
        if (plugin instanceof PlaytimePulse) {
            isDebug = ((PlaytimePulse) plugin).isDebugMode();
        }
        
        if (isDebug) {
            plugin.getLogger().info("Starting PlaytimeConfig initialization...");
            plugin.getLogger().info("Config loaded");
        }
        
        rewardIdKey = new NamespacedKey(plugin, "reward_id");
        navButtonTypeKey = new NamespacedKey(plugin, "nav_button_type");
        
        if (isDebug) {
            plugin.getLogger().info("Namespaced keys created");
        }
        
        loadGuiConfig();
        loadTranslations();
        loadConfig(plugin);
        
        // Always show the final initialization message
        plugin.getLogger().info("PlaytimeConfig initialized with " + rewardSlots.size() + " rewards");
    }

    public static PlaytimeConfig getInstance(JavaPlugin plugin) {
        if (instance == null) {
            instance = new PlaytimeConfig(plugin);
        }
        return instance;
    }

    public void loadConfig(JavaPlugin plugin) {
        plugin.reloadConfig();
        this.config = plugin.getConfig();
        loadGuiConfig();
        rewardSlots.clear();
        
        // Debug logging
        if (plugin instanceof PlaytimePulse playtimePulse && playtimePulse.isDebugMode()) {
            plugin.getLogger().info("[DEBUG] Loading rewards configuration...");
            plugin.getLogger().info("Checking for rewards section in config...");
        }
        
        if (config.isConfigurationSection("rewards")) {
            if (plugin instanceof PlaytimePulse playtimePulse && playtimePulse.isDebugMode()) {
                plugin.getLogger().info("Rewards section found in config");
            }
            
            // Fix potential NullPointerException
            var rewardsSection = config.getConfigurationSection("rewards");
            if (rewardsSection != null) {
                if (plugin instanceof PlaytimePulse playtimePulse && playtimePulse.isDebugMode()) {
                    plugin.getLogger().info("Successfully got rewards section, found " + rewardsSection.getKeys(false).size() + " reward keys");
                }
                int loadedRewards = 0;
                int failedRewards = 0;
                
                for (String key : rewardsSection.getKeys(false)) {
                    // Debug logging
                    if (plugin instanceof PlaytimePulse playtimePulse && playtimePulse.isDebugMode()) {
                        plugin.getLogger().info("[DEBUG] Processing reward key: " + key);
                        plugin.getLogger().info("Processing reward: " + key);
                    }
                    
                    try {
                        // Fix potential NullPointerException
                        var rewardSection = config.getConfigurationSection("rewards." + key);
                        if (rewardSection != null) {
                            if (plugin instanceof PlaytimePulse playtimePulse && playtimePulse.isDebugMode()) {
                                plugin.getLogger().info("Successfully got reward section for: " + key);
                            }
                            // Check if this section has nested reward sections (indentation error)
                            boolean hasNestedRewards = false;
                            for (String subKey : rewardSection.getKeys(false)) {
                                if (config.isConfigurationSection("rewards." + key + "." + subKey)) {
                                    // This indicates a nested reward - likely an indentation error
                                    hasNestedRewards = true;
                                    plugin.getLogger().warning("Possible indentation error in reward '" + key + 
                                        "': Found nested section '" + subKey + 
                                        "'. Check if this should be a separate reward.");
                                }
                            }
                            
                            Map<String, Object> rewardData = rewardSection.getValues(true);
                            if (plugin instanceof PlaytimePulse playtimePulse && playtimePulse.isDebugMode()) {
                                plugin.getLogger().info("Reward data for " + key + " has " + rewardData.size() + " entries");
                                plugin.getLogger().info("[DEBUG] Reward data keys: " + rewardData.keySet());
                            }
                            
                            RewardSlot reward = RewardSlot.fromMap(rewardData);
                            if (reward != null) {
                                rewardSlots.add(reward);
                                loadedRewards++;
                                if (plugin instanceof PlaytimePulse playtimePulse && playtimePulse.isDebugMode()) {
                                    plugin.getLogger().info("Successfully loaded reward: " + key);
                                }
                            } else {
                                plugin.getLogger().warning("Failed to load reward with id: " + key + 
                                    " - Check for missing required fields or incorrect data types");
                                failedRewards++;
                            }
                        } else {
                            plugin.getLogger().warning("Failed to load reward section with id: " + key);
                            failedRewards++;
                        }
                    } catch (Exception e) {
                        plugin.getLogger().warning("Failed to load reward with id: " + key + " - " + e.getMessage());
                        e.printStackTrace();
                        failedRewards++;
                    }
                }
                
                if (plugin instanceof PlaytimePulse playtimePulse && playtimePulse.isDebugMode()) {
                    plugin.getLogger().info("Reward loading complete: " + loadedRewards + " loaded, " + failedRewards + " failed");
                }
                
                // Additional validation - check for duplicate slots on the same page
                Map<String, List<RewardSlot>> rewardsByPage = rewardSlots.stream()
                    .collect(Collectors.groupingBy(r -> r.page() + ":" + r.slot()));
                
                for (Map.Entry<String, List<RewardSlot>> entry : rewardsByPage.entrySet()) {
                    if (entry.getValue().size() > 1) {
                        String[] parts = entry.getKey().split(":");
                        plugin.getLogger().warning("Slot conflict detected: Page " + parts[0] + 
                            ", Slot " + parts[1] + " is used by " + entry.getValue().size() + " rewards:");
                        for (RewardSlot reward : entry.getValue()) {
                            plugin.getLogger().warning("  - " + reward.id());
                        }
                    }
                }
            } else {
                plugin.getLogger().warning("Rewards section is null despite being detected");
            }
        } else {
            plugin.getLogger().warning("No 'rewards' section found in config!");
        }
    }

    public List<RewardSlot> getRewardSlots() {
        return Collections.unmodifiableList(rewardSlots);
    }

    /**
     * Calculate the total number of pages needed based on rewards
     * @return The total number of pages
     */
    public int getTotalPages() {
        // Calculate total pages based on the highest page number in reward slots
        return rewardSlots.stream()
                .mapToInt(RewardSlot::page)
                .max()
                .orElse(-1) + 1; // +1 because pages are 0-indexed
    }

    public Optional<RewardSlot> getRewardSlotById(String id) {
        return rewardSlots.stream()
                .filter(r -> r.id().equalsIgnoreCase(id))
                .findFirst();
    }

    public Inventory getRewardsGUIForPlayer(long playtime, Set<String> claimed, int page) {
        // Create the holder with null inventory first
        RewardsGUI.RewardsGUIHolder holder = new RewardsGUI.RewardsGUIHolder(page, null);
        // Create the inventory with the holder
        Inventory inventory = Bukkit.createInventory(holder, guiSize, Component.text("Playtime Rewards - Page " + (page + 1)));
        // Update the holder with the actual inventory
        holder.setInventory(inventory);
        Set<Integer> usedSlots = new HashSet<>();
        
        // Get rewards for the current page based on the reward's page property
        List<RewardSlot> pageRewards = getPageRewards(page);

        for (RewardSlot reward : pageRewards) {
            usedSlots.add(reward.slot());
            if (claimed.contains(reward.id())) {
                ItemStack claimedItem = new ItemStack(Material.BARRIER);
                ItemMeta claimedMeta = claimedItem.getItemMeta();
                if (claimedMeta != null) {
                    // Use the reward's name if available, otherwise default to "Reward"
                    String rewardName = (reward.name() != null && !reward.name().isEmpty()) ? 
                        LegacyComponentSerializer.legacyAmpersand().deserialize(reward.name()).content() : 
                        "Reward";
                    
                    claimedMeta.displayName(Component.text(rewardName + " (CLAIMED)").color(NamedTextColor.RED));
                    List<Component> lore = new ArrayList<>();
                    lore.add(Component.text(statusText).color(NamedTextColor.GOLD)
                            .append(Component.text(claimedText).color(NamedTextColor.GREEN)));
                    lore.add(Component.text(requiredTimeText).color(NamedTextColor.GOLD)
                            .append(Component.text(formatPlaytime(reward.requiredPlaytime())).color(NamedTextColor.YELLOW)));
                    RewardsGUI.markUnmovable(claimedMeta);
                    claimedMeta.lore(lore);
                    claimedItem.setItemMeta(claimedMeta);
                }
                inventory.setItem(reward.slot(), claimedItem);
            } else if (playtime >= reward.requiredPlaytime()) {
                // We need to pass a player UUID to support placeholders, but this method doesn't have access to it
                // For now, we'll continue using the existing method
                ItemStack item = createRewardItem(reward);
                ItemMeta meta = item.getItemMeta();
                if (meta != null) {
                    meta.getPersistentDataContainer().set(rewardIdKey, PersistentDataType.STRING, reward.id());
                    meta.addEnchant(org.bukkit.enchantments.Enchantment.UNBREAKING, 1, true);
                    meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

                    List<Component> lore = new ArrayList<>();
                    List<Component> existingLore = meta.lore();
                    if (existingLore != null) {
                        lore.addAll(existingLore);
                    }
                    lore.add(Component.empty());
                    lore.add(Component.text(statusText).color(NamedTextColor.GOLD)
                            .append(Component.text(readyToClaimText).color(NamedTextColor.GREEN)));
                    lore.add(Component.text(requiredTimeText).color(NamedTextColor.GOLD)
                            .append(Component.text(formatPlaytime(reward.requiredPlaytime())).color(NamedTextColor.YELLOW)));
                    meta.lore(lore);
                    
                    item.setItemMeta(meta);
                }
                inventory.setItem(reward.slot(), item);
            } else {
                // We need to pass a player UUID to support placeholders, but this method doesn't have access to it
                // For now, we'll continue using the existing method
                ItemStack item = createRewardItem(reward);
                ItemMeta meta = item.getItemMeta();
                if (meta != null) {
                    List<Component> lore = new ArrayList<>();
                    List<Component> existingLore = meta.lore();
                    if (existingLore != null) {
                        lore.addAll(existingLore);
                    }
                    lore.add(Component.empty());
                    lore.add(Component.text(statusText).color(NamedTextColor.GOLD)
                            .append(Component.text(notClaimedText).color(NamedTextColor.RED)));
                    lore.add(Component.text(requiredTimeText).color(NamedTextColor.GOLD)
                            .append(Component.text(formatPlaytime(reward.requiredPlaytime())).color(NamedTextColor.YELLOW)));
                    lore.add(Component.text(yourTimeText).color(NamedTextColor.GOLD)
                            .append(Component.text(formatPlaytime(playtime)).color(NamedTextColor.YELLOW)));
                    meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                    meta.lore(lore);
                    item.setItemMeta(meta);
                }
                inventory.setItem(reward.slot(), item);
            }
        }

        int navigationStartSlot = guiSize - 9;
        for (int i = 0; i < guiSize; i++) {
            if (usedSlots.contains(i)) {
                continue;
            }
            if (navigationEnabled && guiSize > 9 && i >= navigationStartSlot) {
                continue;
            }
            inventory.setItem(i, createPlaceholderItem(fillUnusedSlots));
        }

        if (navigationEnabled && guiSize > 9) {
            int navRowStart = guiSize - 9;
            if (page > 0) {
                ItemStack prevButton = createNavigationButton(prevPageMaterial, prevPageName, prevPageHeadId, NAV_PREV);
                inventory.setItem(navRowStart + 3, prevButton);
            } else {
                inventory.setItem(navRowStart + 3, createPlaceholderItem(Material.GRAY_STAINED_GLASS_PANE));
            }

            // Check if this is the last page with rewards
            int totalPages = getTotalPages();
            if (page < totalPages - 1) {
                ItemStack nextButton = createNavigationButton(nextPageMaterial, nextPageName, nextPageHeadId, NAV_NEXT);
                inventory.setItem(navRowStart + 5, nextButton);
            } else {
                // Show "More Rewards Coming Soon" item instead of next button
                ItemStack moreRewardsItem = new ItemStack(Material.CLOCK);
                ItemMeta moreRewardsMeta = moreRewardsItem.getItemMeta();
                if (moreRewardsMeta != null) {
                    moreRewardsMeta.displayName(Component.text("More Rewards Coming Soon!").color(NamedTextColor.GOLD));
                    List<Component> lore = new ArrayList<>();
                    lore.add(Component.text("Check back later for new rewards!").color(NamedTextColor.YELLOW));
                    moreRewardsMeta.lore(lore);
                    RewardsGUI.markUnmovable(moreRewardsMeta);
                    moreRewardsItem.setItemMeta(moreRewardsMeta);
                }
                inventory.setItem(navRowStart + 5, moreRewardsItem);
            }

            ItemStack closeButton = createNavigationButton(closeMaterial, closeName, closeHeadId, NAV_CLOSE);
            inventory.setItem(navRowStart + 8, closeButton);
        }

        return inventory;
    }

    public Inventory getRewardsGUIForPlayer(long playtime, Set<String> claimed, int page, UUID playerUUID) {
        // Create the holder with null inventory first
        RewardsGUI.RewardsGUIHolder holder = new RewardsGUI.RewardsGUIHolder(page, null);
        // Create the inventory with the holder
        Inventory inventory = Bukkit.createInventory(holder, guiSize, Component.text("Playtime Rewards - Page " + (page + 1)));
        // Update the holder with the actual inventory
        holder.setInventory(inventory);
        Set<Integer> usedSlots = new HashSet<>();
        
        // Get rewards for the current page based on the reward's page property
        List<RewardSlot> pageRewards = getPageRewards(page);

        for (RewardSlot reward : pageRewards) {
            usedSlots.add(reward.slot());
            if (claimed.contains(reward.id())) {
                ItemStack claimedItem = new ItemStack(Material.BARRIER);
                ItemMeta claimedMeta = claimedItem.getItemMeta();
                if (claimedMeta != null) {
                    // Use the reward's name if available, otherwise default to "Reward"
                    String rewardName = (reward.name() != null && !reward.name().isEmpty()) ? 
                        LegacyComponentSerializer.legacyAmpersand().deserialize(reward.name()).content() : 
                        "Reward";
                    
                    claimedMeta.displayName(Component.text(rewardName + " (CLAIMED)").color(NamedTextColor.RED));
                    List<Component> lore = new ArrayList<>();
                    lore.add(Component.text(statusText).color(NamedTextColor.GOLD)
                            .append(Component.text(claimedText).color(NamedTextColor.GREEN)));
                    lore.add(Component.text(requiredTimeText).color(NamedTextColor.GOLD)
                            .append(Component.text(formatPlaytime(reward.requiredPlaytime())).color(NamedTextColor.YELLOW)));
                    RewardsGUI.markUnmovable(claimedMeta);
                    claimedMeta.lore(lore);
                    claimedItem.setItemMeta(claimedMeta);
                }
                inventory.setItem(reward.slot(), claimedItem);
            } else if (playtime >= reward.requiredPlaytime()) {
                // Use the new method that supports placeholders
                ItemStack item = createRewardItem(reward, playerUUID);
                ItemMeta meta = item.getItemMeta();
                if (meta != null) {
                    meta.getPersistentDataContainer().set(rewardIdKey, PersistentDataType.STRING, reward.id());
                    meta.addEnchant(org.bukkit.enchantments.Enchantment.UNBREAKING, 1, true);
                    meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

                    List<Component> lore = new ArrayList<>();
                    List<Component> existingLore = meta.lore();
                    if (existingLore != null) {
                        lore.addAll(existingLore);
                    }
                    lore.add(Component.empty());
                    lore.add(Component.text(statusText).color(NamedTextColor.GOLD)
                            .append(Component.text(readyToClaimText).color(NamedTextColor.GREEN)));
                    lore.add(Component.text(requiredTimeText).color(NamedTextColor.GOLD)
                            .append(Component.text(formatPlaytime(reward.requiredPlaytime())).color(NamedTextColor.YELLOW)));
                    meta.lore(lore);
                    
                    item.setItemMeta(meta);
                }
                inventory.setItem(reward.slot(), item);
            } else {
                // Use the new method that supports placeholders
                ItemStack item = createRewardItem(reward, playerUUID);
                ItemMeta meta = item.getItemMeta();
                if (meta != null) {
                    List<Component> lore = new ArrayList<>();
                    List<Component> existingLore = meta.lore();
                    if (existingLore != null) {
                        lore.addAll(existingLore);
                    }
                    lore.add(Component.empty());
                    lore.add(Component.text(statusText).color(NamedTextColor.GOLD)
                            .append(Component.text(notClaimedText).color(NamedTextColor.RED)));
                    lore.add(Component.text(requiredTimeText).color(NamedTextColor.GOLD)
                            .append(Component.text(formatPlaytime(reward.requiredPlaytime())).color(NamedTextColor.YELLOW)));
                    lore.add(Component.text(yourTimeText).color(NamedTextColor.GOLD)
                            .append(Component.text(formatPlaytime(playtime)).color(NamedTextColor.YELLOW)));
                    meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                    meta.lore(lore);
                    item.setItemMeta(meta);
                }
                inventory.setItem(reward.slot(), item);
            }
        }

        int navigationStartSlot = guiSize - 9;
        for (int i = 0; i < guiSize; i++) {
            // Skip slots that are already filled with rewards
            if (usedSlots.contains(i)) {
                continue;
            }
            
            // Skip navigation slots if navigation is enabled
            if (navigationEnabled && guiSize > 9 && i >= navigationStartSlot) {
                continue;
            }
            
            ItemStack placeholder = new ItemStack(fillUnusedSlots);
            ItemMeta placeholderMeta = placeholder.getItemMeta();
            if (placeholderMeta != null) {
                placeholderMeta.displayName(Component.text(" ")); // Empty name to avoid item name showing
                RewardsGUI.markUnmovable(placeholderMeta); // Make item non-movable
                placeholder.setItemMeta(placeholderMeta);
            }
            inventory.setItem(i, placeholder);
        }

        // Add navigation buttons if enabled and there's space
        if (navigationEnabled && guiSize > 9) {
            int navRowStart = guiSize - 9;
            
            // Previous page button - disabled on first page
            if (page > 0) {
                ItemStack prevButton = new NavigationButton(prevPageMaterial, prevPageName, prevPageHeadId).createItem();
                ItemMeta prevMeta = prevButton.getItemMeta();
                if (prevMeta != null) {
                    prevMeta.getPersistentDataContainer().set(navButtonTypeKey, PersistentDataType.STRING, NAV_PREV);
                    RewardsGUI.markUnmovable(prevMeta); // Unified approach to immovability
                    prevButton.setItemMeta(prevMeta);
                }
                inventory.setItem(navRowStart + 3, prevButton);
            } else {
                // Disabled previous button on first page
                inventory.setItem(navRowStart + 3, createPlaceholderItem(Material.GRAY_STAINED_GLASS_PANE));
            }
            
            // Next page button
            ItemStack nextButton = new NavigationButton(nextPageMaterial, nextPageName, nextPageHeadId).createItem();
            ItemMeta nextMeta = nextButton.getItemMeta();
            if (nextMeta != null) {
                nextMeta.getPersistentDataContainer().set(navButtonTypeKey, PersistentDataType.STRING, NAV_NEXT);
                RewardsGUI.markUnmovable(nextMeta); // Unified approach to immovability
                nextButton.setItemMeta(nextMeta);
            }
            inventory.setItem(navRowStart + 5, nextButton);
            
            // More rewards coming soon item
            ItemStack moreRewardsItem = new ItemStack(Material.CHEST);
            ItemMeta moreRewardsMeta = moreRewardsItem.getItemMeta();
            if (moreRewardsMeta != null) {
                moreRewardsMeta.displayName(Component.text("More Rewards").color(NamedTextColor.GOLD));
                List<Component> lore = new ArrayList<>();
                lore.add(Component.text("Check back later for new rewards!").color(NamedTextColor.YELLOW));
                moreRewardsMeta.lore(lore);
                RewardsGUI.markUnmovable(moreRewardsMeta);
                moreRewardsItem.setItemMeta(moreRewardsMeta);
            }
            inventory.setItem(navRowStart + 5, moreRewardsItem);
            
            // Close button
            ItemStack closeButton = new NavigationButton(closeMaterial, closeName, closeHeadId).createItem();
            ItemMeta closeMeta = closeButton.getItemMeta();
            if (closeMeta != null) {
                closeMeta.getPersistentDataContainer().set(navButtonTypeKey, PersistentDataType.STRING, NAV_CLOSE);
                RewardsGUI.markUnmovable(closeMeta); // Unified approach to immovability
                closeButton.setItemMeta(closeMeta);
            }
            inventory.setItem(navRowStart + 8, closeButton);
        }

        return inventory;
    }

    // Creates a navigation button with the specified material, name, headId, and type
    private ItemStack createNavigationButton(Material material, String name, String headId, String buttonType) {
        ItemStack button = new NavigationButton(material, name, headId).createItem();
        ItemMeta meta = button.getItemMeta();
        if (meta != null) {
            meta.getPersistentDataContainer().set(navButtonTypeKey, PersistentDataType.STRING, buttonType);
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS);
            button.setItemMeta(meta);
        }
        return button;
    }

    /**
     * Get rewards for a specific page based on the reward's page property
     * @param page The page number
     * @return List of rewards for the specified page
     */
    private List<RewardSlot> getPageRewards(int page) {
        List<RewardSlot> allRewards = getRewardSlots();
        List<RewardSlot> pageRewards = new ArrayList<>();
        for (RewardSlot reward : allRewards) {
            if (reward.page() == page) {
                pageRewards.add(reward);
            }
        }
        return pageRewards;
    }


    private ItemStack createRewardItem(RewardSlot reward) {
        ItemStack item;
        if (reward.headId() != null && !reward.headId().isEmpty() && reward.material() == Material.PLAYER_HEAD) {
            item = createCustomHead(reward.headId());
        } else {
            item = new ItemStack(reward.material());
        }

        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            if (reward.name() != null && !reward.name().isEmpty()) {
                meta.displayName(LegacyComponentSerializer.legacyAmpersand().deserialize(reward.name()));
            }
            if (!reward.lore().isEmpty()) {
                List<Component> lore = new ArrayList<>();
                for (String line : reward.lore()) {
                    lore.add(LegacyComponentSerializer.legacyAmpersand().deserialize(line));
                }
                meta.lore(lore);
            }
            meta.getPersistentDataContainer().set(rewardIdKey, PersistentDataType.STRING, reward.id());
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS);
            item.setItemMeta(meta);
        }
        return item;
    }
    
    private ItemStack createRewardItem(RewardSlot reward, UUID playerUUID) {
        ItemStack item;
        if (reward.headId() != null && !reward.headId().isEmpty() && reward.material() == Material.PLAYER_HEAD) {
            item = createCustomHead(reward.headId());
        } else {
            item = new ItemStack(reward.material());
        }

        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            if (reward.name() != null && !reward.name().isEmpty()) {
                meta.displayName(LegacyComponentSerializer.legacyAmpersand().deserialize(reward.name()));
            }
            
            if (!reward.lore().isEmpty()) {
                List<Component> lore = new ArrayList<>();
                // Process placeholders in lore if PlaceholderAPI is available
                for (String line : reward.lore()) {
                    String processedLine = line;
                    if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
                        org.bukkit.OfflinePlayer player = Bukkit.getOfflinePlayer(playerUUID);
                        processedLine = me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(player, processedLine);
                    }
                    lore.add(LegacyComponentSerializer.legacyAmpersand().deserialize(processedLine));
                }
                meta.lore(lore);
            }
            
            meta.getPersistentDataContainer().set(rewardIdKey, PersistentDataType.STRING, reward.id());
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createPlaceholderItem(Material material) {
        ItemStack placeholder = new ItemStack(material);
        ItemMeta placeholderMeta = placeholder.getItemMeta();
        if (placeholderMeta != null) {
            placeholderMeta.displayName(Component.text(" "));
            RewardsGUI.markUnmovable(placeholderMeta);
            placeholder.setItemMeta(placeholderMeta);
        }
        return placeholder;
    }

    private String formatPlaytime(long seconds) {
        long days = seconds / 86400;
        long hours = (seconds % 86400) / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60; // Added seconds
        StringBuilder sb = new StringBuilder();
        if (days > 0) {
            sb.append(days).append("d ");
        }
        if (hours > 0) {
            sb.append(hours).append("h ");
        }
        if (minutes > 0) {
            sb.append(minutes).append("m ");
        }
        if (secs > 0 || sb.length() == 0) { // Include seconds in the output
            sb.append(secs).append("s");
        }
        return sb.toString().trim();
    }

    private ItemStack createCustomHead(String texture) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        if (texture == null || texture.isEmpty()) return head;
        try {
            ItemMeta meta = head.getItemMeta();
            if (meta instanceof org.bukkit.inventory.meta.SkullMeta skullMeta) {
                com.destroystokyo.paper.profile.PlayerProfile profile = Bukkit.createProfile(UUID.randomUUID());
                profile.setProperty(new com.destroystokyo.paper.profile.ProfileProperty("textures", texture));
                skullMeta.setPlayerProfile(profile);
                head.setItemMeta(skullMeta);
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to create custom head with texture: " + texture + " - " + e.getMessage());
        }
        return head;
    }

    private void loadGuiConfig() {
        // Only show these logs if debug mode is enabled
        if (plugin instanceof PlaytimePulse playtimePulse && playtimePulse.isDebugMode()) {
            plugin.getLogger().info("Loading GUI config...");
        }
        
        if (config.isConfigurationSection("gui")) {
            if (plugin instanceof PlaytimePulse playtimePulse && playtimePulse.isDebugMode()) {
                plugin.getLogger().info("GUI section found in config");
            }
            
            guiSize = config.getInt("gui.slots", 54);
            config.getInt("gui.slots-per-page", 45);
            String fillMaterialStr = config.getString("gui.fill-unused-slots", "BLACK_STAINED_GLASS_PANE");
            try {
                fillUnusedSlots = Material.valueOf(fillMaterialStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid fill-unused-slots material: " + fillMaterialStr + ", using BLACK_STAINED_GLASS_PANE");
                fillUnusedSlots = Material.BLACK_STAINED_GLASS_PANE;
            }
            
            if (config.isConfigurationSection("gui.navigation")) {
                if (plugin instanceof PlaytimePulse playtimePulse && playtimePulse.isDebugMode()) {
                    plugin.getLogger().info("Navigation section found in config");
                }
                navigationEnabled = config.getBoolean("gui.navigation.enabled", true);
                if (config.isConfigurationSection("gui.navigation.prev-page")) {
                    if (plugin instanceof PlaytimePulse playtimePulse && playtimePulse.isDebugMode()) {
                        plugin.getLogger().info("Prev-page section found in config");
                    }
                    String materialStr = config.getString("gui.navigation.prev-page.material", "ARROW");
                    try {
                        prevPageMaterial = Material.valueOf(materialStr.toUpperCase());
                    } catch (IllegalArgumentException e) {
                        plugin.getLogger().warning("Invalid prev-page material: " + materialStr + ", using ARROW");
                        prevPageMaterial = Material.ARROW;
                    }
                    prevPageName = config.getString("gui.navigation.prev-page.name", "&b← Previous Page");
                    prevPageHeadId = config.getString("gui.navigation.prev-page.headId", null);
                }
                if (config.isConfigurationSection("gui.navigation.next-page")) {
                    if (plugin instanceof PlaytimePulse playtimePulse && playtimePulse.isDebugMode()) {
                        plugin.getLogger().info("Next-page section found in config");
                    }
                    String materialStr = config.getString("gui.navigation.next-page.material", "ARROW");
                    try {
                        nextPageMaterial = Material.valueOf(materialStr.toUpperCase());
                    } catch (IllegalArgumentException e) {
                        plugin.getLogger().warning("Invalid next-page material: " + materialStr + ", using ARROW");
                        nextPageMaterial = Material.ARROW;
                    }
                    nextPageName = config.getString("gui.navigation.next-page.name", "&bNext Page →");
                    nextPageHeadId = config.getString("gui.navigation.next-page.headId", null);
                }
                if (config.isConfigurationSection("gui.navigation.close")) {
                    if (plugin instanceof PlaytimePulse playtimePulse && playtimePulse.isDebugMode()) {
                        plugin.getLogger().info("Close section found in config");
                    }
                    String materialStr = config.getString("gui.navigation.close.material", "BARRIER");
                    try {
                        closeMaterial = Material.valueOf(materialStr.toUpperCase());
                    } catch (IllegalArgumentException e) {
                        plugin.getLogger().warning("Invalid close material: " + materialStr + ", using BARRIER");
                        closeMaterial = Material.BARRIER;
                    }
                    closeName = config.getString("gui.navigation.close.name", "&c✖ Close");
                    closeHeadId = config.getString("gui.navigation.close.headId", null);
                }
            }
        }
        if (plugin instanceof PlaytimePulse playtimePulse && playtimePulse.isDebugMode()) {
            plugin.getLogger().info("GUI config loading complete");
        }
    }

    private void loadTranslations() {
        // Get the language from config
        String language = config.getString("language", "en");
        
        // Only show this log if debug mode is enabled
        if (plugin instanceof PlaytimePulse playtimePulse && playtimePulse.isDebugMode()) {
            plugin.getLogger().info("Loading translations for language: " + language);
        }
        
        // Try to load language file
        File langDir = new File(plugin.getDataFolder(), "lang");
        if (!langDir.exists()) {
            langDir.mkdirs();
        }
        
        File langFile = new File(langDir, language + ".yml");
        FileConfiguration langConfig = null;
        
        if (langFile.exists()) {
            // Load custom language file
            langConfig = YamlConfiguration.loadConfiguration(langFile);
            if (plugin instanceof PlaytimePulse playtimePulse && playtimePulse.isDebugMode()) {
                plugin.getLogger().info("Loaded custom language file: " + language + ".yml - Using custom translations");
            }
        } else {
            // Try to load from resources
            try {
                InputStream resourceStream = plugin.getResource("lang/" + language + ".yml");
                if (resourceStream != null) {
                    langConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(resourceStream));
                    if (plugin instanceof PlaytimePulse playtimePulse && playtimePulse.isDebugMode()) {
                        plugin.getLogger().info("Loaded built-in language file: " + language + ".yml - Using default translations");
                    }
                    
                    // Copy to data folder for user customization
                    // Get a fresh stream for copying since the previous one was consumed
                    InputStream copyStream = plugin.getResource("lang/" + language + ".yml");
                    if (copyStream != null) {
                        File outFile = new File(langDir, language + ".yml");
                        try (OutputStream out = new FileOutputStream(outFile)) {
                            byte[] buffer = new byte[1024];
                            int length;
                            while ((length = copyStream.read(buffer)) > 0) {
                                out.write(buffer, 0, length);
                            }
                            if (plugin instanceof PlaytimePulse playtimePulse && playtimePulse.isDebugMode()) {
                                plugin.getLogger().info("Copied built-in language file to plugin data directory for customization");
                            }
                        } finally {
                            copyStream.close();
                        }
                    }
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to load language file: " + language + ".yml - " + e.getMessage());
            }
        }
        
        // Load translations either from language file or config
        if (langConfig != null && langConfig.isConfigurationSection("gui.translations")) {
            statusText = langConfig.getString("gui.translations.status", statusText);
            claimedText = langConfig.getString("gui.translations.claimed", claimedText);
            readyToClaimText = langConfig.getString("gui.translations.ready_to_claim", readyToClaimText);
            notClaimedText = langConfig.getString("gui.translations.not_claimed", notClaimedText);
            requiredTimeText = langConfig.getString("gui.translations.required_time", requiredTimeText);
            yourTimeText = langConfig.getString("gui.translations.your_time", yourTimeText);
            alreadyClaimedText = langConfig.getString("gui.translations.already_claimed", alreadyClaimedText);
            rewardNotFoundText = langConfig.getString("gui.translations.reward_not_found", rewardNotFoundText);
            noRewardsAvailableText = langConfig.getString("gui.translations.no_rewards_available", noRewardsAvailableText);
            readyToClaimStatusText = langConfig.getString("gui.translations.ready_to_claim_status", readyToClaimStatusText);
        } else {
            if (plugin instanceof PlaytimePulse playtimePulse && playtimePulse.isDebugMode()) {
                plugin.getLogger().info("Using default English translations");
            }
        }
        
        if (plugin instanceof PlaytimePulse playtimePulse && playtimePulse.isDebugMode()) {
            plugin.getLogger().info("Loaded translations: Status='" + statusText + "', Claimed='" + claimedText + 
                               "', Ready='" + readyToClaimText + "', NotClaimed='" + notClaimedText + "'");
        }
    }


    // Getters for translation strings
    public String getStatusText() {
        return statusText;
    }

    public String getClaimedText() {
        return claimedText;
    }

    public String getReadyToClaimText() {
        return readyToClaimText;
    }

    public String getNotClaimedText() {
        return notClaimedText;
    }

    public String getRequiredTimeText() {
        return requiredTimeText;
    }

    public String getYourTimeText() {
        return yourTimeText;
    }

    public String getReadyToClaimStatusText() {
        return readyToClaimStatusText;
    }
    
    public String getRewardNotFoundText() {
        return rewardNotFoundText;
    }
    
    public String getAlreadyClaimedText() {
        return alreadyClaimedText;
    }
    
    public String getNoRewardsAvailableText() {
        return noRewardsAvailableText;
    }
}
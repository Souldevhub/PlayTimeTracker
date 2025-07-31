package com.github.souldevhub.playTimePlugin.config;

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
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

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
        rewardIdKey = new NamespacedKey(plugin, "reward_id");
        navButtonTypeKey = new NamespacedKey(plugin, "nav_button_type");
        loadGuiConfig();
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
        if (config.isConfigurationSection("rewards")) {
            // 修复潜在的NullPointerException
            var rewardsSection = config.getConfigurationSection("rewards");
            if (rewardsSection != null) {
                int loadedRewards = 0;
                int failedRewards = 0;
                
                for (String key : rewardsSection.getKeys(false)) {
                    try {
                        // 修复潜在的NullPointerException
                        var rewardSection = config.getConfigurationSection("rewards." + key);
                        if (rewardSection != null) {
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
                            RewardSlot reward = RewardSlot.fromMap(rewardData);
                            if (reward != null) {
                                rewardSlots.add(reward);
                                loadedRewards++;
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
                        failedRewards++;
                    }
                }
                
                plugin.getLogger().info("Reward loading complete: " + loadedRewards + " loaded, " + failedRewards + " failed");
                
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
        //plugin.getLogger().info("Creating rewards GUI for player with playtime: "+ playtime + ", page: " + page);
        //plugin.getLogger().info("Claimed rewards: " + claimed.size());
        // Create the holder with null inventory first
        RewardsGUI.RewardsGUIHolder holder = new RewardsGUI.RewardsGUIHolder(page, null);
        // Create the inventory with the holder
        Inventory inventory = Bukkit.createInventory(holder, guiSize, Component.text("Playtime Rewards - Page " + (page + 1)));
        // Update the holder with the actual inventory
        holder.setInventory(inventory);
        Set<Integer> usedSlots = new HashSet<>();
        
        // Get rewards for the current page based on the reward's page property
        List<RewardSlot> pageRewards = getPageRewards(page);
        //plugin.getLogger().info("Page " + page + " contains " + pageRewards.size() + " rewards");

        for (RewardSlot reward : pageRewards) {
            //plugin.getLogger().info("Processing reward: " + reward.id() + " at slot " + reward.slot());
            usedSlots.add(reward.slot());
            if (claimed.contains(reward.id())) {
                //plugin.getLogger().info("Reward " + reward.id() + " already claimed");
                ItemStack claimedItem = new ItemStack(Material.BARRIER);
                ItemMeta claimedMeta = claimedItem.getItemMeta();
                if (claimedMeta != null) {
                    // Use the reward's name if available, otherwise default to "Reward"
                    String rewardName = (reward.name() != null && !reward.name().isEmpty()) ? 
                        LegacyComponentSerializer.legacyAmpersand().deserialize(reward.name()).content() : 
                        "Reward";
                    
                    claimedMeta.displayName(Component.text(rewardName + " (CLAIMED)").color(NamedTextColor.RED));
                    List<Component> lore = new ArrayList<>();
                    lore.add(Component.text("Status: ").color(NamedTextColor.GOLD)
                            .append(Component.text("Claimed").color(NamedTextColor.GREEN)));
                    lore.add(Component.text("Required Time: ").color(NamedTextColor.GOLD)
                            .append(Component.text(formatPlaytime(reward.requiredPlaytime())).color(NamedTextColor.YELLOW)));
                    RewardsGUI.markUnmovable(claimedMeta);
                    claimedMeta.lore(lore);
                    claimedItem.setItemMeta(claimedMeta);
                }
                inventory.setItem(reward.slot(), claimedItem);
            } else if (playtime >= reward.requiredPlaytime()) {
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
                    lore.add(Component.text("Status: ").color(NamedTextColor.GOLD)
                            .append(Component.text("Ready to Claim").color(NamedTextColor.GREEN)));
                    lore.add(Component.text("Required Time: ").color(NamedTextColor.GOLD)
                            .append(Component.text(formatPlaytime(reward.requiredPlaytime())).color(NamedTextColor.YELLOW)));
                    meta.lore(lore);
                    
                    item.setItemMeta(meta);
                }
                inventory.setItem(reward.slot(), item);
            } else {
                ItemStack item = createRewardItem(reward);
                ItemMeta meta = item.getItemMeta();
                if (meta != null) {
                    List<Component> lore = new ArrayList<>();
                    List<Component> existingLore = meta.lore();
                    if (existingLore != null) {
                        lore.addAll(existingLore);
                    }
                    lore.add(Component.empty());
                    lore.add(Component.text("Status: ").color(NamedTextColor.GOLD)
                            .append(Component.text("Not Claimed").color(NamedTextColor.RED)));
                    lore.add(Component.text("Required Time: ").color(NamedTextColor.GOLD)
                            .append(Component.text(formatPlaytime(reward.requiredPlaytime())).color(NamedTextColor.YELLOW)));
                    lore.add(Component.text("Your Time: ").color(NamedTextColor.GOLD)
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
                //plugin.getLogger().info("Slot " + i + " already used by reward, skipping");
                continue;
            }
            if (navigationEnabled && guiSize > 9 && i >= navigationStartSlot) {
                //plugin.getLogger().info("Slot " + i + " reserved for navigation, skipping");
                continue;
            }
            //plugin.getLogger().info("Adding placeholder to slot " + i);
            inventory.setItem(i, createPlaceholderItem(fillUnusedSlots));
        }

        if (navigationEnabled && guiSize > 9) {
            //plugin.getLogger().info("Adding navigation buttons");
            int navRowStart = guiSize - 9;
            if (page > 0) {
                //plugin.getLogger().info("Adding previous page button");
                ItemStack prevButton = createNavigationButton(prevPageMaterial, prevPageName, prevPageHeadId, NAV_PREV);
                inventory.setItem(navRowStart + 3, prevButton);
            } else {
                //plugin.getLogger().info("Adding disabled previous page button");
                inventory.setItem(navRowStart + 3, createPlaceholderItem(Material.GRAY_STAINED_GLASS_PANE));
            }

            // Check if this is the last page with rewards
            int totalPages = getTotalPages();
            if (page < totalPages - 1) {
                //plugin.getLogger().info("Adding next page button");
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

            //plugin.getLogger().info("Adding close button");
            ItemStack closeButton = createNavigationButton(closeMaterial, closeName, closeHeadId, NAV_CLOSE);
            inventory.setItem(navRowStart + 8, closeButton);
        }

        //plugin.getLogger().info("Finished creating rewards GUI");
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
        StringBuilder sb = new StringBuilder();
        if (days > 0) {
            sb.append(days).append("d ");
        }
        if (hours > 0) {
            sb.append(hours).append("h ");
        }
        if (minutes > 0) {
            sb.append(minutes).append("m");
        }
        if (sb.isEmpty()) {
            sb.append("0m");
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
        if (config.isConfigurationSection("gui")) {
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
                navigationEnabled = config.getBoolean("gui.navigation.enabled", true);
                if (config.isConfigurationSection("gui.navigation.prev-page")) {
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
    }
}
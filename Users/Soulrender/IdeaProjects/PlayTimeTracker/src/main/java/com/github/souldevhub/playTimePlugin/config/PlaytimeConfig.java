import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class PlaytimeConfig {

    // Mark fields as final
    private final NamespacedKey rewardIdKey;
    private final NamespacedKey navButtonTypeKey;
    private final NamespacedKey staticItemKey;

    public PlaytimeConfig(JavaPlugin plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
        rewardIdKey = new NamespacedKey(plugin, "reward_id");
        navButtonTypeKey = new NamespacedKey(plugin, "nav_button_type");
        staticItemKey = new NamespacedKey(plugin, "static_item");
        loadGuiConfig();
    }


    public Inventory getRewardsGUIForPlayer(long playtime, Set<String> claimed, int page) {

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
                ItemStack prevButton = new NavigationButton(prevPageMaterial, prevPageName, prevPageHeadId).createItem();
                ItemMeta prevMeta = prevButton.getItemMeta();
                if (prevMeta != null) {
                    prevMeta.getPersistentDataContainer().set(navButtonTypeKey, PersistentDataType.STRING, NAV_PREV);
                    prevMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS);
                    prevButton.setItemMeta(prevMeta);
                }
                inventory.setItem(navRowStart + 3, prevButton);
            } else {
                inventory.setItem(navRowStart + 3, createPlaceholderItem(Material.GRAY_STAINED_GLASS_PANE));
            }

            ItemStack nextButton = new NavigationButton(nextPageMaterial, nextPageName, nextPageHeadId).createItem();
            ItemMeta nextMeta = nextButton.getItemMeta();
            if (nextMeta != null) {
                nextMeta.getPersistentDataContainer().set(navButtonTypeKey, PersistentDataType.STRING, NAV_NEXT);
                nextMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS);
                nextButton.setItemMeta(nextMeta);
            }
            inventory.setItem(navRowStart + 5, nextButton);

            ItemStack closeButton = new NavigationButton(closeMaterial, closeName, closeHeadId).createItem();
            ItemMeta closeMeta = closeButton.getItemMeta();
            if (closeMeta != null) {
                closeMeta.getPersistentDataContainer().set(navButtonTypeKey, PersistentDataType.STRING, NAV_CLOSE);
                closeMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS);
                closeButton.setItemMeta(closeMeta);
            }
            inventory.setItem(navRowStart + 8, closeButton);
        }

        return inventory;
    }

    // Helper method to create placeholder item
    private ItemStack createPlaceholderItem(Material material) {
        ItemStack placeholder = new ItemStack(material);
        ItemMeta placeholderMeta = placeholder.getItemMeta();
        if (placeholderMeta != null) {
            placeholderMeta.displayName(Component.text(" "));
            markUnmovable(placeholderMeta); // Use the existing markUnmovable method from RewardsGUI
            placeholder.setItemMeta(placeholderMeta);
        }
        return placeholder;
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


    // Remove the unused private method
    // private void markUnmovable(ItemMeta meta) { ... }

}
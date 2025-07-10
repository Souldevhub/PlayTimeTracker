package com.github.souldevhub.playTimePlugin;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PlayTimeConfig {

    private final static PlayTimeConfig instance = new PlayTimeConfig();

    private File file;
    private YamlConfiguration config;

    private List<RewardSlot> rewardSlots = new ArrayList<>();

    public List<RewardSlot> getRewardSlots() {
        return rewardSlots;
    }

    public static PlayTimeConfig getInstance() {
        return instance;
    }

    public void loadConfig(JavaPlugin plugin) {
        if (file == null) {
            file = new File(plugin.getDataFolder(), "config.yml");
        }
        config = YamlConfiguration.loadConfiguration(file);

        List<RewardSlot> loadedSlots = new ArrayList<>();
        List<?> rewards = config.getList("rewards");
        if (rewards != null) {
            for (Object obj : rewards) {
                if (obj instanceof Map<?, ?> map) {
                    String id = map.get("id") != null ? map.get("id").toString() : "";
                    String name = map.get("name") != null ? map.get("name").toString() : "";
                    String matName = map.get("material") != null ? map.get("material").toString() : "";
                    int slot = map.get("slot") instanceof Number n ? n.intValue() : 0;
                    @SuppressWarnings("unchecked")
                    List<String> lore = map.get("lore") instanceof List<?> l ? (List<String>) l : new ArrayList<>();
                    @SuppressWarnings("unchecked")
                    List<String> commands = map.get("commands") instanceof List<?> l ? (List<String>) l : new ArrayList<>();
                    long requiredPlaytime = map.get("requiredPlaytime") instanceof Number n ? n.longValue() : 0L;
                    if (!id.isEmpty() && !matName.isEmpty()) {
                        try {
                            Material mat = Material.valueOf(matName.toUpperCase());
                            loadedSlots.add(new RewardSlot(id, name, mat, slot, lore, commands, requiredPlaytime));
                        } catch (IllegalArgumentException ignored) {
                        }
                    }
                }
            }
        }
        this.rewardSlots = loadedSlots;
    }

    public Inventory getRewardsGUI() {
        int maxSlot = 26;
        for (RewardSlot reward : rewardSlots) {
            if (reward.getSlot() > maxSlot) maxSlot = reward.getSlot();
        }
        int size = ((maxSlot / 9) + 1) * 9;
        Inventory gui = Bukkit.createInventory(null, size, Component.text("Rewards"));
        for (RewardSlot reward : rewardSlots) {
            ItemStack item = new ItemStack(reward.getMaterial());
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                List<Component> loreComponents = new ArrayList<>();
                for (String line : reward.getLore()) {
                    loreComponents.add(LegacyComponentSerializer.legacyAmpersand().deserialize(line));
                }
                meta.lore(loreComponents);
                // Set display name with color support
                if (!reward.getName().isEmpty()) {
                    meta.displayName(LegacyComponentSerializer.legacyAmpersand().deserialize(reward.getName()));
                }
                item.setItemMeta(meta);
            }
            int slot = reward.getSlot();
            if (slot >= 0 && slot < gui.getSize()) {
                gui.setItem(slot, item);
            }
        }
        return gui;
    }

    public Inventory getRewardsGUIForPlayer(String playerName, long playtime, List<String> claimedIds) {
        int maxSlot = 26;
        for (RewardSlot reward : rewardSlots) {
            if (reward.getSlot() > maxSlot) maxSlot = reward.getSlot();
        }
        int size = ((maxSlot / 9) + 1) * 9;
        Inventory gui = Bukkit.createInventory(null, size, Component.text("Rewards"));
        for (RewardSlot reward : rewardSlots) {
            boolean claimed = claimedIds.contains(reward.getId());
            boolean enoughPlaytime = playtime >= reward.getRequiredPlaytime();
            // Only use BARRIER if claimed, otherwise always show original material
            Material displayMaterial = claimed ? Material.BARRIER : reward.getMaterial();

            List<Component> loreComponents = new ArrayList<>();
            // Description (multi-line, colored)
            for (String line : reward.getLore()) {
                loreComponents.add(LegacyComponentSerializer.legacyAmpersand().deserialize(line));
            }
            loreComponents.add(Component.empty());
            loreComponents.add(Component.text("Requirements:", NamedTextColor.YELLOW));
            if (reward.getRequiredPlaytime() > 0) {
                long hours = reward.getRequiredPlaytime() / 3600;
                long minutes = (reward.getRequiredPlaytime() % 3600) / 60;
                loreComponents.add(Component.text("  Playtime: ", NamedTextColor.GRAY)
                        .append(Component.text("%dh %dm".formatted(hours, minutes), NamedTextColor.AQUA)));
            }
            loreComponents.add(Component.text("Claimed: ", NamedTextColor.GRAY)
                    .append(Component.text(claimed ? "Yes" : "No", claimed ? NamedTextColor.GREEN : NamedTextColor.RED)));
            if (!claimed && !enoughPlaytime) {
                loreComponents.add(Component.text("Not enough playtime", NamedTextColor.RED));
            } else if (!claimed) {
                loreComponents.add(Component.text("Click to claim!", NamedTextColor.GREEN));
            }

            ItemStack item = new ItemStack(displayMaterial);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                Component displayName = reward.getName().isEmpty()
                    ? Component.text(reward.getMaterial().name())
                    : LegacyComponentSerializer.legacyAmpersand().deserialize(reward.getName());
                meta.displayName(displayName);
                meta.lore(loreComponents);
                item.setItemMeta(meta);
            }
            int slot = reward.getSlot();
            if (slot >= 0 && slot < gui.getSize()) {
                gui.setItem(slot, item);
            }
        }
        return gui;
    }
}

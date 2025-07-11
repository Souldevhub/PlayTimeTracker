package com.github.souldevhub.playTimePlugin;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PlayTimeConfig {

    private final static PlayTimeConfig instance = new PlayTimeConfig();

    private List<RewardSlot> rewardSlots = new ArrayList<>();

    public List<RewardSlot> getRewardSlots() {
        return rewardSlots;
    }

    public static PlayTimeConfig getInstance() {
        return instance;
    }

    public void loadConfig(JavaPlugin plugin) {
        File file = new File(plugin.getDataFolder(), "config.yml");
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

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

    public Inventory getRewardsGUIForPlayer(long playtime, List<String> claimedIds) {
        int maxSlot = 26;
        for (RewardSlot reward : rewardSlots) {
            if (reward.slot() > maxSlot) maxSlot = reward.slot();
        }
        int size = ((maxSlot / 9) + 1) * 9;
        Inventory gui = Bukkit.createInventory(null, size, Component.text("Rewards"));
        for (RewardSlot reward : rewardSlots) {
            boolean claimed = claimedIds.contains(reward.id());
            boolean enoughPlaytime = playtime >= reward.requiredPlaytime();
            // Only use BARRIER if claimed, otherwise always show original material
            Material displayMaterial = claimed ? Material.BARRIER : reward.material();

            List<Component> loreComponents = new ArrayList<>();
            // Description (multi-line, colored)
            for (String line : reward.lore()) {
                loreComponents.add(LegacyComponentSerializer.legacyAmpersand().deserialize(line));
            }
            loreComponents.add(Component.empty());
            loreComponents.add(Component.text("Requirements:", NamedTextColor.YELLOW));
            if (reward.requiredPlaytime() > 0) {
                long hours = reward.requiredPlaytime() / 3600;
                long minutes = (reward.requiredPlaytime() % 3600) / 60;
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
                Component displayName = reward.name().isEmpty()
                    ? Component.text(reward.material().name())
                    : LegacyComponentSerializer.legacyAmpersand().deserialize(reward.name());
                meta.displayName(displayName);
                meta.lore(loreComponents);
                item.setItemMeta(meta);
            }
            int slot = reward.slot();
            if (slot >= 0 && slot < gui.getSize()) {
                gui.setItem(slot, item);
            }
        }
        return gui;
    }
}

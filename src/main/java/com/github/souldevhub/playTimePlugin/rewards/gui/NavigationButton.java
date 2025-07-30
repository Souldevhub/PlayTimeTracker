package com.github.souldevhub.playTimePlugin.rewards.gui;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;


public class NavigationButton {
    private final Material material;
    private final String name;
    private final String headId;
    private static final Logger logger = Logger.getLogger(NavigationButton.class.getName());


    public NavigationButton(Material material, String name, String headId) {
        this.material = material;
        this.name = name;
        this.headId = headId;
    }


    public ItemStack createItem() {
        ItemStack item;

        // Check if headId is provided and not empty, then create a custom head item
        if (headId != null && !headId.isEmpty() && material == Material.PLAYER_HEAD) {
            item = createCustomHead(headId);
        } else {
            item = new ItemStack(material);
        }

        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(LegacyComponentSerializer.legacyAmpersand().deserialize(name));
            meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ATTRIBUTES);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createCustomHead(String texture) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        if (texture == null || texture.isEmpty()) return head;

        try {
            SkullMeta meta = (SkullMeta) head.getItemMeta();
            PlayerProfile profile = Bukkit.createProfile(UUID.randomUUID());
            profile.setProperty(new ProfileProperty("textures", texture));
            meta.setPlayerProfile(profile);
            meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ATTRIBUTES);
            head.setItemMeta(meta);
        } catch (Exception e) {
            logger.log(Level.WARNING, "Failed to create custom head with texture: " + texture, e);
            ItemStack fallback = new ItemStack(Material.TORCH);
            ItemMeta meta = fallback.getItemMeta();
            if (meta != null) {
                meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ATTRIBUTES);
                fallback.setItemMeta(meta);
            }
            return fallback;
        }
        return head;
    }
}
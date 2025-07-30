package com.github.souldevhub.playTimePlugin.listeners;

import com.github.souldevhub.playTimePlugin.playtime.PlaytimeTracker;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.UUID;

public class ActivityListener implements Listener {
    
    private final PlaytimeTracker playtimeTracker;
    
    public ActivityListener(PlaytimeTracker playtimeTracker) {
        this.playtimeTracker = playtimeTracker;
    }
    
    @EventHandler
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        recordInteraction(event.getPlayer().getUniqueId());
    }
    
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        recordInteraction(event.getPlayer().getUniqueId());
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        recordInteraction(event.getWhoClicked().getUniqueId());
    }
    
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        recordInteraction(event.getPlayer().getUniqueId());
    }
    
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        recordInteraction(event.getPlayer().getUniqueId());
    }
    
    /**
     * Records a player interaction
     * @param uuid Player UUID
     */
    private void recordInteraction(UUID uuid) {
        playtimeTracker.recordInteraction(uuid);
    }
}
package com.github.souldevhub.playTimePlugin.listeners;

import com.github.souldevhub.playTimePlugin.playtime.PlaytimeTracker;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import java.util.UUID;

public class PlaytimeListener implements Listener {

    private final PlaytimeTracker tracker;

    public PlaytimeListener(PlaytimeTracker tracker) {
        this.tracker = tracker;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        UUID uuid = e.getPlayer().getUniqueId();
        tracker.onPlayerJoin(uuid);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        UUID uuid = e.getPlayer().getUniqueId();
        tracker.onPlayerQuit(uuid);
    }

}
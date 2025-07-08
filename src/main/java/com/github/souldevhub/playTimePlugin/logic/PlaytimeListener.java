package com.github.souldevhub.playTimePlugin.logic;


import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class PlaytimeListener implements Listener, CommandExecutor {

    private final PlaytimeTracker tracker;

    public PlaytimeListener(PlaytimeTracker tracker) {
        this.tracker = tracker;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e){
        UUID uuid = e.getPlayer().getUniqueId();
        tracker.onPlayerJoin(uuid);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        UUID uuid = e.getPlayer().getUniqueId();
        tracker.onPlayerQuit(uuid);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        UUID uuid = player.getUniqueId();
        long totalSeconds = tracker.getTotalPlaytime(uuid);
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;


        MiniMessage mm = MiniMessage.miniMessage();
        player.sendMessage(mm.deserialize("<aqua>Your playtime is: <gold>" + hours + " hours <aqua>and <gold>" + minutes + " minutes"));


        player.sendMessage(
                Component.text("Opening playtime menu...", NamedTextColor.GRAY)
        );

        return true;
    }
}

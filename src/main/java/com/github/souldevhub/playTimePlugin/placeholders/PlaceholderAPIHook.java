package com.github.souldevhub.playTimePlugin.placeholders;

import com.github.souldevhub.playTimePlugin.logic.DataHandler;
import com.github.souldevhub.playTimePlugin.logic.PlaytimeTracker;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class PlaceholderAPIHook extends PlaceholderExpansion {

    private final DataHandler dataHandler;
    private final PlaytimeTracker playtimeTracker;

    public PlaceholderAPIHook(DataHandler dataHandler, PlaytimeTracker playtimeTracker) {
        this.dataHandler = dataHandler;
        this.playtimeTracker = playtimeTracker;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "playtime"; // Placeholder prefix
    }

    @Override
    public @NotNull String getAuthor() {
        return "souldevhub"; // Your name or plugin author
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0.0"; // Version of your expansion
    }

    @Override
    public boolean canRegister() {
        return this.getRequiredPlugin() == null || Bukkit.getPluginManager().getPlugin(this.getRequiredPlugin()) != null;
    }


    @Override
    public boolean persist() {
        return true; // Keep loaded across reloads
    }

    @Override
    public @Nullable String onRequest(OfflinePlayer player, @NotNull String params) {
        if (player == null || !player.isOnline()) return "";

        UUID uuid = player.getUniqueId();

        long saved = dataHandler.getSavedPlaytime(uuid);
        long current = playtimeTracker.getCurrentSessionPlaytime(uuid);
        long overall = saved + current;

        return switch (params.toLowerCase()) {
            case "seconds" -> String.valueOf(overall % 60);
            case "minutes" -> String.valueOf((overall / 60) % 60);
            case "hours" -> String.valueOf((overall / 3600) % 24);
            case "days" -> String.valueOf(overall / 86400);
            case "formatted", "overall_formatted" -> formatPlaytime(overall);
            case "saved" -> String.valueOf(saved);
            case "current" -> String.valueOf(current);
            case "overall" -> String.valueOf(overall);
            default -> "";
        };
    }

    private String formatPlaytime(long totalSeconds) {
        long days = totalSeconds / 86400;
        long hours = (totalSeconds % 86400) / 3600;
        long minutes = (totalSeconds % 3600) / 60;

        return String.format("%dd %02dh %02dm", days, hours, minutes);
    }

}

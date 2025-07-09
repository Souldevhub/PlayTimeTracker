package com.github.souldevhub.playTimePlugin;

import com.github.souldevhub.playTimePlugin.logic.DataHandler;
import com.github.souldevhub.playTimePlugin.logic.PlaytimeListener;
import com.github.souldevhub.playTimePlugin.logic.PlaytimeTracker;

import com.github.souldevhub.playTimePlugin.placeholders.PlaceholderAPIHook;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class PlayTimePlugin extends JavaPlugin {
    private DataHandler dataHandler;
    private PlaytimeTracker playtimeTracker;

    @Override
    public void onEnable() {
        // Plugin startup logic
        saveDefaultConfig();
        this.dataHandler = new DataHandler(this);
        this.playtimeTracker = new PlaytimeTracker(this, dataHandler);
        playtimeTracker.startTrackingTask();

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new PlaceholderAPIHook(dataHandler, playtimeTracker).register();
            getLogger().info("Hooked into PlaceholderAPI!");
        } else {
            getLogger().warning("PlaceholderAPI not found. Placeholders will not work.");
        }


        PlaytimeListener listener = new PlaytimeListener(playtimeTracker);
        getServer().getPluginManager().registerEvents(listener, this);

        PluginCommand command = getCommand("playtime");
        if (command != null) {
            command.setExecutor(listener);
        } else {
            getLogger().severe("Command 'playtime' is not defined in plugin.yml!");
        }


        getLogger().info("Playtime Tracker loaded!");

    }

    @Override
    public void onDisable() {
        if (dataHandler != null) {
            // Make sure all session times are added to saved playtime before saving
            if (playtimeTracker != null) {
                playtimeTracker.flushAllSessionTimes();
            }
            dataHandler.saveAll();
        }
        getLogger().info("Playtime Tracker shut down.");
    }
}

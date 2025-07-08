package com.github.souldevhub.playTimePlugin;

import com.github.souldevhub.playTimePlugin.logic.DataHandler;
import com.github.souldevhub.playTimePlugin.logic.PlaytimeListener;
import com.github.souldevhub.playTimePlugin.logic.PlaytimeTracker;

import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class PlayTimePlugin extends JavaPlugin {
    private DataHandler dataHandler;

    @Override
    public void onEnable() {
        // Plugin startup logic
        saveDefaultConfig();
        dataHandler = new DataHandler(this);

        PlaytimeTracker playtimeTracker = new PlaytimeTracker(this, dataHandler);
        playtimeTracker.startTrackingTask();

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
            dataHandler.saveAll();
        }
        getLogger().info("Playtime Tracker shut down.");
    }
}

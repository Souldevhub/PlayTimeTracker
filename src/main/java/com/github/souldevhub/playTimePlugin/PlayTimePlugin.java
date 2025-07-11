package com.github.souldevhub.playTimePlugin;


import com.github.souldevhub.playTimePlugin.logic.*;

import com.github.souldevhub.playTimePlugin.placeholders.PlaceholderAPIHook;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;


public final class PlayTimePlugin extends JavaPlugin {
    private DataHandler dataHandler;
    private PlaytimeTracker playtimeTracker;
    private ClaimedRewardsHandler claimedRewardsHandler;

    @Override
    public void onEnable() {
        // Plugin startup logic
        saveDefaultConfig();
        PlayTimeConfig.getInstance().loadConfig(this);

        this.dataHandler = new DataHandler(this);
        this.playtimeTracker = new PlaytimeTracker(this, dataHandler);
        this.claimedRewardsHandler = new ClaimedRewardsHandler(this);
        playtimeTracker.startTrackingTask();

        // Register RewardsGUIListener with dependencies
        getServer().getPluginManager().registerEvents(
            new RewardsGUIListener(playtimeTracker, claimedRewardsHandler), this
        );


        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new PlaceholderAPIHook(dataHandler, playtimeTracker).register();
            getLogger().info("Hooked into PlaceholderAPI!");
        } else {
            getLogger().warning("PlaceholderAPI not found. Placeholders will not work.");
        }


        PlaytimeListener listener = new PlaytimeListener(playtimeTracker, this);
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

    public ClaimedRewardsHandler getClaimedRewardsHandler() {
        return claimedRewardsHandler;
    }
}

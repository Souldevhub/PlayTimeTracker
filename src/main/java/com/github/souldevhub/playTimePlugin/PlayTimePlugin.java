package com.github.souldevhub.playTimePlugin;


import com.github.souldevhub.playTimePlugin.logic.*;

import com.github.souldevhub.playTimePlugin.placeholders.PlaceholderAPIHook;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;


public final class PlayTimePlugin extends JavaPlugin {
    private DataHandler dataHandler;
    private PlaytimeTracker playtimeTracker;
    private ClaimedRewardsHandler claimedRewardsHandler;

    @Override
    public void onEnable() {
        int pluginId = 26638;
        new Metrics(this, pluginId);
        // Plugin startup logic
        saveDefaultConfig();

        // Load configuration and pre-validate sounds
        Bukkit.getScheduler().runTask(this, () -> {
            getLogger().info("Loading reward configuration...");
            PlayTimeConfig.getInstance(this).loadConfig(this);

            int rewardCount = PlayTimeConfig.getInstance(this).getRewardSlots().size();
            getLogger().info("Loaded " + rewardCount + " reward slots from configuration");
        });
        // Initialize RewardsGUI
        RewardsGUI.init(this);

        this.dataHandler = new DataHandler(this);
        this.playtimeTracker = new PlaytimeTracker(this, dataHandler);
        this.claimedRewardsHandler = new ClaimedRewardsHandler(this);
        playtimeTracker.startTrackingTask();

        // Register RewardsGUIListener with dependencies
        getServer().getPluginManager().registerEvents(
            new RewardsGUIListener(playtimeTracker, claimedRewardsHandler, this), this
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
            command.setTabCompleter(listener);
        } else {
            getLogger().severe("Command 'playtime' is not defined in plugin.yml!");
        }


        getLogger().info("PlayTimePulse loaded!");

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
        getLogger().info("PlayTimePulse shut down.");
    }

    public ClaimedRewardsHandler getClaimedRewardsHandler() {
        return claimedRewardsHandler;
    }

    public DataHandler getDataHandler() {
        return dataHandler;
    }
}

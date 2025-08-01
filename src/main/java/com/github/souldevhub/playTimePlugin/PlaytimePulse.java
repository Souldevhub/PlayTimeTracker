package com.github.souldevhub.playTimePlugin;

import com.github.souldevhub.playTimePlugin.data.DataHandler;
import com.github.souldevhub.playTimePlugin.playtime.PlaytimeTracker;
import com.github.souldevhub.playTimePlugin.placeholders.PlaceholderAPIHook;
import com.github.souldevhub.playTimePlugin.listeners.RewardsGUIListener;
import com.github.souldevhub.playTimePlugin.config.PlaytimeConfig;
import com.github.souldevhub.playTimePlugin.data.ClaimedRewardsDataHandler;
import com.github.souldevhub.playTimePlugin.commands.PlaytimeCommand;
import com.github.souldevhub.playTimePlugin.listeners.PlaytimeListener;
import com.github.souldevhub.playTimePlugin.listeners.ActivityListener;
import com.github.souldevhub.playTimePlugin.rewards.gui.RewardsGUI;
import org.bukkit.plugin.java.JavaPlugin;
import org.bstats.bukkit.Metrics;

public final class PlaytimePulse extends JavaPlugin {
    private DataHandler dataHandler;
    private PlaytimeTracker playtimeTracker;
    private RewardsGUIListener rewardsGUIListener;
    private ClaimedRewardsDataHandler claimedRewardsDataHandler;
    private PlaytimeConfig playtimeConfig;
    private boolean debugMode = false;

    @Override
    public void onEnable() {
        // Initialize bStats metrics
        int pluginId = 26638;
        new Metrics(this, pluginId);
        
        // Save default config
        saveDefaultConfig();
        
        // Load debug mode setting
        debugMode = getConfig().getBoolean("debug", false);
        if (debugMode) {
            getLogger().info("Debug mode enabled");
        }

        // Initialize core components
        dataHandler = new DataHandler(this);
        playtimeTracker = new PlaytimeTracker(this, dataHandler);
        rewardsGUIListener = new RewardsGUIListener(this);
        claimedRewardsDataHandler = new ClaimedRewardsDataHandler(this);
        playtimeConfig = PlaytimeConfig.getInstance(this);

        // Initialize RewardsGUI
        RewardsGUI.init(this);

        // Start tracking task
        playtimeTracker.startTrackingTask();

        // Register event listeners
        getServer().getPluginManager().registerEvents(rewardsGUIListener, this);
        getServer().getPluginManager().registerEvents(new PlaytimeListener(playtimeTracker), this);
        getServer().getPluginManager().registerEvents(new ActivityListener(playtimeTracker), this);

        // Register commands
        PlaytimeCommand playtimeCommand = new PlaytimeCommand(playtimeTracker, this);
        getCommand("playtime").setExecutor(playtimeCommand);
        getCommand("playtime").setTabCompleter(playtimeCommand);

        // Hook into PlaceholderAPI if available
        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new PlaceholderAPIHook(dataHandler, playtimeTracker, playtimeConfig, claimedRewardsDataHandler, this).register();
            getLogger().info("Hooked into PlaceholderAPI!");
        } else {
            getLogger().warning("PlaceholderAPI not found. Placeholders will not work.");
        }
        
        getLogger().info("PlayTimePulse v" + getDescription().getVersion() + " loaded!");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        if (debugMode) {
            getLogger().info("PlaytimePulse plugin disabled!");
        }
    }
    
    public DataHandler getDataHandler() {
        return dataHandler;
    }
    
    public PlaytimeTracker getPlaytimeTracker() {
        return playtimeTracker;
    }
    
    public RewardsGUIListener getRewardsGUIListener() {
        return rewardsGUIListener;
    }
    
    public ClaimedRewardsDataHandler getClaimedRewardsDataHandler() {
        return claimedRewardsDataHandler;
    }
    
    public PlaytimeConfig getPlaytimeConfig() {
        return playtimeConfig;
    }
    
    public boolean isDebugMode() {
        return debugMode;
    }
    
    public void setDebugMode(boolean debugMode) {
        this.debugMode = debugMode;
        getConfig().set("debug", debugMode);
        saveConfig();
    }
}
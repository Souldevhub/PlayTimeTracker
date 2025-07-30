package com.github.souldevhub.playTimePlugin;

import com.github.souldevhub.playTimePlugin.commands.PlaytimeCommand;
import com.github.souldevhub.playTimePlugin.config.PlaytimeConfig;
import com.github.souldevhub.playTimePlugin.data.DataHandler;
import com.github.souldevhub.playTimePlugin.listeners.ActivityListener;
import com.github.souldevhub.playTimePlugin.listeners.PlaytimeListener;
import com.github.souldevhub.playTimePlugin.listeners.RewardsGUIListener;
import com.github.souldevhub.playTimePlugin.placeholders.PlaceholderAPIHook;
import com.github.souldevhub.playTimePlugin.playtime.PlaytimeTracker;
import com.github.souldevhub.playTimePlugin.rewards.gui.RewardsGUI;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;


public final class PlaytimePulse extends JavaPlugin {
    private DataHandler dataHandler;
    private PlaytimeTracker playtimeTracker;
    private RewardsGUIListener rewardsGUIListener;
    private PlaytimeConfig playtimeConfig;

    @Override
    public void onEnable() {
        int pluginId = 26638;
        new Metrics(this, pluginId);
        // Plugin startup logic
        saveDefaultConfig();

        // Initialize RewardsGUI first
        RewardsGUI.init(this);

        this.dataHandler = new DataHandler(this);
        this.playtimeTracker = new PlaytimeTracker(this, dataHandler);
        this.rewardsGUIListener = new RewardsGUIListener(this);
        
        // Configure AFK protection from config
        configureAFKProtection();
        
        playtimeTracker.startTrackingTask();

        // Load configuration and pre-validate sounds
        Bukkit.getScheduler().runTask(this, () -> {
            var logger = getLogger();
            logger.info("Loading reward configuration...");
            playtimeConfig = PlaytimeConfig.getInstance(this);
            playtimeConfig.loadConfig(this);

            int rewardCount = playtimeConfig.getRewardSlots().size();
            logger.info("Loaded " + rewardCount + " reward slots from configuration");
        });

        // Register RewardsGUIListener with dependencies
        getServer().getPluginManager().registerEvents(rewardsGUIListener, this);


        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            PlaceholderAPIHook placeholderHook = new PlaceholderAPIHook(dataHandler, playtimeTracker);
            placeholderHook.register();
            getLogger().info("Hooked into PlaceholderAPI!");
        } else {
            getLogger().warning("PlaceholderAPI not found. Placeholders will not work.");
        }


        PlaytimeListener listener = new PlaytimeListener(playtimeTracker);
        getServer().getPluginManager().registerEvents(listener, this);
        
        // Register ActivityListener for AFK protection
        ActivityListener activityListener = new ActivityListener(playtimeTracker);
        getServer().getPluginManager().registerEvents(activityListener, this);

        PluginCommand command = getCommand("playtime");
        if (command != null) {
            PlaytimeCommand playtimeCommand = new PlaytimeCommand(playtimeTracker, this);
            command.setExecutor(playtimeCommand);
            command.setTabCompleter(playtimeCommand);
        } else {
            getLogger().severe("Command 'playtime' is not defined in plugin.yml!");
        }


        getLogger().info("PlayTimePulse loaded!");

    }

    @Override
    public void onDisable() {
        if (dataHandler != null) {
            if (playtimeTracker != null) {
                playtimeTracker.flushAllSessionTimes();
            }
            dataHandler.saveAll();
        }
        getLogger().info("PlayTimePulse shut down.");
    }

    public RewardsGUIListener getRewardsGUIListener() {
        return rewardsGUIListener;
    }

    public DataHandler getDataHandler() {
        return dataHandler;
    }
    
    public PlaytimeTracker getPlaytimeTracker() {
        return playtimeTracker;
    }
    
    /**
     * Configure AFK protection settings from config.yml
     */
    private void configureAFKProtection() {
        FileConfiguration config = getConfig();

        int interactionThreshold = config.getInt("afk-protection.interaction-threshold", 2);
        long timeWindowMinutes = config.getLong("afk-protection.time-window-minutes", 5);
        
        // Apply settings to playtime tracker
        playtimeTracker.configureAFKProtection(interactionThreshold, timeWindowMinutes);
        
        getLogger().info("AFK Protection configured: " + interactionThreshold + " interactions in " + timeWindowMinutes + " minutes");
    }
}
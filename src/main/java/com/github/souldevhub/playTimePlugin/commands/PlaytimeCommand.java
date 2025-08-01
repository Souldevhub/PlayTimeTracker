package com.github.souldevhub.playTimePlugin.commands;

import com.github.souldevhub.playTimePlugin.PlaytimePulse;
import com.github.souldevhub.playTimePlugin.data.DataHandler;
import com.github.souldevhub.playTimePlugin.playtime.PlaytimeTracker;
import com.github.souldevhub.playTimePlugin.rewards.gui.RewardsGUI;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class PlaytimeCommand implements CommandExecutor, TabCompleter {

    private final PlaytimeTracker tracker;
    private final PlaytimePulse plugin;
    private final DataHandler dataHandler;

    public PlaytimeCommand(PlaytimeTracker tracker, PlaytimePulse plugin) {
        this.tracker = tracker;
        this.plugin = plugin;
        this.dataHandler = plugin.getDataHandler();
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String @NotNull [] args) {
        if (args.length == 1) {
            List<String> completions = new ArrayList<>();
            if (sender.hasPermission("playtime.admin")) {
                completions.addAll(Arrays.asList("add", "reset", "reload", "debug"));
            }
            return completions;
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("add") || args[0].equalsIgnoreCase("reset")) {
                return null; // Return null to indicate we want player names
            } else if (args[0].equalsIgnoreCase("debug")) {
                return Arrays.asList("true", "false");
            }
        } else if (args.length == 3 || args.length == 4) {
            if (args[0].equalsIgnoreCase("add")) {
                // Provide time format hints
                List<String> completions = new ArrayList<>();
                if (args.length == 3) {
                    completions.add("<hours>h");
                } else if (args.length == 4) {
                    completions.add("[<minutes>m]");
                }
                return completions;
            }
        }
        return new ArrayList<>(); // Empty list for no completions
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (args.length == 0) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(Component.text("This command can only be used by players!", NamedTextColor.RED));
                return true;
            }
            showPlaytime(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "add" -> {
                if (!sender.hasPermission("playtime.admin")) {
                    sender.sendMessage(Component.text("You don't have permission to use this command!", NamedTextColor.RED));
                    return true;
                }
                handleAddCommand(sender, args);
                return true;
            }
            case "reset" -> {
                if (!sender.hasPermission("playtime.admin")) {
                    sender.sendMessage(Component.text("You don't have permission to use this command!", NamedTextColor.RED));
                    return true;
                }
                handleResetCommand(sender, args);
                return true;
            }
            case "reload" -> {
                if (!sender.hasPermission("playtime.admin")) {
                    sender.sendMessage(Component.text("You don't have permission to use this command!", NamedTextColor.RED));
                    return true;
                }
                handleReloadCommand(sender);
                return true;
            }
            case "debug" -> {
                if (!sender.hasPermission("playtime.admin")) {
                    sender.sendMessage(Component.text("You don't have permission to use this command!", NamedTextColor.RED));
                    return true;
                }
                handleDebugCommand(sender, args);
                return true;
            }
            default -> {
                sender.sendMessage(Component.text("Unknown command. Use /playtime for help.", NamedTextColor.RED));
                return true;
            }
        }
    }

    private void showPlaytime(Player player) {
        UUID uuid = player.getUniqueId();
        long totalSeconds = tracker.getTotalPlaytime(uuid);
        long days = totalSeconds / 86400;
        long hours = (totalSeconds % 86400) / 3600;
        long minutes = (totalSeconds % 3600) / 60;

        MiniMessage mm = MiniMessage.miniMessage();
        player.sendMessage(mm.deserialize("<aqua>Your playtime: <gold>" + days + " days</gold>, <gold>" + hours + " hours</gold> and <gold>" + minutes + " minutes</gold>"));

        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);

        // Open GUI after a short delay (5 ticks = 0.25 seconds)
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            player.sendMessage(Component.text("Opening rewards menu...", NamedTextColor.GRAY));
            RewardsGUI.open(player, tracker, plugin.getRewardsGUIListener());
            player.playSound(player.getLocation(), Sound.BLOCK_CHEST_OPEN, 0.5f, 1.0f);
        }, 5L);
    }

    private void handleAddCommand(@NotNull CommandSender sender, @NotNull String[] args) {
        if (args.length < 3 || args.length > 4) {
            sender.sendMessage(Component.text("Usage: /playtime add <player> <hours>h [<minutes>m]", NamedTextColor.RED));
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(Component.text("Player not found!", NamedTextColor.RED));
            return;
        }

        @NotNull String hoursStr = args[2].toLowerCase().replace("h", "");
        @NotNull String minutesStr = args.length == 4 ? args[3].toLowerCase().replace("m", "") : "0";

        try {
            int hours = Integer.parseInt(hoursStr);
            int minutes = Integer.parseInt(minutesStr);

            if (hours < 0 || minutes < 0 || minutes > 59) {
                sender.sendMessage(Component.text("Invalid time values!", NamedTextColor.RED));
                return;
            }

            long seconds = (hours * 3600L) + (minutes * 60L);
            dataHandler.addPlaytime(target.getUniqueId(), seconds);
            dataHandler.savePlaytime(target.getUniqueId());

            Component successMessage = Component.text()
                    .append(Component.text("Added ", NamedTextColor.GREEN))
                    .append(Component.text(hours + "h " + minutes + "m", NamedTextColor.GOLD))
                    .append(Component.text(" of playtime to ", NamedTextColor.GREEN))
                    .append(Component.text(target.getName(), NamedTextColor.GOLD))
                    .build();

            sender.sendMessage(successMessage);

            if (sender instanceof Player player) {
                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 1.0f);
            }

            // Notify target player
            target.sendMessage(Component.text()
                    .append(Component.text("An admin has added ", NamedTextColor.GREEN))
                    .append(Component.text(hours + "h " + minutes + "m", NamedTextColor.GOLD))
                    .append(Component.text(" to your playtime!", NamedTextColor.GREEN))
                    .build());
            target.playSound(target.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.0f);

        } catch (NumberFormatException e) {
            sender.sendMessage(Component.text("Invalid number format!", NamedTextColor.RED));
        }
    }

    private void handleResetCommand(@NotNull CommandSender sender, @NotNull String[] args) {
        if (args.length != 2) {
            sender.sendMessage(Component.text("Usage: /playtime reset <player>", NamedTextColor.RED));
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(Component.text("Player not found!", NamedTextColor.RED));
            return;
        }

        UUID targetUUID = target.getUniqueId();

        tracker.flushAllSessionTimes(); // Make sure no session time is pending
        dataHandler.addPlaytime(targetUUID, -dataHandler.getPlaytime(targetUUID)); // Set to 0
        dataHandler.savePlaytime(targetUUID);

        plugin.getRewardsGUIListener().resetClaimedRewards(targetUUID);

        Component successMessage = Component.text()
                .append(Component.text("Reset playtime for ", NamedTextColor.GREEN))
                .append(Component.text(target.getName(), NamedTextColor.GOLD))
                .build();

        sender.sendMessage(successMessage);

        if (sender instanceof Player player) {
            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 0.5f, 1.0f);
        }

        target.sendMessage(Component.text("Your playtime has been reset by an admin.", NamedTextColor.RED));
        target.playSound(target.getLocation(), Sound.BLOCK_ANVIL_LAND, 0.5f, 1.0f);
    }
    
    private void handleReloadCommand(@NotNull CommandSender sender) {
        try {
            plugin.reloadConfig();
            plugin.getPlaytimeConfig().loadConfig(plugin);
            
            sender.sendMessage(Component.text("PlayTimePulse configuration reloaded successfully!", NamedTextColor.GREEN));
            
            if (sender instanceof Player player) {
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 0.5f, 1.0f);
            }
        } catch (Exception e) {
            sender.sendMessage(Component.text("Failed to reload configuration: " + e.getMessage(), NamedTextColor.RED));
            plugin.getLogger().severe("Failed to reload configuration: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void handleDebugCommand(@NotNull CommandSender sender, @NotNull String[] args) {
        if (args.length < 2) {
            sender.sendMessage(Component.text("Current debug mode: " + (plugin.isDebugMode() ? "ENABLED" : "DISABLED"), 
                plugin.isDebugMode() ? NamedTextColor.GREEN : NamedTextColor.RED));
            sender.sendMessage(Component.text("Usage: /playtime debug <true|false>", NamedTextColor.GRAY));
            return;
        }
        
        boolean newDebugState;
        try {
            newDebugState = Boolean.parseBoolean(args[1]);
        } catch (IllegalArgumentException e) {
            sender.sendMessage(Component.text("Invalid argument. Use 'true' or 'false'.", NamedTextColor.RED));
            return;
        }
        
        plugin.setDebugMode(newDebugState);
        
        sender.sendMessage(Component.text("Debug mode has been " + (newDebugState ? "ENABLED" : "DISABLED"), 
            newDebugState ? NamedTextColor.GREEN : NamedTextColor.RED));
        
        if (sender instanceof Player player) {
            player.playSound(player.getLocation(), 
                newDebugState ? Sound.BLOCK_NOTE_BLOCK_CHIME : Sound.BLOCK_NOTE_BLOCK_BASS, 0.5f, 1.0f);
        }
    }
}
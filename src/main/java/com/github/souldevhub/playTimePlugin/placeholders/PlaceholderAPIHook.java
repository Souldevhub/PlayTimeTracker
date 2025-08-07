package com.github.souldevhub.playTimePlugin.placeholders;

import com.github.souldevhub.playTimePlugin.data.DataHandler;
import com.github.souldevhub.playTimePlugin.playtime.PlaytimeTracker;
import com.github.souldevhub.playTimePlugin.config.PlaytimeConfig;
import com.github.souldevhub.playTimePlugin.data.ClaimedRewardsDataHandler;
import com.github.souldevhub.playTimePlugin.rewards.RewardSlot;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class PlaceholderAPIHook extends PlaceholderExpansion {

    private final DataHandler dataHandler;
    private final PlaytimeTracker playtimeTracker;

    private final PlaytimeConfig playtimeConfig;
    private final ClaimedRewardsDataHandler claimedRewardsDataHandler;
    private final JavaPlugin plugin;

    public PlaceholderAPIHook(DataHandler dataHandler, PlaytimeTracker playtimeTracker) {
        this.dataHandler = dataHandler;
        this.playtimeTracker = playtimeTracker;
        this.playtimeConfig = null;
        this.claimedRewardsDataHandler = null;
        this.plugin = null;
    }

    /*
     * Enhanced constructor that accepts additional dependencies
     * This is the constructor that should be used for full functionality
     */
    public PlaceholderAPIHook(DataHandler dataHandler, PlaytimeTracker playtimeTracker, 
                             PlaytimeConfig playtimeConfig, 
                             ClaimedRewardsDataHandler claimedRewardsDataHandler,
                             JavaPlugin plugin) {
        this.dataHandler = dataHandler;
        this.playtimeTracker = playtimeTracker;
        this.playtimeConfig = playtimeConfig;
        this.claimedRewardsDataHandler = claimedRewardsDataHandler;
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "playtime";
    }

    @Override
    public @NotNull String getAuthor() {
        return "souldevhub";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0.0";
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

        // Handle the new placeholders with reward-specific parameters
        if (params.toLowerCase().startsWith("required_")) {
            return handleRequiredPlaceholder(params, uuid, overall);
        }

        // Handle new time_left placeholder
        if (params.toLowerCase().startsWith("time_left")) {
            return handleTimeLeftPlaceholder(params, uuid, overall);
        }

        // Handle claimable rewards placeholder
        if (params.equalsIgnoreCase("claimable_rewards")) {
            return getClaimableRewardsCount(uuid, overall);
        }
        
        // Handle leaderboard placeholders
        if (params.toLowerCase().startsWith("top_")) {
            return getLeaderboardPosition(params, uuid, overall);
        }

        return switch (params.toLowerCase()) {
            case "seconds" -> String.valueOf(overall % 60);
            case "minutes" -> String.valueOf((overall / 60) % 60);
            case "hours" -> String.valueOf((overall / 3600) % 24);
            case "days" -> String.valueOf(overall / 86400);
            case "formatted" -> formatPlaytime(current);           // Only current session time formatted
            case "overall_formatted" -> formatPlaytime(overall); // Overall time formatted
            case "saved" -> String.valueOf(saved);
            case "current" -> String.valueOf(current);
            case "overall" -> String.valueOf(overall);
            // New placeholders
            case "required" -> getNextRewardRequiredTime(uuid, overall);
            case "time_left" -> getTimeLeftForNextReward(uuid, overall);
            case "claimable_rewards" -> getClaimableRewardsCount(uuid, overall);
            case "next_reward_name" -> getNextRewardName(uuid, overall); // New placeholder
            case "leaderboard_position" -> getPlayerLeaderboardPosition(uuid, overall); // New placeholder
            default -> {
                if (params.toLowerCase().startsWith("required_status_")) {
                    // %playtime_required_status_rewardId% - Shows status for specific reward
                    String rewardId = params.substring("required_status_".length());
                    yield getRewardStatus(rewardId, uuid, overall);
                }
                if (params.toLowerCase().startsWith("time_left_")) {
                    // %playtime_time_left_rewardId% - Shows time left for specific reward
                    String rewardId = params.substring("time_left_".length());
                    yield getTimeLeftForReward(rewardId, uuid, overall);
                }
                yield "";
            }
        };
    }

    private @Nullable String handleRequiredPlaceholder(@NotNull String params, UUID uuid, long overall) {
        // %playtime_required% - Shows time required for next unclaimed reward
        if (params.equalsIgnoreCase("required")) {
            return getNextRewardRequiredTime(uuid, overall);
        }
        
        // %playtime_required_status_rewardId% - Shows status for specific reward
        if (params.toLowerCase().startsWith("required_status_")) {
            String rewardId = params.substring("required_status_".length());
            return getRewardStatus(rewardId, uuid, overall);
        }
        
        return "";
    }

    private @Nullable String handleTimeLeftPlaceholder(@NotNull String params, UUID uuid, long overall) {
        // %playtime_time_left% - Shows time left for next reward
        if (params.equalsIgnoreCase("time_left")) {
            return getTimeLeftForNextReward(uuid, overall);
        }
        
        // %playtime_time_left_rewardId% - Shows time left for specific reward
        if (params.toLowerCase().startsWith("time_left_")) {
            String rewardId = params.substring("time_left_".length());
            return getTimeLeftForReward(rewardId, uuid, overall);
        }
        
        return "";
    }

    /*
     * Fully implemented methods for the new placeholders
     */
     
    private String getPlayerLeaderboardPosition(UUID uuid, long overall) {
        if (dataHandler == null) {
            return "N/A";
        }
        
        try {
            // Get all players and their playtime
            Map<UUID, Long> allPlaytimes = dataHandler.getAllPlaytimes();
            
            // Add current session times for online players
            for (UUID playerId : allPlaytimes.keySet()) {
                long sessionTime = playtimeTracker.getCurrentSessionPlaytime(playerId);
                allPlaytimes.put(playerId, allPlaytimes.get(playerId) + sessionTime);
            }
            
            // Sort players by playtime in descending order
            List<Map.Entry<UUID, Long>> sortedPlayers = allPlaytimes.entrySet()
                    .stream()
                    .sorted(Map.Entry.<UUID, Long>comparingByValue().reversed())
                    .collect(Collectors.toList());
            
            // Find the position of the requesting player
            for (int i = 0; i < sortedPlayers.size(); i++) {
                if (sortedPlayers.get(i).getKey().equals(uuid)) {
                    return String.valueOf(i + 1);
                }
            }
            
            // Player not found in leaderboard
            return "Not ranked";
            
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
     
    private String getLeaderboardPosition(String params, UUID uuid, long overall) {
        if (dataHandler == null) {
            return "N/A";
        }
        
        try {
            // Extract the position number from params like "top_1", "top_2", etc.
            String positionStr = params.substring("top_".length());
            int position = Integer.parseInt(positionStr);
            
            // Check if position is within valid range (1-10)
            if (position < 1 || position > 10) {
                return "Position out of range (1-10)";
            }
            
            // Get all players and their playtime
            Map<UUID, Long> allPlaytimes = dataHandler.getAllPlaytimes();
            
            // Add current session times for online players
            for (UUID playerId : allPlaytimes.keySet()) {
                long sessionTime = playtimeTracker.getCurrentSessionPlaytime(playerId);
                allPlaytimes.put(playerId, allPlaytimes.get(playerId) + sessionTime);
            }
            
            // Sort players by playtime in descending order and get the top players
            List<Map.Entry<UUID, Long>> sortedPlayers = allPlaytimes.entrySet()
                    .stream()
                    .sorted(Map.Entry.<UUID, Long>comparingByValue().reversed())
                    .limit(10)
                    .collect(Collectors.toList());
            
            // Check if the requested position exists
            if (position > sortedPlayers.size()) {
                return "No player at position " + position;
            }
            
            // Get the player at the requested position
            Map.Entry<UUID, Long> playerEntry = sortedPlayers.get(position - 1);
            UUID topPlayerUUID = playerEntry.getKey();
            long playtime = playerEntry.getValue();
            
            // Try to get the player's name
            OfflinePlayer topPlayer = plugin.getServer().getOfflinePlayer(topPlayerUUID);
            String playerName = topPlayer.getName();
            if (playerName == null) {
                playerName = topPlayerUUID.toString(); // Fallback to UUID if name is not available
            }
            
            // Format and return the result
            return playerName + " (" + formatPlaytime(playtime) + ")";
            
        } catch (NumberFormatException e) {
            return "Invalid position number";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
     
    private String getNextRewardName(UUID uuid, long overall) {
        if (playtimeConfig == null || claimedRewardsDataHandler == null) {
            return "N/A";
        }
        
        List<RewardSlot> rewardSlots = playtimeConfig.getRewardSlots();
        List<String> claimedRewards = claimedRewardsDataHandler.getClaimedRewards(uuid);
        
        RewardSlot nextReward = null;
        long minRequiredTime = Long.MAX_VALUE;
        boolean foundUnclaimed = false;
        
        // Find the unclaimed reward with the minimum required time that is greater than player's current time
        for (RewardSlot reward : rewardSlots) {
            // Skip if reward is already claimed
            if (claimedRewards.contains(reward.id())) {
                continue;
            }
            
            // Find the reward with the lowest required time that is greater than current playtime
            if (reward.requiredPlaytime() > overall && reward.requiredPlaytime() < minRequiredTime) {
                minRequiredTime = reward.requiredPlaytime();
                nextReward = reward;
                foundUnclaimed = true;
            }
        }
        
        // If we didn't find a reward that requires more time, look for the highest reward they've qualified for but not claimed
        if (!foundUnclaimed) {
            for (RewardSlot reward : rewardSlots) {
                // Skip if reward is already claimed
                if (claimedRewards.contains(reward.id())) {
                    continue;
                }
                
                // Find the reward with the highest required time that the player has qualified for
                if (reward.requiredPlaytime() <= overall && reward.requiredPlaytime() > minRequiredTime) {
                    minRequiredTime = reward.requiredPlaytime();
                    nextReward = reward;
                    foundUnclaimed = true;
                }
            }
        }
        
        if (!foundUnclaimed) {
            return "All rewards claimed";
        }
        
        if (nextReward == null) {
            return "No rewards available";
        }
        
        // Return the name of the next reward, or the ID if name is not set
        return nextReward.name() != null && !nextReward.name().isEmpty() ? nextReward.name() : nextReward.id();
    }

    private String getNextRewardRequiredTime(UUID uuid, long overall) {
        if (playtimeConfig == null || claimedRewardsDataHandler == null) {
            return "N/A";
        }
        
        List<RewardSlot> rewardSlots = playtimeConfig.getRewardSlots();
        List<String> claimedRewards = claimedRewardsDataHandler.getClaimedRewards(uuid);
        
        long minRequiredTime = Long.MAX_VALUE;
        boolean foundUnclaimed = false;
        
        // Find the unclaimed reward with the minimum required time that is greater than player's current time
        // This makes more sense as the "next" reward rather than just the lowest unclaimed reward
        for (RewardSlot reward : rewardSlots) {
            // Skip if reward is already claimed
            if (claimedRewards.contains(reward.id())) {
                continue;
            }
            
            // Only consider rewards that require more time than the player currently has
            // This ensures we're getting the "next" reward, not just the lowest one
            if (reward.requiredPlaytime() > overall && reward.requiredPlaytime() < minRequiredTime) {
                minRequiredTime = reward.requiredPlaytime();
                foundUnclaimed = true;
            }
        }
        
        // If we didn't find a reward that requires more time, look for the highest reward they've qualified for but not claimed
        if (!foundUnclaimed) {
            for (RewardSlot reward : rewardSlots) {
                // Skip if reward is already claimed
                if (claimedRewards.contains(reward.id())) {
                    continue;
                }
                
                // Find the reward with the highest required time that the player has qualified for
                if (reward.requiredPlaytime() <= overall && reward.requiredPlaytime() > minRequiredTime) {
                    minRequiredTime = reward.requiredPlaytime();
                    foundUnclaimed = true;
                }
            }
        }
        
        if (!foundUnclaimed) {
            return "All rewards claimed";
        }
        
        if (minRequiredTime == Long.MAX_VALUE) {
            return "No rewards available";
        }
        
        return formatPlaytime(minRequiredTime);
    }

    private String getRewardStatus(String rewardId, UUID uuid, long overall) {
        if (playtimeConfig == null || claimedRewardsDataHandler == null) {
            return "N/A";
        }
        
        List<RewardSlot> rewardSlots = playtimeConfig.getRewardSlots();
        List<String> claimedRewards = claimedRewardsDataHandler.getClaimedRewards(uuid);
        
        // Find the reward with the given ID
        RewardSlot targetReward = null;
        for (RewardSlot reward : rewardSlots) {
            if (reward.id().equalsIgnoreCase(rewardId)) {
                targetReward = reward;
                break;
            }
        }
        
        if (targetReward == null) {
            return "Reward not found";
        }
        
        // Check if reward is already claimed
        if (claimedRewards.contains(targetReward.id())) {
            return "Claimed";
        }
        
        // Check if reward is ready to claim
        if (overall >= targetReward.requiredPlaytime()) {
            return "Ready to Claim";
        } else {
            return "Not Claimed";
        }
    }

    private String getTimeLeftForNextReward(UUID uuid, long overall) {
        if (playtimeConfig == null || claimedRewardsDataHandler == null) {
            return "N/A";
        }
        
        List<RewardSlot> rewardSlots = playtimeConfig.getRewardSlots();
        List<String> claimedRewards = claimedRewardsDataHandler.getClaimedRewards(uuid);
        
        long minTimeLeft = Long.MAX_VALUE;
        boolean foundUnclaimed = false;
        
        for (RewardSlot reward : rewardSlots) {
            // Skip if reward is already claimed
            if (claimedRewards.contains(reward.id())) {
                continue;
            }
            
            foundUnclaimed = true;
            long timeLeft = reward.requiredPlaytime() - overall;
            // Find the minimum time left among unclaimed rewards
            if (timeLeft >= 0 && timeLeft < minTimeLeft) {
                minTimeLeft = timeLeft;
            }
        }
        
        if (!foundUnclaimed) {
            return "All rewards claimed";
        }
        
        if (minTimeLeft == Long.MAX_VALUE) {
            return "No rewards available";
        }
        
        // If time left is negative, it means the reward is ready to claim
        if (minTimeLeft <= 0) {
            return "Ready to claim";
        }
        
        return formatPlaytime(minTimeLeft);
    }

    private String getTimeLeftForReward(String rewardId, UUID uuid, long overall) {
        if (playtimeConfig == null || claimedRewardsDataHandler == null) {
            return "N/A";
        }
        
        List<RewardSlot> rewardSlots = playtimeConfig.getRewardSlots();
        List<String> claimedRewards = claimedRewardsDataHandler.getClaimedRewards(uuid);
        
        // Find the reward with the given ID
        RewardSlot targetReward = null;
        for (RewardSlot reward : rewardSlots) {
            if (reward.id().equalsIgnoreCase(rewardId)) {
                targetReward = reward;
                break;
            }
        }
        
        if (targetReward == null) {
            return playtimeConfig.getRewardNotFoundText();
        }
        
        // Check if reward is already claimed
        if (claimedRewards.contains(targetReward.id())) {
            return playtimeConfig.getAlreadyClaimedText();
        }
        
        long timeLeft = targetReward.requiredPlaytime() - overall;
        
        // If time left is negative, it means the reward is ready to claim
        if (timeLeft <= 0) {
            return playtimeConfig.getReadyToClaimStatusText();
        }
        
        return formatPlaytime(timeLeft);
    }

    private String getClaimableRewardsCount(UUID uuid, long overall) {
        if (playtimeConfig == null || claimedRewardsDataHandler == null) {
            return "N/A";
        }
        
        List<RewardSlot> rewardSlots = playtimeConfig.getRewardSlots();
        List<String> claimedRewards = claimedRewardsDataHandler.getClaimedRewards(uuid);
        
        int claimableCount = 0;
        
        for (RewardSlot reward : rewardSlots) {
            // Skip if reward is already claimed
            if (claimedRewards.contains(reward.id())) {
                continue;
            }
            
            // Check if the player has enough playtime for this reward
            if (overall >= reward.requiredPlaytime()) {
                claimableCount++;
            }
        }
        
        return String.valueOf(claimableCount);
    }

    private String formatPlaytime(long totalSeconds) {
        long days = totalSeconds / 86400;
        long hours = (totalSeconds % 86400) / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;

        StringBuilder sb = new StringBuilder();
        if (days > 0) {
            sb.append(days).append("d ");
        }
        if (hours > 0) {
            sb.append(hours).append("h ");
        }
        if (minutes > 0) {
            sb.append(minutes).append("m ");
        }
        if (seconds > 0 || sb.length() == 0) {
            sb.append(seconds).append("s");
        }
        
        return sb.toString().trim();
    }

    private String getOverallTimeLeft(UUID uuid, long overall) {
        if (playtimeConfig == null || claimedRewardsDataHandler == null) {
            return "N/A";
        }
        
        List<RewardSlot> rewardSlots = playtimeConfig.getRewardSlots();
        List<String> claimedRewards = claimedRewardsDataHandler.getClaimedRewards(uuid);
        
        long minTimeLeft = Long.MAX_VALUE;
        boolean hasUnclaimedRewards = false;
        
        for (RewardSlot reward : rewardSlots) {
            // Skip if already claimed
            if (claimedRewards.contains(reward.id())) {
                continue;
            }
            
            hasUnclaimedRewards = true;
            long timeLeft = reward.requiredPlaytime() - overall;
            if (timeLeft < minTimeLeft) {
                minTimeLeft = timeLeft;
            }
        }
        
        if (!hasUnclaimedRewards) {
            return playtimeConfig.getNoRewardsAvailableText();
        }
        
        // If time left is negative, it means the reward is ready to claim
        if (minTimeLeft <= 0) {
            return playtimeConfig.getReadyToClaimStatusText();
        }
        
        return formatPlaytime(minTimeLeft);
    }
}
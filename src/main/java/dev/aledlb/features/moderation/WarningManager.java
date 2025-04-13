package dev.aledlb.features.moderation;

import dev.aledlb.utilities.Logger;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Manages player warnings with configurable thresholds and actions.
 */
public class WarningManager {
    private final JavaPlugin plugin;
    private final Map<UUID, List<Warning>> playerWarnings;
    private final Map<Integer, WarningAction> thresholdActions;
    private final int maxWarnings;

    /**
     * Creates a new WarningManager instance.
     * @param plugin The plugin instance.
     */
    public WarningManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.playerWarnings = new HashMap<>();
        this.thresholdActions = new HashMap<>();
        this.maxWarnings = plugin.getConfig().getInt("warnings.max-warnings", 5);
        
        loadThresholdActions();
    }

    /**
     * Loads warning threshold actions from the configuration.
     */
    private void loadThresholdActions() {
        FileConfiguration config = plugin.getConfig();
        ConfigurationSection thresholdsSection = config.getConfigurationSection("warnings.thresholds");
        
        if (thresholdsSection == null) {
            // Default thresholds if none are configured
            thresholdActions.put(1, new WarningAction("kick", "You have been kicked for reaching 1 warning."));
            thresholdActions.put(3, new WarningAction("tempban", "You have been temporarily banned for 1 hour for reaching 3 warnings."));
            thresholdActions.put(5, new WarningAction("ban", "You have been permanently banned for reaching 5 warnings."));
            return;
        }

        for (String key : thresholdsSection.getKeys(false)) {
            try {
                int threshold = Integer.parseInt(key);
                ConfigurationSection actionSection = thresholdsSection.getConfigurationSection(key);
                
                if (actionSection != null) {
                    String actionType = actionSection.getString("action", "none");
                    String message = actionSection.getString("message", "You have reached " + threshold + " warnings.");
                    long duration = actionSection.getLong("duration", 0);
                    
                    thresholdActions.put(threshold, new WarningAction(actionType, message, duration));
                }
            } catch (NumberFormatException e) {
                Logger.warning("Invalid warning threshold: " + key);
            }
        }
    }

    /**
     * Adds a warning to a player.
     * @param target The player to warn.
     * @param staff The staff member who issued the warning.
     * @param reason The reason for the warning.
     * @return The new warning count for the player.
     */
    public int addWarning(OfflinePlayer target, Player staff, String reason) {
        if (target == null || target.getName() == null) {
            return -1;
        }

        UUID targetUuid = target.getUniqueId();
        String targetName = target.getName();
        
        // Get or create warning list
        List<Warning> warnings = playerWarnings.computeIfAbsent(targetUuid, k -> new ArrayList<>());
        
        // Create new warning
        Warning warning = new Warning(targetName, staff.getName(), reason, System.currentTimeMillis());
        warnings.add(warning);
        
        // Check if we've reached a threshold
        int warningCount = warnings.size();
        checkThreshold(target, warningCount);
        
        // Notify staff
        staff.sendMessage(ChatColor.GREEN + "Warning added to " + targetName + ". Total warnings: " + warningCount);
        
        // Notify player if online
        if (target.isOnline()) {
            Player onlinePlayer = target.getPlayer();
            if (onlinePlayer != null) {
                onlinePlayer.sendMessage(ChatColor.RED + "You have received a warning from " + staff.getName() + ".");
                onlinePlayer.sendMessage(ChatColor.RED + "Reason: " + reason);
                onlinePlayer.sendMessage(ChatColor.RED + "Total warnings: " + warningCount + "/" + maxWarnings);
            }
        }
        
        return warningCount;
    }

    /**
     * Checks if a player has reached a warning threshold and applies the appropriate action.
     * @param target The player to check.
     * @param warningCount The player's current warning count.
     */
    private void checkThreshold(OfflinePlayer target, int warningCount) {
        // Find the highest threshold that has been reached
        int highestThreshold = 0;
        for (int threshold : thresholdActions.keySet()) {
            if (warningCount >= threshold && threshold > highestThreshold) {
                highestThreshold = threshold;
            }
        }
        
        // Apply the action if a threshold was reached
        if (highestThreshold > 0) {
            WarningAction action = thresholdActions.get(highestThreshold);
            applyWarningAction(target, action);
        }
    }

    /**
     * Applies a warning action to a player.
     * @param target The player to apply the action to.
     * @param action The action to apply.
     */
    private void applyWarningAction(OfflinePlayer target, WarningAction action) {
        if (target == null || target.getName() == null) {
            return;
        }

        String targetName = target.getName();
        
        switch (action.getType().toLowerCase()) {
            case "kick":
                if (target.isOnline()) {
                    Player onlinePlayer = target.getPlayer();
                    if (onlinePlayer != null) {
                        onlinePlayer.kickPlayer(action.getMessage());
                    }
                }
                break;
                
            case "tempban":
                // This would require the TempBanManager to be passed to this class
                // For now, we'll just log it
                Logger.info("Player " + targetName + " should be tempbanned for " + action.getDuration() + " minutes.");
                break;
                
            case "ban":
                Bukkit.getBanList(org.bukkit.BanList.Type.NAME).addBan(
                    targetName,
                    action.getMessage(),
                    null,
                    "WarningSystem"
                );
                
                if (target.isOnline()) {
                    Player onlinePlayer = target.getPlayer();
                    if (onlinePlayer != null) {
                        onlinePlayer.kickPlayer(action.getMessage());
                    }
                }
                break;
                
            default:
                Logger.info("Unknown warning action: " + action.getType());
                break;
        }
    }

    /**
     * Gets the warning count for a player.
     * @param target The player to check.
     * @return The player's warning count.
     */
    public int getWarningCount(OfflinePlayer target) {
        if (target == null || target.getName() == null) {
            return -1;
        }

        List<Warning> warnings = playerWarnings.get(target.getUniqueId());
        return warnings != null ? warnings.size() : 0;
    }

    /**
     * Gets all warnings for a player.
     * @param target The player to check.
     * @return A list of the player's warnings.
     */
    public List<Warning> getWarnings(OfflinePlayer target) {
        if (target == null || target.getName() == null) {
            return new ArrayList<>();
        }

        return playerWarnings.getOrDefault(target.getUniqueId(), new ArrayList<>());
    }

    /**
     * Removes a warning from a player.
     * @param target The player to remove the warning from.
     * @param index The index of the warning to remove.
     * @return true if the warning was removed, false otherwise.
     */
    public boolean removeWarning(OfflinePlayer target, int index) {
        if (target == null || target.getName() == null) {
            return false;
        }

        List<Warning> warnings = playerWarnings.get(target.getUniqueId());
        if (warnings == null || index < 0 || index >= warnings.size()) {
            return false;
        }

        warnings.remove(index);
        return true;
    }

    /**
     * Clears all warnings for a player.
     * @param target The player to clear warnings for.
     * @return true if the warnings were cleared, false otherwise.
     */
    public boolean clearWarnings(OfflinePlayer target) {
        if (target == null || target.getName() == null) {
            return false;
        }

        playerWarnings.remove(target.getUniqueId());
        return true;
    }

    /**
     * Represents a warning issued to a player.
     */
    public static class Warning {
        private final String playerName;
        private final String staffName;
        private final String reason;
        private final long timestamp;

        /**
         * Creates a new Warning instance.
         * @param playerName The name of the player who received the warning.
         * @param staffName The name of the staff member who issued the warning.
         * @param reason The reason for the warning.
         * @param timestamp The timestamp when the warning was issued.
         */
        public Warning(String playerName, String staffName, String reason, long timestamp) {
            this.playerName = playerName;
            this.staffName = staffName;
            this.reason = reason;
            this.timestamp = timestamp;
        }

        /**
         * Gets the name of the player who received the warning.
         * @return The player's name.
         */
        public String getPlayerName() {
            return playerName;
        }

        /**
         * Gets the name of the staff member who issued the warning.
         * @return The staff member's name.
         */
        public String getStaffName() {
            return staffName;
        }

        /**
         * Gets the reason for the warning.
         * @return The warning reason.
         */
        public String getReason() {
            return reason;
        }

        /**
         * Gets the timestamp when the warning was issued.
         * @return The warning timestamp.
         */
        public long getTimestamp() {
            return timestamp;
        }
    }

    /**
     * Represents an action to take when a warning threshold is reached.
     */
    public static class WarningAction {
        private final String type;
        private final String message;
        private final long duration;

        /**
         * Creates a new WarningAction instance.
         * @param type The type of action (kick, tempban, ban).
         * @param message The message to display to the player.
         */
        public WarningAction(String type, String message) {
            this(type, message, 0);
        }

        /**
         * Creates a new WarningAction instance.
         * @param type The type of action (kick, tempban, ban).
         * @param message The message to display to the player.
         * @param duration The duration of the action in minutes (for tempban).
         */
        public WarningAction(String type, String message, long duration) {
            this.type = type;
            this.message = message;
            this.duration = duration;
        }

        /**
         * Gets the type of action.
         * @return The action type.
         */
        public String getType() {
            return type;
        }

        /**
         * Gets the message to display to the player.
         * @return The action message.
         */
        public String getMessage() {
            return message;
        }

        /**
         * Gets the duration of the action in minutes.
         * @return The action duration.
         */
        public long getDuration() {
            return duration;
        }
    }
} 
package dev.aledlb.commands.staff.moderation;

import dev.aledlb.features.moderation.TempBanManager;
import dev.aledlb.utilities.Logger;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * Command executor for the /tempban command.
 * Allows staff to temporarily ban players from the server.
 */
public class TempBanCommand implements CommandExecutor {

    private static final String PERMISSION = "core.tempban";
    private static final String USAGE = ChatColor.RED + "Usage: /tempban <player> <duration> <reason>";
    
    private final TempBanManager tempBanManager;

    /**
     * Creates a new TempBanCommand instance.
     * @param tempBanManager The TempBanManager to use for ban operations.
     */
    public TempBanCommand(TempBanManager tempBanManager) {
        this.tempBanManager = tempBanManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Check permission
        if (!sender.hasPermission(PERMISSION)) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return true;
        }

        // Check arguments
        if (args.length < 3) {
            sender.sendMessage(USAGE);
            return true;
        }

        // Get target player
        String targetName = args[0];
        OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);
        
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Player not found: " + targetName);
            return true;
        }

        // Check if player is already banned
        if (tempBanManager.isTempBanned(target)) {
            sender.sendMessage(ChatColor.RED + "Player " + target.getName() + " is already banned.");
            return true;
        }

        // Parse duration
        String durationStr = args[1];
        long duration;
        try {
            duration = parseDuration(durationStr);
        } catch (IllegalArgumentException e) {
            sender.sendMessage(ChatColor.RED + "Invalid duration format: " + durationStr);
            sender.sendMessage(ChatColor.YELLOW + "Valid formats: 1h, 1d, 1w, 1m");
            return true;
        }

        // Get reason
        StringBuilder reasonBuilder = new StringBuilder();
        for (int i = 2; i < args.length; i++) {
            reasonBuilder.append(args[i]).append(" ");
        }
        String reason = reasonBuilder.toString().trim();

        // Execute ban
        boolean success = tempBanManager.tempBan(target, (Player) sender, duration, reason);
        
        if (success) {
            // Notify sender
            sender.sendMessage(ChatColor.GREEN + "Player " + targetName + " has been banned for " + formatDuration(duration) + ".");
            sender.sendMessage(ChatColor.YELLOW + "Reason: " + reason);
            
            // Log the action
            Logger.info("Player " + targetName + " was banned by " + sender.getName() + " for " + formatDuration(duration) + ". Reason: " + reason);
        } else {
            sender.sendMessage(ChatColor.RED + "Failed to ban player " + targetName + ".");
        }

        return true;
    }

    /**
     * Parses a duration string into milliseconds.
     * @param durationStr The duration string to parse (e.g., "1h", "1d", "1w", "1m").
     * @return The duration in milliseconds.
     * @throws IllegalArgumentException if the duration format is invalid.
     */
    private long parseDuration(String durationStr) {
        if (durationStr == null || durationStr.isEmpty()) {
            throw new IllegalArgumentException("Duration cannot be empty");
        }

        // Extract the number and unit
        String numberStr = durationStr.substring(0, durationStr.length() - 1);
        String unit = durationStr.substring(durationStr.length() - 1).toLowerCase();

        // Parse the number
        int number;
        try {
            number = Integer.parseInt(numberStr);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid number in duration: " + numberStr);
        }

        // Convert to milliseconds
        long milliseconds;
        switch (unit) {
            case "s": // seconds
                milliseconds = number * 1000L;
                break;
            case "m": // minutes
                milliseconds = number * 60 * 1000L;
                break;
            case "h": // hours
                milliseconds = number * 60 * 60 * 1000L;
                break;
            case "d": // days
                milliseconds = number * 24 * 60 * 60 * 1000L;
                break;
            case "w": // weeks
                milliseconds = number * 7 * 24 * 60 * 60 * 1000L;
                break;
            default:
                throw new IllegalArgumentException("Invalid duration unit: " + unit);
        }

        return milliseconds;
    }

    /**
     * Formats a duration in milliseconds to a human-readable string.
     * @param durationMs The duration in milliseconds.
     * @return A formatted duration string.
     */
    private String formatDuration(long durationMs) {
        long seconds = durationMs / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        long weeks = days / 7;
        long months = days / 30;

        if (months > 0) {
            return months + " month" + (months != 1 ? "s" : "");
        } else if (weeks > 0) {
            return weeks + " week" + (weeks != 1 ? "s" : "");
        } else if (days > 0) {
            return days + " day" + (days != 1 ? "s" : "");
        } else if (hours > 0) {
            return hours + " hour" + (hours != 1 ? "s" : "");
        } else if (minutes > 0) {
            return minutes + " minute" + (minutes != 1 ? "s" : "");
        } else {
            return seconds + " second" + (seconds != 1 ? "s" : "");
        }
    }

    /**
     * Formats the expiry time based on the duration.
     * @param durationMs The duration in milliseconds.
     * @return A formatted expiry time string.
     */
    private String formatExpiryTime(long durationMs) {
        long expiryTime = System.currentTimeMillis() + durationMs;
        return new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date(expiryTime));
    }
} 
package dev.aledlb.features.moderation;

import dev.aledlb.utilities.Logger;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Manages temporary bans with automatic unban functionality.
 */
public class TempBanManager {
    private final JavaPlugin plugin;
    private final Map<UUID, BukkitTask> unbanTasks;
    private final Map<UUID, String> banReasons;
    private final Map<UUID, String> banStaff;

    /**
     * Creates a new TempBanManager instance.
     * @param plugin The plugin instance.
     */
    public TempBanManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.unbanTasks = new HashMap<>();
        this.banReasons = new HashMap<>();
        this.banStaff = new HashMap<>();
    }

    /**
     * Temporarily bans a player.
     * @param target The player to ban.
     * @param staff The staff member who issued the ban.
     * @param duration The ban duration in minutes.
     * @param reason The reason for the ban.
     * @return true if the ban was successful, false otherwise.
     */
    public boolean tempBan(OfflinePlayer target, Player staff, long duration, String reason) {
        if (target == null) {
            return false;
        }

        UUID targetUuid = target.getUniqueId();
        String targetName = target.getName();
        
        if (targetName == null) {
            return false;
        }

        // Cancel any existing unban task
        if (unbanTasks.containsKey(targetUuid)) {
            unbanTasks.get(targetUuid).cancel();
            unbanTasks.remove(targetUuid);
        }

        // Calculate ban expiration
        long expirationTime = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(duration);
        Date expirationDate = new Date(expirationTime);

        // Ban the player
        Bukkit.getBanList(BanList.Type.NAME).addBan(
            targetName,
            reason + " (Expires: " + formatDuration(duration) + ")",
            expirationDate,
            staff.getName()
        );

        // Store ban information
        banReasons.put(targetUuid, reason);
        banStaff.put(targetUuid, staff.getName());

        // Schedule unban task
        BukkitTask unbanTask = Bukkit.getScheduler().runTaskLater(plugin, () -> {
            unban(target);
            Logger.info("Player " + targetName + " has been automatically unbanned.");
        }, TimeUnit.MINUTES.toSeconds(duration) * 20); // Convert minutes to ticks

        unbanTasks.put(targetUuid, unbanTask);

        // Kick the player if online
        if (target.isOnline()) {
            Player onlinePlayer = target.getPlayer();
            if (onlinePlayer != null) {
                onlinePlayer.kickPlayer(
                    "§cYou have been temporarily banned!\n" +
                    "§7Reason: §f" + reason + "\n" +
                    "§7Duration: §f" + formatDuration(duration) + "\n" +
                    "§7Expires: §f" + formatDate(expirationDate)
                );
            }
        }

        return true;
    }

    /**
     * Unbans a player.
     * @param target The player to unban.
     * @return true if the unban was successful, false otherwise.
     */
    public boolean unban(OfflinePlayer target) {
        if (target == null || target.getName() == null) {
            return false;
        }

        UUID targetUuid = target.getUniqueId();
        String targetName = target.getName();

        // Remove from ban list
        Bukkit.getBanList(BanList.Type.NAME).pardon(targetName);

        // Cancel unban task if exists
        if (unbanTasks.containsKey(targetUuid)) {
            unbanTasks.get(targetUuid).cancel();
            unbanTasks.remove(targetUuid);
        }

        // Remove stored ban information
        banReasons.remove(targetUuid);
        banStaff.remove(targetUuid);

        return true;
    }

    /**
     * Checks if a player is temporarily banned.
     * @param target The player to check.
     * @return true if the player is temporarily banned, false otherwise.
     */
    public boolean isTempBanned(OfflinePlayer target) {
        if (target == null || target.getName() == null) {
            return false;
        }

        return unbanTasks.containsKey(target.getUniqueId());
    }

    /**
     * Gets the remaining ban duration in minutes.
     * @param target The player to check.
     * @return The remaining ban duration in minutes, or -1 if not banned.
     */
    public long getRemainingBanDuration(OfflinePlayer target) {
        if (target == null || target.getName() == null) {
            return -1;
        }

        UUID targetUuid = target.getUniqueId();
        if (!unbanTasks.containsKey(targetUuid)) {
            return -1;
        }

        BukkitTask task = unbanTasks.get(targetUuid);
        return task.getTaskId() / 20 / 60; // Convert ticks to minutes
    }

    /**
     * Gets the reason for a player's ban.
     * @param target The player to check.
     * @return The ban reason, or null if not banned.
     */
    public String getBanReason(OfflinePlayer target) {
        if (target == null || target.getName() == null) {
            return null;
        }

        return banReasons.get(target.getUniqueId());
    }

    /**
     * Gets the staff member who issued a ban.
     * @param target The player to check.
     * @return The staff member's name, or null if not banned.
     */
    public String getBanStaff(OfflinePlayer target) {
        if (target == null || target.getName() == null) {
            return null;
        }

        return banStaff.get(target.getUniqueId());
    }

    /**
     * Formats a duration in minutes to a human-readable string.
     * @param minutes The duration in minutes.
     * @return A formatted duration string.
     */
    private String formatDuration(long minutes) {
        if (minutes < 60) {
            return minutes + " minute" + (minutes != 1 ? "s" : "");
        } else if (minutes < 1440) { // Less than 24 hours
            long hours = minutes / 60;
            long remainingMinutes = minutes % 60;
            return hours + " hour" + (hours != 1 ? "s" : "") + 
                   (remainingMinutes > 0 ? " and " + remainingMinutes + " minute" + (remainingMinutes != 1 ? "s" : "") : "");
        } else {
            long days = minutes / 1440;
            long remainingHours = (minutes % 1440) / 60;
            return days + " day" + (days != 1 ? "s" : "") + 
                   (remainingHours > 0 ? " and " + remainingHours + " hour" + (remainingHours != 1 ? "s" : "") : "");
        }
    }

    /**
     * Formats a date to a human-readable string.
     * @param date The date to format.
     * @return A formatted date string.
     */
    private String formatDate(Date date) {
        return date.toString();
    }
} 
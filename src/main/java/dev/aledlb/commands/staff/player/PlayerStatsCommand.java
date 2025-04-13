package dev.aledlb.commands.staff.player;

import dev.aledlb.features.player.PlayerData;
import dev.aledlb.features.player.PlayerDataManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

/**
 * Handles the /playerstats command which displays player statistics.
 */
public class PlayerStatsCommand implements CommandExecutor {
    private static final String PERMISSION = "core.playerstats";
    private static final String USAGE = "/playerstats [player]";
    private static final String ERROR_NO_PERMISSION = "§cYou don't have permission to use this command.";
    private static final String ERROR_PLAYER_NOT_FOUND = "§cPlayer not found.";
    private static final String HEADER = "§6=== Player Statistics ===";
    private static final String STATS_FORMAT = "§7%s: §f%s";

    private final PlayerDataManager dataManager;
    private final SimpleDateFormat dateFormat;

    /**
     * Creates a new PlayerStatsCommand instance.
     * @param dataManager The player data manager.
     */
    public PlayerStatsCommand(PlayerDataManager dataManager) {
        this.dataManager = dataManager;
        this.dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission(PERMISSION)) {
            sender.sendMessage(ERROR_NO_PERMISSION);
            return true;
        }

        if (args.length == 0) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("§cUsage: " + USAGE);
                return true;
            }
            displayStats(sender, ((Player) sender).getUniqueId());
            return true;
        }

        if (args.length == 1) {
            String targetName = args[0];
            Player target = Bukkit.getPlayer(targetName);
            if (target != null) {
                displayStats(sender, target.getUniqueId());
            } else {
                // Try to find the player by name in the data files
                UUID uuid = findPlayerUUID(targetName);
                if (uuid != null) {
                    displayStats(sender, uuid);
                } else {
                    sender.sendMessage(ERROR_PLAYER_NOT_FOUND);
                }
            }
            return true;
        }

        sender.sendMessage("§cUsage: " + USAGE);
        return true;
    }

    /**
     * Displays player statistics.
     * @param sender The command sender.
     * @param uuid The player's UUID.
     */
    private void displayStats(CommandSender sender, UUID uuid) {
        PlayerData data = dataManager.getPlayerData(uuid);
        String playerName = Bukkit.getOfflinePlayer(uuid).getName();

        sender.sendMessage(HEADER);
        sender.sendMessage(String.format(STATS_FORMAT, "Player", playerName));
        sender.sendMessage(String.format(STATS_FORMAT, "Playtime", formatPlaytime(data.getPlaytime())));
        sender.sendMessage(String.format(STATS_FORMAT, "Deaths", data.getDeaths()));
        sender.sendMessage(String.format(STATS_FORMAT, "Kills", data.getKills()));
        sender.sendMessage(String.format(STATS_FORMAT, "K/D Ratio", calculateKDRatio(data)));
        sender.sendMessage(String.format(STATS_FORMAT, "Last Seen", formatDate(data.getLastSeen())));
        
        if (data.getLastLocation() != null) {
            sender.sendMessage(String.format(STATS_FORMAT, "Last Location", formatLocation(data.getLastLocation())));
        }
    }

    /**
     * Formats playtime into a readable string.
     * @param playtime The playtime in milliseconds.
     * @return The formatted playtime.
     */
    private String formatPlaytime(long playtime) {
        long seconds = playtime / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        if (days > 0) {
            return String.format("%dd %dh %dm", days, hours % 24, minutes % 60);
        } else if (hours > 0) {
            return String.format("%dh %dm %ds", hours, minutes % 60, seconds % 60);
        } else if (minutes > 0) {
            return String.format("%dm %ds", minutes, seconds % 60);
        } else {
            return String.format("%ds", seconds);
        }
    }

    /**
     * Calculates the kill/death ratio.
     * @param data The player data.
     * @return The formatted K/D ratio.
     */
    private String calculateKDRatio(PlayerData data) {
        if (data.getDeaths() == 0) {
            return data.getKills() + ".0";
        }
        return String.format("%.2f", (double) data.getKills() / data.getDeaths());
    }

    /**
     * Formats a timestamp into a readable date string.
     * @param timestamp The timestamp.
     * @return The formatted date.
     */
    private String formatDate(long timestamp) {
        return dateFormat.format(new Date(timestamp));
    }

    /**
     * Formats a location into a readable string.
     * @param location The location.
     * @return The formatted location.
     */
    private String formatLocation(org.bukkit.Location location) {
        return String.format("%.2f, %.2f, %.2f (%s)",
            location.getX(), location.getY(), location.getZ(),
            location.getWorld().getName());
    }

    /**
     * Finds a player's UUID by their name.
     * @param name The player's name.
     * @return The player's UUID, or null if not found.
     */
    private UUID findPlayerUUID(String name) {
        // This is a simplified version. In a real implementation,
        // you would want to use a more robust method to find offline players.
        return Bukkit.getOfflinePlayer(name).getUniqueId();
    }
} 
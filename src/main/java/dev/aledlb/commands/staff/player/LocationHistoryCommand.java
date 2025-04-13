package dev.aledlb.commands.staff.player;

import dev.aledlb.features.player.PlayerData;
import dev.aledlb.features.player.PlayerDataManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Handles the /locationhistory command which manages player location history.
 */
public class LocationHistoryCommand implements CommandExecutor {
    private static final String PERMISSION = "core.locationhistory";
    private static final String USAGE = "/locationhistory <player> [list|teleport] [index]";
    private static final String ERROR_NO_PERMISSION = "§cYou don't have permission to use this command.";
    private static final String ERROR_PLAYER_NOT_FOUND = "§cPlayer not found.";
    private static final String ERROR_USAGE = "§cUsage: " + USAGE;
    private static final String ERROR_NO_HISTORY = "§cNo location history found for this player.";
    private static final String ERROR_INVALID_INDEX = "§cInvalid location index.";
    private static final String SUCCESS_TELEPORT = "§aTeleported to location %d.";
    private static final String HEADER = "§6=== Location History ===";
    private static final String LOCATION_FORMAT = "§7%d: §f%s";

    private final PlayerDataManager dataManager;
    private final SimpleDateFormat dateFormat;

    /**
     * Creates a new LocationHistoryCommand instance.
     * @param dataManager The player data manager.
     */
    public LocationHistoryCommand(PlayerDataManager dataManager) {
        this.dataManager = dataManager;
        this.dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission(PERMISSION)) {
            sender.sendMessage(ERROR_NO_PERMISSION);
            return true;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage("§cThis command can only be used by players.");
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage(ERROR_USAGE);
            return true;
        }

        String targetName = args[0];
        Player target = Bukkit.getPlayer(targetName);
        if (target == null) {
            sender.sendMessage(ERROR_PLAYER_NOT_FOUND);
            return true;
        }

        if (args.length == 1) {
            listLocations(sender, target.getUniqueId());
            return true;
        }

        String action = args[1].toLowerCase();
        switch (action) {
            case "list":
                listLocations(sender, target.getUniqueId());
                break;
            case "teleport":
                if (args.length < 3) {
                    sender.sendMessage(ERROR_USAGE);
                    return true;
                }
                try {
                    int index = Integer.parseInt(args[2]);
                    teleportToLocation((Player) sender, target.getUniqueId(), index);
                } catch (NumberFormatException e) {
                    sender.sendMessage(ERROR_INVALID_INDEX);
                }
                break;
            default:
                sender.sendMessage(ERROR_USAGE);
        }

        return true;
    }

    /**
     * Lists a player's location history.
     * @param sender The command sender.
     * @param uuid The player's UUID.
     */
    private void listLocations(CommandSender sender, UUID uuid) {
        PlayerData data = dataManager.getPlayerData(uuid);
        List<Location> locations = data.getLocationHistory();
        
        if (locations.isEmpty()) {
            sender.sendMessage(ERROR_NO_HISTORY);
            return;
        }

        sender.sendMessage(HEADER);
        for (int i = 0; i < locations.size(); i++) {
            Location loc = locations.get(i);
            sender.sendMessage(String.format(LOCATION_FORMAT, i, formatLocation(loc)));
        }
    }

    /**
     * Teleports a player to a location from history.
     * @param player The player to teleport.
     * @param targetUuid The target player's UUID.
     * @param index The location index.
     */
    private void teleportToLocation(Player player, UUID targetUuid, int index) {
        PlayerData data = dataManager.getPlayerData(targetUuid);
        List<Location> locations = data.getLocationHistory();
        
        if (locations.isEmpty()) {
            player.sendMessage(ERROR_NO_HISTORY);
            return;
        }

        if (index < 0 || index >= locations.size()) {
            player.sendMessage(ERROR_INVALID_INDEX);
            return;
        }

        Location loc = locations.get(index);
        player.teleport(loc);
        player.sendMessage(String.format(SUCCESS_TELEPORT, index));
    }

    /**
     * Formats a location into a readable string.
     * @param location The location.
     * @return The formatted location.
     */
    private String formatLocation(Location location) {
        return String.format("%.2f, %.2f, %.2f (%s)",
            location.getX(), location.getY(), location.getZ(),
            location.getWorld().getName());
    }
} 
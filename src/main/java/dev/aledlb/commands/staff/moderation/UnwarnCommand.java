package dev.aledlb.commands.staff.moderation;

import dev.aledlb.features.moderation.WarningManager;
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
 * Command executor for the /unwarn command.
 * Allows staff to remove warnings from players.
 */
public class UnwarnCommand implements CommandExecutor {

    private static final String PERMISSION = "core.unwarn";
    private static final String USAGE = ChatColor.RED + "Usage: /unwarn <player> [warningId]";
    
    private final WarningManager warningManager;

    /**
     * Creates a new UnwarnCommand instance.
     * @param warningManager The WarningManager to use for unwarn operations.
     */
    public UnwarnCommand(WarningManager warningManager) {
        this.warningManager = warningManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Check permission
        if (!sender.hasPermission(PERMISSION)) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return true;
        }

        // Check arguments
        if (args.length < 1) {
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

        // Check if player has warnings
        int warningCount = warningManager.getWarningCount(target);
        
        if (warningCount <= 0) {
            sender.sendMessage(ChatColor.RED + "Player " + targetName + " has no warnings to remove.");
            return true;
        }

        // If no warning ID is provided, remove the most recent warning
        if (args.length == 1) {
            boolean success = warningManager.removeWarning(target, warningCount - 1);
            
            if (success) {
                // Notify sender
                sender.sendMessage(ChatColor.GREEN + "Removed the most recent warning from " + targetName + ".");
                
                // Notify player if online
                if (target.isOnline()) {
                    Player onlinePlayer = target.getPlayer();
                    if (onlinePlayer != null) {
                        onlinePlayer.sendMessage(ChatColor.GREEN + "Your most recent warning has been removed by " + sender.getName() + ".");
                    }
                }
                
                // Log the action
                Logger.info("Latest warning was removed from " + targetName + " by " + sender.getName());
            } else {
                sender.sendMessage(ChatColor.RED + "Failed to remove warning from " + targetName + ".");
            }
        } else {
            // Try to remove a specific warning by ID
            try {
                int warningId = Integer.parseInt(args[1]);
                boolean success = warningManager.removeWarning(target, warningId);
                
                if (success) {
                    // Notify sender
                    sender.sendMessage(ChatColor.GREEN + "Removed warning #" + warningId + " from " + targetName + ".");
                    
                    // Notify player if online
                    if (target.isOnline()) {
                        Player onlinePlayer = target.getPlayer();
                        if (onlinePlayer != null) {
                            onlinePlayer.sendMessage(ChatColor.GREEN + "Warning #" + warningId + " has been removed by " + sender.getName() + ".");
                        }
                    }
                    
                    // Log the action
                    Logger.info("Warning #" + warningId + " was removed from " + targetName + " by " + sender.getName());
                } else {
                    sender.sendMessage(ChatColor.RED + "Failed to remove warning #" + warningId + " from " + targetName + ".");
                }
            } catch (NumberFormatException e) {
                sender.sendMessage(ChatColor.RED + "Invalid warning ID: " + args[1]);
                sender.sendMessage(ChatColor.YELLOW + "Use /unwarn <player> to remove the most recent warning.");
            }
        }

        return true;
    }
} 
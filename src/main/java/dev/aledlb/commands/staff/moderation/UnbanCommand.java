package dev.aledlb.commands.staff.moderation;

import dev.aledlb.features.moderation.TempBanManager;
import dev.aledlb.utilities.Logger;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.UUID;

/**
 * Command executor for the /unban command.
 * Allows staff to unban players from the server.
 */
public class UnbanCommand implements CommandExecutor {

    private static final String PERMISSION = "core.unban";
    private static final String USAGE = ChatColor.RED + "Usage: /unban <player>";
    
    private final TempBanManager tempBanManager;

    /**
     * Creates a new UnbanCommand instance.
     * @param tempBanManager The TempBanManager to use for unban operations.
     */
    public UnbanCommand(TempBanManager tempBanManager) {
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

        // Check if player is banned
        if (!tempBanManager.isTempBanned(target)) {
            sender.sendMessage(ChatColor.RED + "Player " + targetName + " is not banned.");
            return true;
        }

        // Execute unban
        boolean success = tempBanManager.unban(target);
        
        if (success) {
            // Notify sender
            sender.sendMessage(ChatColor.GREEN + "Player " + targetName + " has been unbanned.");
            
            // Log the action
            Logger.info("Player " + targetName + " was unbanned by " + sender.getName());
        } else {
            sender.sendMessage(ChatColor.RED + "Failed to unban player " + targetName + ".");
        }

        return true;
    }
} 
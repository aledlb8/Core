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
 * Command executor for the /warn command.
 * Allows staff to warn players for rule violations.
 */
public class WarnCommand implements CommandExecutor {

    private static final String PERMISSION = "core.warn";
    private static final String USAGE = ChatColor.RED + "Usage: /warn <player> <reason>";
    
    private final WarningManager warningManager;

    /**
     * Creates a new WarnCommand instance.
     * @param warningManager The WarningManager to use for warning operations.
     */
    public WarnCommand(WarningManager warningManager) {
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
        if (args.length < 2) {
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

        // Get reason
        StringBuilder reasonBuilder = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            reasonBuilder.append(args[i]).append(" ");
        }
        String reason = reasonBuilder.toString().trim();

        // Execute warning
        int warningCount = warningManager.addWarning(target, (Player) sender, reason);
        
        if (warningCount > 0) {
            // Notify sender
            sender.sendMessage(ChatColor.GREEN + "Player " + targetName + " has been warned.");
            sender.sendMessage(ChatColor.YELLOW + "Reason: " + reason);
            sender.sendMessage(ChatColor.YELLOW + "Total warnings: " + warningCount);
            
            // Notify player if online
            if (target.isOnline()) {
                Player onlinePlayer = target.getPlayer();
                if (onlinePlayer != null) {
                    onlinePlayer.sendMessage(ChatColor.RED + "You have been warned by " + sender.getName() + ".");
                    onlinePlayer.sendMessage(ChatColor.YELLOW + "Reason: " + reason);
                }
            }
            
            // Log the action
            Logger.info("Player " + targetName + " was warned by " + sender.getName() + ". Reason: " + reason);
        } else {
            sender.sendMessage(ChatColor.RED + "Failed to warn player " + targetName + ".");
        }

        return true;
    }
} 
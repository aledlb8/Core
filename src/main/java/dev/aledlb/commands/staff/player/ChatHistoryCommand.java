package dev.aledlb.commands.staff.player;

import dev.aledlb.features.player.PlayerData;
import dev.aledlb.features.player.PlayerDataManager;
import dev.aledlb.utilities.Logger;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Command to manage player chat history.
 * Usage: /chathistory <player> [list|clear]
 */
public class ChatHistoryCommand implements CommandExecutor {
    private static final String PERMISSION = "core.chathistory";
    private static final String USAGE = "/chathistory <player> [list|clear]";
    private static final String ERROR_PREFIX = ChatColor.RED + "Error: " + ChatColor.GRAY;
    private static final String SUCCESS_PREFIX = ChatColor.GREEN + "Success: " + ChatColor.GRAY;
    private static final String INFO_PREFIX = ChatColor.YELLOW + "Info: " + ChatColor.GRAY;
    private static final String HEADER = ChatColor.GOLD + "=== Chat History for %s ===";
    
    private final PlayerDataManager dataManager;
    private final SimpleDateFormat dateFormat;

    /**
     * Creates a new ChatHistoryCommand instance.
     * @param dataManager The player data manager.
     */
    public ChatHistoryCommand(PlayerDataManager dataManager) {
        this.dataManager = dataManager;
        this.dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission(PERMISSION)) {
            sender.sendMessage(ERROR_PREFIX + "You don't have permission to use this command.");
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage(ERROR_PREFIX + "Usage: " + USAGE);
            return true;
        }

        String targetName = args[0];
        UUID uuid = null;

        // Try to get UUID from online player first
        Player targetPlayer = Bukkit.getPlayer(targetName);
        if (targetPlayer != null) {
            uuid = targetPlayer.getUniqueId();
        } else {
            // If not online, try to get from offline player
            try {
                uuid = Bukkit.getOfflinePlayer(targetName).getUniqueId();
            } catch (Exception e) {
                sender.sendMessage(ERROR_PREFIX + "Player not found: " + targetName);
                return true;
            }
        }

        PlayerData data = dataManager.getPlayerData(uuid);
        if (data == null) {
            sender.sendMessage(ERROR_PREFIX + "Could not load player data for: " + targetName);
            return true;
        }

        if (args.length == 1 || (args.length == 2 && args[1].equalsIgnoreCase("list"))) {
            listChatHistory(sender, data, targetName);
        } else if (args.length == 2 && args[1].equalsIgnoreCase("clear")) {
            clearChatHistory(sender, data, targetName);
        } else {
            sender.sendMessage(ERROR_PREFIX + "Usage: " + USAGE);
        }

        return true;
    }

    /**
     * Lists the chat history for a player.
     * @param sender The command sender.
     * @param data The player data.
     * @param targetName The target player's name.
     */
    private void listChatHistory(CommandSender sender, PlayerData data, String targetName) {
        List<String> messages = data.getChatHistory();
        
        if (messages.isEmpty()) {
            sender.sendMessage(INFO_PREFIX + "No chat history found for " + targetName);
            return;
        }

        sender.sendMessage(String.format(HEADER, targetName));
        
        for (String message : messages) {
            String[] parts = message.split("\\|", 2);
            if (parts.length == 2) {
                long timestamp = Long.parseLong(parts[0]);
                String content = parts[1];
                String date = dateFormat.format(new Date(timestamp));
                sender.sendMessage(ChatColor.GRAY + "[" + date + "] " + ChatColor.WHITE + content);
            }
        }
    }

    /**
     * Clears the chat history for a player.
     * @param sender The command sender.
     * @param data The player data.
     * @param targetName The target player's name.
     */
    private void clearChatHistory(CommandSender sender, PlayerData data, String targetName) {
        data.clearChatHistory();
        dataManager.savePlayerData(data);
        sender.sendMessage(SUCCESS_PREFIX + "Cleared chat history for " + targetName);
    }
} 
package dev.aledlb.commands.staff.moderation;

import dev.aledlb.features.moderation.ChatFilter;
import dev.aledlb.utilities.Logger;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Command executor for the /chatfilter command.
 * Allows staff to manage chat filter rules.
 */
public class ChatFilterCommand implements CommandExecutor, TabCompleter {

    private static final String PERMISSION = "core.chatfilter";
    private static final String USAGE = ChatColor.RED + "Usage: /chatfilter <add|remove|list|reload> [name] [pattern] [action] [message]";
    
    private static final List<String> VALID_ACTIONS = Arrays.asList("block", "warn", "mute");
    
    private final ChatFilter chatFilter;

    /**
     * Creates a new ChatFilterCommand instance.
     * @param chatFilter The ChatFilter to use for managing filter rules.
     */
    public ChatFilterCommand(ChatFilter chatFilter) {
        this.chatFilter = chatFilter;
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

        String action = args[0].toLowerCase();

        switch (action) {
            case "add":
                handleAdd(sender, args);
                break;
            case "remove":
                handleRemove(sender, args);
                break;
            case "list":
                handleList(sender);
                break;
            case "reload":
                handleReload(sender);
                break;
            default:
                sender.sendMessage(ChatColor.RED + "Invalid action: " + action);
                sender.sendMessage(ChatColor.YELLOW + "Valid actions: add, remove, list, reload");
                break;
        }

        return true;
    }

    /**
     * Handles the add action.
     * @param sender The command sender.
     * @param args The command arguments.
     */
    private void handleAdd(CommandSender sender, String[] args) {
        if (args.length < 5) {
            sender.sendMessage(ChatColor.RED + "Usage: /chatfilter add <name> <pattern> <action> <message>");
            return;
        }

        String name = args[1];
        String pattern = args[2];
        String action = args[3].toLowerCase();
        String message = String.join(" ", Arrays.copyOfRange(args, 4, args.length));

        // Validate action
        if (!VALID_ACTIONS.contains(action)) {
            sender.sendMessage(ChatColor.RED + "Invalid action: " + action);
            sender.sendMessage(ChatColor.YELLOW + "Valid actions: " + String.join(", ", VALID_ACTIONS));
            return;
        }

        // Add the rule
        boolean success = chatFilter.addFilterRule(name, pattern, action, message);

        if (success) {
            sender.sendMessage(ChatColor.GREEN + "Added filter rule '" + name + "' successfully.");
            Logger.info("Filter rule '" + name + "' was added by " + sender.getName());
        } else {
            sender.sendMessage(ChatColor.RED + "Failed to add filter rule '" + name + "'.");
        }
    }

    /**
     * Handles the remove action.
     * @param sender The command sender.
     * @param args The command arguments.
     */
    private void handleRemove(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /chatfilter remove <name>");
            return;
        }

        String name = args[1];

        // Remove the rule
        boolean success = chatFilter.removeFilterRule(name);

        if (success) {
            sender.sendMessage(ChatColor.GREEN + "Removed filter rule '" + name + "' successfully.");
            Logger.info("Filter rule '" + name + "' was removed by " + sender.getName());
        } else {
            sender.sendMessage(ChatColor.RED + "Failed to remove filter rule '" + name + "' or it doesn't exist.");
        }
    }

    /**
     * Handles the list action.
     * @param sender The command sender.
     */
    private void handleList(CommandSender sender) {
        List<String> rules = chatFilter.getFilterRules();
        
        if (rules.isEmpty()) {
            sender.sendMessage(ChatColor.YELLOW + "No filter rules are currently configured.");
            return;
        }

        sender.sendMessage(ChatColor.GREEN + "=== Chat Filter Rules ===");
        for (String rule : rules) {
            sender.sendMessage(rule);
        }
    }

    /**
     * Handles the reload action.
     * @param sender The command sender.
     */
    private void handleReload(CommandSender sender) {
        chatFilter.reload();
        sender.sendMessage(ChatColor.GREEN + "Chat filter rules reloaded successfully.");
        Logger.info("Chat filter rules were reloaded by " + sender.getName());
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission(PERMISSION)) {
            return new ArrayList<>();
        }

        if (args.length == 1) {
            return Arrays.asList("add", "remove", "list", "reload").stream()
                    .filter(action -> action.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("remove")) {
            // This would require access to the list of rule names
            // For simplicity, we'll return an empty list
            return new ArrayList<>();
        }

        if (args.length == 4 && args[0].equalsIgnoreCase("add")) {
            return VALID_ACTIONS.stream()
                    .filter(action -> action.startsWith(args[3].toLowerCase()))
                    .collect(Collectors.toList());
        }

        return new ArrayList<>();
    }
} 
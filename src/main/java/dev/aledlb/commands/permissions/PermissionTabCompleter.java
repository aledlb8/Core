package dev.aledlb.commands.permissions;

import dev.aledlb.Core;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Provides tab completion for permission-related commands.
 * This class handles suggestions for:
 * - Main permission commands
 * - User-specific commands
 * - Group-specific commands
 * - Default permission commands
 */
public class PermissionTabCompleter implements TabCompleter {

    private static final String[] MAIN_COMMANDS = {"user", "group", "listgroups", "reload", "default"};
    private static final String[] USER_COMMANDS = {"add", "remove", "addgroup", "removegroup", "info"};
    private static final String[] GROUP_COMMANDS = {"add", "remove", "prefix", "info", "addmember", "removemember", "family", "addparent", "removeparent"};
    private static final String[] DEFAULT_COMMANDS = {"add", "remove", "info"};

    private final Core plugin;

    public PermissionTabCompleter(Core plugin) {
        this.plugin = plugin;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            // First argument: main commands
            return filterCompletions(MAIN_COMMANDS, args[0]);
        }

        switch (args[0].toLowerCase()) {
            case "user":
                return handleUserCompletions(sender, args);
            case "group":
                return handleGroupCompletions(sender, args);
            case "default":
                return handleDefaultCompletions(sender, args);
            default:
                return completions;
        }
    }

    /**
     * Handles tab completion for user-related commands
     * @param sender The command sender
     * @param args The command arguments
     * @return List of possible completions
     */
    private List<String> handleUserCompletions(CommandSender sender, String[] args) {
        if (args.length == 2) {
            // Second argument: player names
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        } else if (args.length == 3) {
            // Third argument: user commands
            return filterCompletions(USER_COMMANDS, args[2]);
        } else if (args.length == 4) {
            // Fourth argument: depends on the previous command
            switch (args[2].toLowerCase()) {
                case "add":
                case "remove":
                    // Suggest permissions
                    return suggestPermissions(args[3]);
                case "addgroup":
                case "removegroup":
                    // Suggest groups
                    return suggestGroups(args[3]);
                default:
                    return new ArrayList<>();
            }
        }
        return new ArrayList<>();
    }

    /**
     * Handles tab completion for group-related commands
     * @param sender The command sender
     * @param args The command arguments
     * @return List of possible completions
     */
    private List<String> handleGroupCompletions(CommandSender sender, String[] args) {
        if (args.length == 2) {
            // Second argument: group names
            return suggestGroups(args[1]);
        } else if (args.length == 3) {
            // Third argument: group commands
            return filterCompletions(GROUP_COMMANDS, args[2]);
        } else if (args.length == 4) {
            // Fourth argument: depends on the previous command
            switch (args[2].toLowerCase()) {
                case "add":
                case "remove":
                    // Suggest permissions
                    return suggestPermissions(args[3]);
                case "addmember":
                case "removemember":
                    // Suggest players
                    return Bukkit.getOnlinePlayers().stream()
                            .map(Player::getName)
                            .filter(name -> name.toLowerCase().startsWith(args[3].toLowerCase()))
                            .collect(Collectors.toList());
                case "addparent":
                case "removeparent":
                    // Suggest groups (excluding the current group)
                    return suggestGroups(args[3]).stream()
                            .filter(group -> !group.equals(args[1]))
                            .collect(Collectors.toList());
                default:
                    return new ArrayList<>();
            }
        }
        return new ArrayList<>();
    }

    /**
     * Handles tab completion for default permission commands
     * @param sender The command sender
     * @param args The command arguments
     * @return List of possible completions
     */
    private List<String> handleDefaultCompletions(CommandSender sender, String[] args) {
        if (args.length == 2) {
            // Second argument: default commands
            return filterCompletions(DEFAULT_COMMANDS, args[1]);
        } else if (args.length == 3 && !args[1].equalsIgnoreCase("info")) {
            // Third argument: permissions (for add/remove)
            return suggestPermissions(args[2]);
        }
        return new ArrayList<>();
    }

    /**
     * Filters a list of completions based on the current input
     * @param completions The list of possible completions
     * @param input The current input
     * @return Filtered list of completions
     */
    private List<String> filterCompletions(String[] completions, String input) {
        return Arrays.stream(completions)
                .filter(completion -> completion.toLowerCase().startsWith(input.toLowerCase()))
                .collect(Collectors.toList());
    }

    /**
     * Suggests a list of permissions based on the current input
     * @param input The current input
     * @return List of suggested permissions
     */
    private List<String> suggestPermissions(String input) {
        // Common permission patterns
        String[] commonPermissions = {
            "core.",
            "core.admin.",
            "core.user.",
            "core.group.",
            "minecraft.",
            "bukkit.",
            "essentials.",
            "worldedit.",
            "worldguard."
        };

        return Arrays.stream(commonPermissions)
                .filter(permission -> permission.toLowerCase().startsWith(input.toLowerCase()))
                .collect(Collectors.toList());
    }

    /**
     * Suggests a list of groups based on the current input
     * @param input The current input
     * @return List of suggested groups
     */
    private List<String> suggestGroups(String input) {
        if (!plugin.getConfigManager().getConfig().contains("groups")) {
            return new ArrayList<>();
        }

        return plugin.getConfigManager().getConfig().getConfigurationSection("groups").getKeys(false).stream()
                .filter(group -> group.toLowerCase().startsWith(input.toLowerCase()))
                .collect(Collectors.toList());
    }
}
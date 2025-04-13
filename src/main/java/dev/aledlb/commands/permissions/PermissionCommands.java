package dev.aledlb.commands.permissions;

import dev.aledlb.Core;
import dev.aledlb.utilities.Logger;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static dev.aledlb.listeners.PlayerEvent.updateNameTag;

/**
 * Handles all permission-related commands for the Core plugin.
 * This class provides functionality to manage:
 * - User permissions
 * - Group permissions
 * - Default permissions
 * - Permission inheritance
 */
public class PermissionCommands implements CommandExecutor {

    private static final String HEADER = "§3====[§bCore Permissions§3]====";
    private static final String SUCCESS_PREFIX = "§2";
    private static final String ERROR_PREFIX = "§c";
    private static final String INFO_PREFIX = "§3";
    private static final String HIGHLIGHT = "§a";

    private final Core plugin;

    public PermissionCommands(Core plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            usage(sender, "main");
            return true;
        }

        try {
            switch (args[0].toLowerCase()) {
                case "user":
                    return user(sender, args);
                case "group":
                    return group(sender, args);
                case "listgroups":
                    return listGroups(sender);
                case "reload":
                    return reload(sender);
                case "default":
                    return defaultGroup(sender, args);
                default:
                    usage(sender, "main");
        return true;
    }
        } catch (Exception e) {
            Logger.severe("Error executing permission command: " + e.getMessage());
            sender.sendMessage(ERROR_PREFIX + "An error occurred while executing the command. Check the console for details.");
            return true;
        }
    }

    /**
     * Reloads all permissions
     * @param sender The command sender
     * @return true if successful
     */
    private boolean reload(CommandSender sender) {
        sender.sendMessage(HEADER);
        plugin.getPermissionManager().reloadPermissions();
        sender.sendMessage(SUCCESS_PREFIX + "Permissions reloaded successfully.");
        return true;
    }

    /**
     * Handles user-related commands
     * @param sender The command sender
     * @param args The command arguments
     * @return true if successful
     */
    private boolean user(CommandSender sender, String[] args) {
        args = shift(args);
        if (args.length < 1) {
            usage(sender, "user");
            return true;
        }

        // Get the target player
        OfflinePlayer targetPlayer = null;
        try {
            // Try to get player by name first
            targetPlayer = Bukkit.getOfflinePlayer(args[0]);
            
            // If not found, try to parse as UUID
            if (targetPlayer == null || !targetPlayer.hasPlayedBefore()) {
                try {
                    UUID uuid = UUID.fromString(args[0]);
                    targetPlayer = Bukkit.getOfflinePlayer(uuid);
                } catch (IllegalArgumentException e) {
                    // Not a valid UUID, continue with null targetPlayer
                }
            }
            
            if (targetPlayer == null || !targetPlayer.hasPlayedBefore()) {
                sender.sendMessage(ERROR_PREFIX + "Player not found: " + args[0]);
                return true;
            }
        } catch (Exception e) {
            sender.sendMessage(ERROR_PREFIX + "Error finding player: " + e.getMessage());
            return true;
        }

        // Handle info command
        if (args.length == 1 || (args.length > 1 && args[1].equalsIgnoreCase("info"))) {
            return listPermissions(targetPlayer, sender);
        }

        if (args.length < 3) {
            usage(sender, "user");
            return true;
        }

        // Handle other user commands
        switch (args[1].toLowerCase()) {
            case "add":
                return addPermission(targetPlayer, args[2], sender);
            case "remove":
                return removePermission(targetPlayer, args[2], sender);
            case "addgroup":
                return addGroup(targetPlayer, args[2], sender);
            case "removegroup":
                return removeGroup(targetPlayer, args[2], sender);
            default:
                usage(sender, "user");
                return true;
        }
    }

    /**
     * Handles group-related commands
     * @param sender The command sender
     * @param args The command arguments
     * @return true if successful
     */
    private boolean group(CommandSender sender, String[] args) {
        args = shift(args);
        if (args.length < 1) {
            usage(sender, "group");
            return true;
        }

        String groupName = args[0];
        
        // Handle info command
        if (args.length == 1 || (args.length > 1 && args[1].equalsIgnoreCase("info"))) {
            return listGroupPermissions(new String[]{groupName}, sender);
        }
        
        // Handle family command
        if (args.length > 1 && args[1].equalsIgnoreCase("family")) {
            return listFamily(sender, groupName);
        }

        if (args.length < 3) {
            usage(sender, "group");
            return true;
        }

        // Handle other group commands
        switch (args[1].toLowerCase()) {
            case "prefix":
                return prefixGroup(groupName, args[2], sender);
            case "add":
                return addGroupPermission(groupName, args[2], sender);
            case "remove":
                return removeGroupPermission(groupName, args[2], sender);
            case "addparent":
                return addGroupParent(groupName, args[2], sender);
            case "removeparent":
                return removeGroupParent(groupName, args[2], sender);
            case "addmember":
                return addGroupMember(args[2], groupName, sender);
            case "removemember":
                return removeGroupMember(args[2], groupName, sender);
            default:
                usage(sender, "group");
                return true;
        }
    }

    /**
     * Handles default permission commands
     * @param sender The command sender
     * @param args The command arguments
     * @return true if successful
     */
    private boolean defaultGroup(CommandSender sender, String[] args) {
        args = shift(args);
        if (args.length < 1) {
            usage(sender, "default");
            return true;
        }
        
        if (args[0].equalsIgnoreCase("info")) {
            return listDefaultPermissions(sender);
        }
        
        if (args.length < 2) {
            usage(sender, "default");
            return true;
        }
        
        switch (args[0].toLowerCase()) {
            case "add":
                return addPermission(null, args[1], sender);
            case "remove":
                return removePermission(null, args[1], sender);
            default:
                usage(sender, "default");
                return true;
        }
    }

    /**
     * Adds a player to a group
     * @param player The player to add
     * @param groupName The group name
     * @param sender The command sender
     * @return true if successful
     */
    private boolean addGroup(OfflinePlayer player, String groupName, CommandSender sender) {
        sender.sendMessage(HEADER);
        
        // Check if group exists
        if (!plugin.getConfigManager().getConfig().contains("groups." + groupName)) {
            sender.sendMessage(ERROR_PREFIX + "Group " + HIGHLIGHT + groupName + ERROR_PREFIX + " does not exist.");
            return true;
        }
        
        List<String> groups = plugin.getConfigManager().getConfig().getStringList("users." + player.getName() + ".groups");
        if (groups.contains(groupName)) {
            sender.sendMessage(SUCCESS_PREFIX + "Player " + HIGHLIGHT + player.getName() + SUCCESS_PREFIX + " is already in group " + HIGHLIGHT + groupName + SUCCESS_PREFIX + ".");
            return true;
        }
        
        groups.add(groupName);
        plugin.getConfigManager().getConfig().set("users." + player.getName() + ".groups", groups);
        plugin.getConfigManager().saveConfig("config");
        
        sender.sendMessage(SUCCESS_PREFIX + "Added player " + HIGHLIGHT + player.getName() + SUCCESS_PREFIX + " to group " + HIGHLIGHT + groupName + SUCCESS_PREFIX + ".");
        
        // Update permissions for online players
        if (player.isOnline()) {
            plugin.getPermissionManager().addPermissions(player.getPlayer());
        }
        
        return true;
    }

    /**
     * Removes a player from a group
     * @param player The player to remove
     * @param groupName The group name
     * @param sender The command sender
     * @return true if successful
     */
    private boolean removeGroup(OfflinePlayer player, String groupName, CommandSender sender) {
        sender.sendMessage(HEADER);
        
        List<String> groups = plugin.getConfigManager().getConfig().getStringList("users." + player.getName() + ".groups");
        if (!groups.contains(groupName)) {
            sender.sendMessage(SUCCESS_PREFIX + "Player " + HIGHLIGHT + player.getName() + SUCCESS_PREFIX + " is not in group " + HIGHLIGHT + groupName + SUCCESS_PREFIX + ".");
            return true;
        }
        
        groups.remove(groupName);
        plugin.getConfigManager().getConfig().set("users." + player.getName() + ".groups", groups);
        plugin.getConfigManager().saveConfig("config");
        
        sender.sendMessage(SUCCESS_PREFIX + "Removed player " + HIGHLIGHT + player.getName() + SUCCESS_PREFIX + " from group " + HIGHLIGHT + groupName + SUCCESS_PREFIX + ".");
        
        // Update permissions for online players
        if (player.isOnline()) {
            plugin.getPermissionManager().addPermissions(player.getPlayer());
        }
        
        return true;
    }

    /**
     * Adds a parent group to a group
     * @param groupName The group name
     * @param parentName The parent group name
     * @param sender The command sender
     * @return true if successful
     */
    private boolean addGroupParent(String groupName, String parentName, CommandSender sender) {
        sender.sendMessage(HEADER);
        
        // Check if both groups exist
        if (!plugin.getConfigManager().getConfig().contains("groups." + groupName)) {
            sender.sendMessage(ERROR_PREFIX + "Group " + HIGHLIGHT + groupName + ERROR_PREFIX + " does not exist.");
            return true;
        }
        
        if (!plugin.getConfigManager().getConfig().contains("groups." + parentName)) {
            sender.sendMessage(ERROR_PREFIX + "Parent group " + HIGHLIGHT + parentName + ERROR_PREFIX + " does not exist.");
            return true;
        }
        
        // Check for circular inheritance
        if (wouldCreateCircularInheritance(groupName, parentName)) {
            sender.sendMessage(ERROR_PREFIX + "Cannot add " + HIGHLIGHT + parentName + ERROR_PREFIX + " as parent of " + HIGHLIGHT + groupName + ERROR_PREFIX + " as it would create circular inheritance.");
            return true;
        }
        
        List<String> parents = plugin.getConfigManager().getConfig().getStringList("groups." + groupName + ".parents");
        if (parents.contains(parentName)) {
            sender.sendMessage(SUCCESS_PREFIX + "Group " + HIGHLIGHT + groupName + SUCCESS_PREFIX + " already has parent " + HIGHLIGHT + parentName + SUCCESS_PREFIX + ".");
            return true;
        }
        
        parents.add(parentName);
        plugin.getConfigManager().getConfig().set("groups." + groupName + ".parents", parents);
        plugin.getConfigManager().saveConfig("config");
        
        sender.sendMessage(SUCCESS_PREFIX + "Group " + HIGHLIGHT + groupName + SUCCESS_PREFIX + " now has parent " + HIGHLIGHT + parentName + SUCCESS_PREFIX + ".");
        
        // Update permissions for all online players
        plugin.getPermissionManager().reloadPermissionsWithoutSaving();
        
        return true;
    }

    /**
     * Checks if adding a parent would create circular inheritance
     * @param groupName The group name
     * @param parentName The parent group name
     * @return true if circular inheritance would be created
     */
    private boolean wouldCreateCircularInheritance(String groupName, String parentName) {
        // Check if parent is already a child of the group
        List<String> children = new ArrayList<>();
        for (String group : plugin.getConfigManager().getConfig().getConfigurationSection("groups").getKeys(false)) {
            if (plugin.getConfigManager().getConfig().getStringList("groups." + group + ".parents").contains(groupName)) {
                children.add(group);
            }
        }
        
        // Check if any child of the group is the parent or has the parent as a parent
        for (String child : children) {
            if (child.equals(parentName)) {
                return true;
            }
            
            List<String> childParents = plugin.getConfigManager().getConfig().getStringList("groups." + child + ".parents");
            if (childParents.contains(parentName)) {
                return true;
            }
        }
        
        return false;
    }

    /**
     * Removes a parent group from a group
     * @param groupName The group name
     * @param parentName The parent group name
     * @param sender The command sender
     * @return true if successful
     */
    private boolean removeGroupParent(String groupName, String parentName, CommandSender sender) {
        sender.sendMessage(HEADER);
        
        List<String> parents = plugin.getConfigManager().getConfig().getStringList("groups." + groupName + ".parents");
        if (!parents.contains(parentName)) {
            sender.sendMessage(SUCCESS_PREFIX + "Group " + HIGHLIGHT + groupName + SUCCESS_PREFIX + " does not have parent " + HIGHLIGHT + parentName + SUCCESS_PREFIX + ".");
            return true;
        }
        
        parents.remove(parentName);
        plugin.getConfigManager().getConfig().set("groups." + groupName + ".parents", parents);
        plugin.getConfigManager().saveConfig("config");
        
        sender.sendMessage(SUCCESS_PREFIX + "Removed parent " + HIGHLIGHT + parentName + SUCCESS_PREFIX + " from group " + HIGHLIGHT + groupName + SUCCESS_PREFIX + ".");
        
        // Update permissions for all online players
        plugin.getPermissionManager().reloadPermissionsWithoutSaving();
        
        return true;
    }

    /**
     * Adds a permission to a group
     * @param groupName The group name
     * @param permission The permission to add
     * @param sender The command sender
     * @return true if successful
     */
    private boolean addGroupPermission(String groupName, String permission, CommandSender sender) {
        sender.sendMessage(HEADER);
        
        // Check if group exists
        if (!plugin.getConfigManager().getConfig().contains("groups." + groupName)) {
            sender.sendMessage(ERROR_PREFIX + "Group " + HIGHLIGHT + groupName + ERROR_PREFIX + " does not exist.");
            return true;
        }
        
        List<String> permissions = plugin.getConfigManager().getConfig().getStringList("groups." + groupName + ".permissions");
        if (permissions.contains(permission)) {
            sender.sendMessage(SUCCESS_PREFIX + "Group " + HIGHLIGHT + groupName + SUCCESS_PREFIX + " already has permission " + HIGHLIGHT + permission + SUCCESS_PREFIX + ".");
            return true;
        }
        
        permissions.add(permission);
        plugin.getConfigManager().getConfig().set("groups." + groupName + ".permissions", permissions);
        plugin.getConfigManager().saveConfig("config");
        
        sender.sendMessage(SUCCESS_PREFIX + "Added permission " + HIGHLIGHT + permission + SUCCESS_PREFIX + " to group " + HIGHLIGHT + groupName + SUCCESS_PREFIX + ".");
        
        // Update permissions for all online players
        plugin.getPermissionManager().reloadPermissionsWithoutSaving();
        
        return true;
    }

    /**
     * Removes a permission from a group
     * @param groupName The group name
     * @param permission The permission to remove
     * @param sender The command sender
     * @return true if successful
     */
    private boolean removeGroupPermission(String groupName, String permission, CommandSender sender) {
        sender.sendMessage(HEADER);
        
        List<String> permissions = plugin.getConfigManager().getConfig().getStringList("groups." + groupName + ".permissions");
        if (!permissions.contains(permission)) {
            sender.sendMessage(SUCCESS_PREFIX + "Group " + HIGHLIGHT + groupName + SUCCESS_PREFIX + " does not have permission " + HIGHLIGHT + permission + SUCCESS_PREFIX + ".");
            return true;
        }
        
        permissions.remove(permission);
        plugin.getConfigManager().getConfig().set("groups." + groupName + ".permissions", permissions);
        plugin.getConfigManager().saveConfig("config");
        
        sender.sendMessage(SUCCESS_PREFIX + "Removed permission " + HIGHLIGHT + permission + SUCCESS_PREFIX + " from group " + HIGHLIGHT + groupName + SUCCESS_PREFIX + ".");
        
        // Update permissions for all online players
        plugin.getPermissionManager().reloadPermissionsWithoutSaving();
        
        return true;
    }

    /**
     * Adds a permission to a player or to default permissions
     * @param player The player to add the permission to, or null for default permissions
     * @param permission The permission to add
     * @param sender The command sender
     * @return true if successful
     */
    private boolean addPermission(OfflinePlayer player, String permission, CommandSender sender) {
        sender.sendMessage(HEADER);
        
        if (player == null) {
            // Add to default permissions
            List<String> permissions = plugin.getConfigManager().getConfig().getStringList("default.permissions");
            if (permissions.contains(permission)) {
                sender.sendMessage(SUCCESS_PREFIX + "Permission " + HIGHLIGHT + permission + SUCCESS_PREFIX + " is already a default permission.");
                return true;
            }
            
            permissions.add(permission);
            plugin.getConfigManager().getConfig().set("default.permissions", permissions);
            plugin.getConfigManager().saveConfig("config");
            
            sender.sendMessage(SUCCESS_PREFIX + "Added permission " + HIGHLIGHT + permission + SUCCESS_PREFIX + " to " + HIGHLIGHT + "all players" + SUCCESS_PREFIX + ".");
        } else {
            // Add to player permissions
            List<String> permissions = plugin.getConfigManager().getConfig().getStringList("users." + player.getName() + ".permissions");
            if (permissions.contains(permission)) {
                sender.sendMessage(SUCCESS_PREFIX + "Player " + HIGHLIGHT + player.getName() + SUCCESS_PREFIX + " already has permission " + HIGHLIGHT + permission + SUCCESS_PREFIX + ".");
                return true;
            }
            
            permissions.add(permission);
            plugin.getConfigManager().getConfig().set("users." + player.getName() + ".permissions", permissions);
            plugin.getConfigManager().saveConfig("config");
            
            sender.sendMessage(SUCCESS_PREFIX + "Added permission " + HIGHLIGHT + permission + SUCCESS_PREFIX + " to player " + HIGHLIGHT + player.getName() + SUCCESS_PREFIX + ".");
            
            // Update permissions for online players
            if (player.isOnline()) {
                plugin.getPermissionManager().addPermissions(player.getPlayer());
            }
        }
        
        return true;
    }

    /**
     * Removes a permission from a player or from default permissions
     * @param player The player to remove the permission from, or null for default permissions
     * @param permission The permission to remove
     * @param sender The command sender
     * @return true if successful
     */
    private boolean removePermission(OfflinePlayer player, String permission, CommandSender sender) {
        sender.sendMessage(HEADER);
        
        if (player == null) {
            // Remove from default permissions
            List<String> permissions = plugin.getConfigManager().getConfig().getStringList("default.permissions");
            if (!permissions.contains(permission)) {
                sender.sendMessage(SUCCESS_PREFIX + "Permission " + HIGHLIGHT + permission + SUCCESS_PREFIX + " is not a default permission.");
                return true;
            }
            
            permissions.remove(permission);
            plugin.getConfigManager().getConfig().set("default.permissions", permissions);
            plugin.getConfigManager().saveConfig("config");
            
            sender.sendMessage(SUCCESS_PREFIX + "Removed permission " + HIGHLIGHT + permission + SUCCESS_PREFIX + " from " + HIGHLIGHT + "all players" + SUCCESS_PREFIX + ".");
            
            // Update permissions for all online players
            plugin.getPermissionManager().reloadPermissionsWithoutSaving();
        } else {
            // Remove from player permissions
            List<String> permissions = plugin.getConfigManager().getConfig().getStringList("users." + player.getName() + ".permissions");
            if (!permissions.contains(permission)) {
                sender.sendMessage(SUCCESS_PREFIX + "Player " + HIGHLIGHT + player.getName() + SUCCESS_PREFIX + " does not have permission " + HIGHLIGHT + permission + SUCCESS_PREFIX + ".");
                return true;
            }
            
            permissions.remove(permission);
            plugin.getConfigManager().getConfig().set("users." + player.getName() + ".permissions", permissions);
            plugin.getConfigManager().saveConfig("config");
            
            sender.sendMessage(SUCCESS_PREFIX + "Removed permission " + HIGHLIGHT + permission + SUCCESS_PREFIX + " from player " + HIGHLIGHT + player.getName() + SUCCESS_PREFIX + ".");
            
            // Update permissions for online players
            if (player.isOnline()) {
                plugin.getPermissionManager().addPermissions(player.getPlayer());
            }
        }
        
        return true;
    }

    /**
     * Sets the prefix for a group
     * @param groupName The group name
     * @param prefix The prefix to set
     * @param sender The command sender
     * @return true if successful
     */
    private boolean prefixGroup(String groupName, String prefix, CommandSender sender) {
        sender.sendMessage(HEADER);
        
        // Check if group exists
        if (!plugin.getConfigManager().getConfig().contains("groups." + groupName)) {
            sender.sendMessage(ERROR_PREFIX + "Group " + HIGHLIGHT + groupName + ERROR_PREFIX + " does not exist.");
            return true;
        }
        
        plugin.getConfigManager().getConfig().set("groups." + groupName + ".prefix", prefix);
        plugin.getConfigManager().saveConfig("config");
        
        sender.sendMessage(SUCCESS_PREFIX + "Set prefix of group " + HIGHLIGHT + groupName + SUCCESS_PREFIX + " to " + HIGHLIGHT + prefix + SUCCESS_PREFIX + ".");
        
        return true;
    }

    /**
     * Lists all default permissions
     * @param sender The command sender
     * @return true if successful
     */
    private boolean listDefaultPermissions(CommandSender sender) {
        sender.sendMessage(HEADER);
        sender.sendMessage(INFO_PREFIX + "Default permissions: ");
        
        List<String> permissions = plugin.getConfigManager().getConfig().getStringList("default.permissions");
        if (permissions.isEmpty()) {
            sender.sendMessage(INFO_PREFIX + "No default permissions set.");
        } else {
            for (String permission : permissions) {
                sender.sendMessage(INFO_PREFIX + "- " + HIGHLIGHT + permission);
            }
        }
        
        return true;
    }

    /**
     * Lists all permissions for a group
     * @param args The command arguments
     * @param sender The command sender
     * @return true if successful
     */
    private boolean listGroupPermissions(String[] args, CommandSender sender) {
        sender.sendMessage(HEADER);
        
        String groupName = args[0];
        
        // Check if group exists
        if (!plugin.getConfigManager().getConfig().contains("groups." + groupName)) {
            sender.sendMessage(ERROR_PREFIX + "Group " + HIGHLIGHT + groupName + ERROR_PREFIX + " does not exist.");
            return true;
        }
        
        sender.sendMessage(INFO_PREFIX + "Group: " + HIGHLIGHT + groupName);
        
        // List permissions
        sender.sendMessage(INFO_PREFIX + "Group permissions: ");
        List<String> permissions = plugin.getConfigManager().getConfig().getStringList("groups." + groupName + ".permissions");
        if (permissions.isEmpty()) {
            sender.sendMessage(INFO_PREFIX + "No permissions set for this group.");
        } else {
            for (String permission : permissions) {
                sender.sendMessage(INFO_PREFIX + "- " + HIGHLIGHT + permission);
            }
        }
        
        // List members
        sender.sendMessage(INFO_PREFIX + "Members: ");
        List<String> members = new ArrayList<>();
        for (String user : plugin.getConfigManager().getConfig().getConfigurationSection("users").getKeys(false)) {
            if (plugin.getConfigManager().getConfig().getStringList("users." + user + ".groups").contains(groupName)) {
                members.add(user);
            }
        }
        
        if (members.isEmpty()) {
            sender.sendMessage(INFO_PREFIX + "No members in this group.");
        } else {
            sender.sendMessage(HIGHLIGHT + members);
        }
        
        return true;
    }

    /**
     * Lists all groups
     * @param sender The command sender
     * @return true if successful
     */
    private boolean listGroups(CommandSender sender) {
        sender.sendMessage(HEADER);
        sender.sendMessage(INFO_PREFIX + "Groups:");
        
        if (!plugin.getConfigManager().getConfig().contains("groups")) {
            sender.sendMessage(INFO_PREFIX + "No groups defined.");
            return true;
        }
        
        for (String group : plugin.getConfigManager().getConfig().getConfigurationSection("groups").getKeys(false)) {
            int users = 0;
            int perms = 0;
            
            // Count users
            for (String user : plugin.getConfigManager().getConfig().getConfigurationSection("users").getKeys(false)) {
                if (plugin.getConfigManager().getConfig().getStringList("users." + user + ".groups").contains(group)) {
                    users++;
                }
            }
            
            // Count permissions
            perms = plugin.getConfigManager().getConfig().getStringList("groups." + group + ".permissions").size();
            
            sender.sendMessage(INFO_PREFIX + "- " + HIGHLIGHT + group + INFO_PREFIX + " (" + perms + " permissions, " + users + " members)");
        }
        
        return true;
    }

    /**
     * Lists the family (parents and children) of a group
     * @param sender The command sender
     * @param groupName The group name
     * @return true if successful
     */
    private boolean listFamily(CommandSender sender, String groupName) {
        sender.sendMessage(HEADER);
        
        // Check if group exists
        if (!plugin.getConfigManager().getConfig().contains("groups." + groupName)) {
            sender.sendMessage(ERROR_PREFIX + "Group " + HIGHLIGHT + groupName + ERROR_PREFIX + " does not exist.");
            return true;
        }

        sender.sendMessage(INFO_PREFIX + "Group: " + HIGHLIGHT + groupName);
        
        // List children
        sender.sendMessage(INFO_PREFIX + "Children: ");
        List<String> children = new ArrayList<>();
        for (String group : plugin.getConfigManager().getConfig().getConfigurationSection("groups").getKeys(false)) {
            if (plugin.getConfigManager().getConfig().getStringList("groups." + group + ".parents").contains(groupName)) {
                children.add(group);
            }
        }
        
        if (children.isEmpty()) {
            sender.sendMessage(INFO_PREFIX + "No children for this group.");
        } else {
            sender.sendMessage(HIGHLIGHT + children);
        }
        
        // List parents
        sender.sendMessage(INFO_PREFIX + "Parents: ");
        List<String> parents = plugin.getConfigManager().getConfig().getStringList("groups." + groupName + ".parents");
        
        if (parents.isEmpty()) {
            sender.sendMessage(INFO_PREFIX + "No parents for this group.");
        } else {
            sender.sendMessage(HIGHLIGHT + parents);
        }
        
        return true;
    }

    /**
     * Lists all permissions for a player
     * @param player The player to list permissions for
     * @param sender The command sender
     * @return true if successful
     */
    private boolean listPermissions(OfflinePlayer player, CommandSender sender) {
        sender.sendMessage(HEADER);
        sender.sendMessage(INFO_PREFIX + "User: " + HIGHLIGHT + player.getName());
        
        // List default permissions
        sender.sendMessage(INFO_PREFIX + "Default permissions: ");
        List<String> defaultPermissions = plugin.getConfigManager().getConfig().getStringList("default.permissions");
        if (defaultPermissions.isEmpty()) {
            sender.sendMessage(INFO_PREFIX + "No default permissions set.");
        } else {
            for (String permission : defaultPermissions) {
                sender.sendMessage(INFO_PREFIX + "- " + HIGHLIGHT + permission);
            }
        }
        
        // List user permissions
        sender.sendMessage(INFO_PREFIX + "User permissions: ");
        List<String> userPermissions = plugin.getConfigManager().getConfig().getStringList("users." + player.getName() + ".permissions");
        if (userPermissions.isEmpty()) {
            sender.sendMessage(INFO_PREFIX + "No user-specific permissions set.");
        } else {
            for (String permission : userPermissions) {
                sender.sendMessage(INFO_PREFIX + "- " + HIGHLIGHT + permission);
            }
        }
        
        // List groups
        sender.sendMessage(INFO_PREFIX + "Groups: ");
        List<String> groups = plugin.getConfigManager().getConfig().getStringList("users." + player.getName() + ".groups");
        if (groups.isEmpty()) {
            sender.sendMessage(INFO_PREFIX + "User is not in any groups.");
        } else {
            for (String group : groups) {
                sender.sendMessage(INFO_PREFIX + "- " + HIGHLIGHT + group);
            }
        }
        
        return true;
    }

    /**
     * Adds a player to a group
     * @param playerName The name of the player
     * @param groupName The name of the group
     * @param sender The command sender
     * @return true if successful
     */
    private boolean addGroupMember(String playerName, String groupName, CommandSender sender) {
        OfflinePlayer targetPlayer = null;
        try {
            // Try to get player by name first
            targetPlayer = Bukkit.getOfflinePlayer(playerName);
            
            // If not found, try to parse as UUID
            if (targetPlayer == null || !targetPlayer.hasPlayedBefore()) {
                try {
                    UUID uuid = UUID.fromString(playerName);
                    targetPlayer = Bukkit.getOfflinePlayer(uuid);
                } catch (IllegalArgumentException e) {
                    // Not a valid UUID, continue with null targetPlayer
                }
            }
            
            if (targetPlayer == null || !targetPlayer.hasPlayedBefore()) {
                sender.sendMessage(ERROR_PREFIX + "Player not found: " + playerName);
                return true;
            }
        } catch (Exception e) {
            sender.sendMessage(ERROR_PREFIX + "Error finding player: " + e.getMessage());
            return true;
        }
        
        return addGroup(targetPlayer, groupName, sender);
    }

    /**
     * Removes a player from a group
     * @param playerName The name of the player
     * @param groupName The name of the group
     * @param sender The command sender
     * @return true if successful
     */
    private boolean removeGroupMember(String playerName, String groupName, CommandSender sender) {
        OfflinePlayer targetPlayer = null;
        try {
            // Try to get player by name first
            targetPlayer = Bukkit.getOfflinePlayer(playerName);
            
            // If not found, try to parse as UUID
            if (targetPlayer == null || !targetPlayer.hasPlayedBefore()) {
                try {
                    UUID uuid = UUID.fromString(playerName);
                    targetPlayer = Bukkit.getOfflinePlayer(uuid);
                } catch (IllegalArgumentException e) {
                    // Not a valid UUID, continue with null targetPlayer
                }
            }
            
            if (targetPlayer == null || !targetPlayer.hasPlayedBefore()) {
                sender.sendMessage(ERROR_PREFIX + "Player not found: " + playerName);
                return true;
            }
        } catch (Exception e) {
            sender.sendMessage(ERROR_PREFIX + "Error finding player: " + e.getMessage());
            return true;
        }
        
        return removeGroup(targetPlayer, groupName, sender);
    }

    /**
     * Shifts the arguments array by removing the first element
     * @param args The arguments array
     * @return The shifted array
     */
    private String[] shift(String[] args) {
        return Arrays.copyOfRange(args, 1, args.length);
    }

    /**
     * Displays usage information for a command
     * @param sender The command sender
     * @param command The command to display usage for
     */
    private void usage(CommandSender sender, String command) {
        sender.sendMessage(HEADER);
        
        switch (command.toLowerCase()) {
            case "main":
                sender.sendMessage(INFO_PREFIX + "Permission Commands:");
                sender.sendMessage(INFO_PREFIX + "/permission user <player> <add|remove|addgroup|removegroup|info> [permission|group]");
                sender.sendMessage(INFO_PREFIX + "/permission group <group> <add|remove|prefix|info|addmember|removemember|family|addparent|removeparent> [permission|prefix|player|parent]");
                sender.sendMessage(INFO_PREFIX + "/permission listgroups");
                sender.sendMessage(INFO_PREFIX + "/permission reload");
                sender.sendMessage(INFO_PREFIX + "/permission default <add|remove|info> [permission]");
                break;
            case "user":
                sender.sendMessage(INFO_PREFIX + "User Commands:");
                sender.sendMessage(INFO_PREFIX + "/permission user <player> add <permission>");
                sender.sendMessage(INFO_PREFIX + "/permission user <player> remove <permission>");
                sender.sendMessage(INFO_PREFIX + "/permission user <player> addgroup <group>");
                sender.sendMessage(INFO_PREFIX + "/permission user <player> removegroup <group>");
                sender.sendMessage(INFO_PREFIX + "/permission user <player> info");
                break;
            case "group":
                sender.sendMessage(INFO_PREFIX + "Group Commands:");
                sender.sendMessage(INFO_PREFIX + "/permission group <group> add <permission>");
                sender.sendMessage(INFO_PREFIX + "/permission group <group> remove <permission>");
                sender.sendMessage(INFO_PREFIX + "/permission group <group> prefix <prefix>");
                sender.sendMessage(INFO_PREFIX + "/permission group <group> info");
                sender.sendMessage(INFO_PREFIX + "/permission group <group> addmember <player>");
                sender.sendMessage(INFO_PREFIX + "/permission group <group> removemember <player>");
                sender.sendMessage(INFO_PREFIX + "/permission group <group> family");
                sender.sendMessage(INFO_PREFIX + "/permission group <group> addparent <parent>");
                sender.sendMessage(INFO_PREFIX + "/permission group <group> removeparent <parent>");
                break;
            case "default":
                sender.sendMessage(INFO_PREFIX + "Default Commands:");
                sender.sendMessage(INFO_PREFIX + "/permission default add <permission>");
                sender.sendMessage(INFO_PREFIX + "/permission default remove <permission>");
                sender.sendMessage(INFO_PREFIX + "/permission default info");
                break;
        }
    }
}
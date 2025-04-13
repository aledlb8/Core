package dev.aledlb.utilities;

import dev.aledlb.Core;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages player permissions for the Core plugin.
 * This class handles all permission-related operations including:
 * - Adding/removing permissions
 * - Managing permission attachments
 * - Group-based permissions
 * - User-specific permissions
 */
public class PermissionManager {
    private final Core plugin;
    private final FileConfiguration config;
    private final ConcurrentHashMap<UUID, PermissionAttachment> permissions;

    public PermissionManager(Core plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
        this.permissions = new ConcurrentHashMap<>();
    }

    /**
     * Adds permissions to all online players
     */
    public void addPermissionsToOnlinePlayers() {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            if (player != null) {
                addPermissions(player);
            }
        }
    }

    /**
     * Adds permissions to a specific player
     * @param player The player to add permissions to
     */
    public void addPermissions(Player player) {
        PermissionAttachment attachment = player.addAttachment(plugin);

        // Add default permissions
        for (String permission : config.getStringList("default.permissions")) {
            addPermission(attachment, permission);
        }

        // Add group permissions
        addGroupPermissions(player, attachment);

        // Add user-specific permissions
        for (String perm : config.getStringList("users." + player.getName() + ".permissions")) {
            addPermission(attachment, perm);
        }

        permissions.put(player.getUniqueId(), attachment);
        
        // Update commands if on MC 1.13+
        if (Core.getMcVersion() >= 13) {
            player.updateCommands();
        }
    }

    /**
     * Adds group-based permissions to a player
     * @param player The player to add group permissions to
     * @param attachment The permission attachment to add permissions to
     */
    private void addGroupPermissions(Player player, PermissionAttachment attachment) {
        for (String group : config.getStringList("users." + player.getName() + ".groups")) {
            Logger.debug("User " + player.getName() + " is in group " + group);
            
            // Add direct group permissions
            for (String perm : config.getStringList("groups." + group + ".permissions")) {
                addPermission(attachment, perm);
            }
            
            // Add parent group permissions
            for (String parent : config.getStringList("groups." + group + ".parents")) {
                for (String perm : config.getStringList("groups." + parent + ".permissions")) {
                    addPermission(attachment, perm);
                }
            }
        }
    }

    /**
     * Adds a single permission to a permission attachment
     * @param attachment The permission attachment
     * @param permission The permission to add
     */
    private void addPermission(PermissionAttachment attachment, String permission) {
        try {
            attachment.setPermission(permission, true);
        } catch (Exception e) {
            Logger.warning("Failed to add permission " + permission + ": " + e.getMessage());
        }
    }

    /**
     * Removes all permission attachments
     */
    public void removeAllPermissions() {
        for (PermissionAttachment attachment : permissions.values()) {
            try {
                attachment.remove();
            } catch (Exception e) {
                Logger.warning("Failed to remove permission attachment: " + e.getMessage());
            }
        }
        permissions.clear();
    }

    /**
     * Removes permissions for a specific player
     * @param player The player to remove permissions from
     */
    public void removePermissions(Player player) {
        PermissionAttachment attachment = permissions.remove(player.getUniqueId());
        if (attachment != null) {
            try {
                player.removeAttachment(attachment);
            } catch (Exception e) {
                Logger.warning("Failed to remove permissions for " + player.getName() + ": " + e.getMessage());
            }
        }
    }

    /**
     * Reloads all permissions
     */
    public void reloadPermissions() {
        removeAllPermissions();
        addPermissionsToOnlinePlayers();
        plugin.saveConfig();
    }

    /**
     * Reloads all permissions without saving the config
     */
    public void reloadPermissionsWithoutSaving() {
        removeAllPermissions();
        addPermissionsToOnlinePlayers();
    }

    /**
     * Gets the permission attachment for a player
     * @param player The player to get the attachment for
     * @return The permission attachment or null if not found
     */
    public PermissionAttachment getPermissionAttachment(Player player) {
        return permissions.get(player.getUniqueId());
    }
} 
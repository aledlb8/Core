package dev.aledlb.commands.staff.moderation;

import dev.aledlb.utilities.Logger;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Handles the unmute command which allows muted players to chat again.
 * This command can only be used by players with the 'core.mute' permission.
 */
public class Unmute implements CommandExecutor {
    private static final String PERMISSION = "core.mute";
    private static final String USAGE = "/unmute <player>";
    private static final String ERROR_NO_PERMISSION = "§cYou don't have permission to use this command.";
    private static final String ERROR_USAGE = "§cUsage: " + USAGE;
    private static final String ERROR_PLAYER_NOT_FOUND = "§cPlayer not found.";
    private static final String SUCCESS_UNMUTE = "§aPlayer %s has been unmuted.";
    private static final String TARGET_UNMUTED = "§aYou have been unmuted.";

    private final JavaPlugin plugin;

    /**
     * Creates a new Unmute command executor.
     * @param plugin The plugin instance.
     */
    public Unmute(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission(PERMISSION)) {
            sender.sendMessage(ERROR_NO_PERMISSION);
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(ERROR_USAGE);
            return true;
        }

        if (args.length == 1) {
            handleUnmuteCommand(sender, args[0]);
        }

        return true;
    }

    /**
     * Handles the unmute command execution.
     * @param sender The command sender.
     * @param targetName The name of the player to unmute.
     */
    private void handleUnmuteCommand(CommandSender sender, String targetName) {
        Player target = Bukkit.getPlayer(targetName);
        if (target == null) {
            Logger.player(sender, ERROR_PLAYER_NOT_FOUND);
            return;
        }

        unmutePlayer(target);
        Logger.player(sender, String.format(SUCCESS_UNMUTE, target.getName()));
        target.sendMessage(TARGET_UNMUTED);
    }

    /**
     * Unmutes a player by removing their metadata.
     * @param player The player to unmute.
     */
    private void unmutePlayer(Player player) {
        player.removeMetadata("muted", plugin);
    }
}

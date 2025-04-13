package dev.aledlb.commands.staff.moderation;

import dev.aledlb.utilities.Logger;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Handles the mute command which prevents players from chatting.
 * This command can only be used by players with the 'core.mute' permission.
 */
public class Mute implements CommandExecutor {
    private static final String PERMISSION = "core.mute";
    private static final String USAGE = "/mute <player>";
    private static final String ERROR_NO_PERMISSION = "§cYou don't have permission to use this command.";
    private static final String ERROR_USAGE = "§cUsage: " + USAGE;
    private static final String ERROR_PLAYER_NOT_FOUND = "§cPlayer not found.";
    private static final String ERROR_CANNOT_MUTE = "§cYou can't mute this player.";
    private static final String SUCCESS_MUTE = "§aPlayer %s has been muted.";
    private static final String TARGET_MUTED = "§cYou have been muted.";

    private final JavaPlugin plugin;

    /**
     * Creates a new Mute command executor.
     * @param plugin The plugin instance.
     */
    public Mute(JavaPlugin plugin) {
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
            handleMuteCommand(sender, args[0]);
        }

        return true;
    }

    /**
     * Handles the mute command execution.
     * @param sender The command sender.
     * @param targetName The name of the player to mute.
     */
    private void handleMuteCommand(CommandSender sender, String targetName) {
        Player target = Bukkit.getPlayer(targetName);
        if (target == null) {
            sender.sendMessage(ERROR_PLAYER_NOT_FOUND);
            return;
        }

        if (cannotBeMuted(target)) {
            sender.sendMessage(ERROR_CANNOT_MUTE);
            return;
        }

        mutePlayer(target);
        Logger.player(sender, String.format(SUCCESS_MUTE, target.getName()));
        target.sendMessage(TARGET_MUTED);
    }

    /**
     * Checks if a player cannot be muted.
     * @param player The player to check.
     * @return true if the player cannot be muted, false otherwise.
     */
    private boolean cannotBeMuted(Player player) {
        return player.isOp() || player.hasPermission("core.mute.bypass");
    }

    /**
     * Mutes a player by setting their metadata.
     * @param player The player to mute.
     */
    private void mutePlayer(Player player) {
        player.setMetadata("muted", new FixedMetadataValue(plugin, true));
    }
}

package dev.aledlb.commands.staff.moderation;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;

/**
 * Handles the freeze command which prevents players from moving.
 * This command can only be used by players with the 'core.freeze' permission.
 */
public class Freeze implements CommandExecutor {
    private static final String PERMISSION = "core.freeze";
    private static final String USAGE = "/freeze <player>";
    private static final String ERROR_NO_PERMISSION = "§cYou don't have permission to use this command.";
    private static final String ERROR_USAGE = "§cUsage: " + USAGE;
    private static final String ERROR_PLAYER_NOT_FOUND = "§cPlayer not found.";
    private static final String ERROR_CANNOT_FREEZE = "§cYou can't freeze this player.";
    private static final String SUCCESS_FREEZE = "§aPlayer %s has been frozen.";
    private static final String SUCCESS_UNFREEZE = "§aPlayer %s has been unfrozen.";
    private static final String TARGET_FROZEN_TITLE = "§cYou have been frozen";
    private static final String TARGET_FROZEN_SUBTITLE = "§cYou can't move, don't disconnect or you will be banned";
    private static final String TARGET_UNFROZEN_TITLE = "§aYou have been unfrozen";
    private static final String TARGET_UNFROZEN_SUBTITLE = "§aYou can move again";

    private static final int TITLE_FADE_IN = 10;
    private static final int TITLE_STAY = 70;
    private static final int TITLE_FADE_OUT = 20;

    private final JavaPlugin plugin;

    /**
     * Creates a new Freeze command executor.
     * @param plugin The plugin instance.
     */
    public Freeze(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission(PERMISSION)) {
            sender.sendMessage(ERROR_NO_PERMISSION);
            return true;
        }

        if (args.length != 1) {
            sender.sendMessage(ERROR_USAGE);
            return true;
        }

        handleFreezeCommand(sender, args[0]);
        return true;
    }

    /**
     * Handles the freeze command execution.
     * @param sender The command sender.
     * @param targetName The name of the player to freeze/unfreeze.
     */
    private void handleFreezeCommand(CommandSender sender, String targetName) {
        Player target = Bukkit.getPlayerExact(targetName);
        if (target == null) {
            sender.sendMessage(ERROR_PLAYER_NOT_FOUND);
            return;
        }

        if (cannotBeFrozen(target)) {
            sender.sendMessage(ERROR_CANNOT_FREEZE);
            return;
        }

        toggleFreezeState(target, sender);
    }

    /**
     * Checks if a player cannot be frozen.
     * @param player The player to check.
     * @return true if the player cannot be frozen, false otherwise.
     */
    private boolean cannotBeFrozen(Player player) {
        return player.hasPermission("core.freeze.bypass") || player.isOp();
    }

    /**
     * Toggles the freeze state of a player.
     * @param target The player to toggle.
     * @param sender The command sender.
     */
    private void toggleFreezeState(Player target, CommandSender sender) {
        if (isPlayerFrozen(target)) {
            unfreezePlayer(target);
            sender.sendMessage(String.format(SUCCESS_UNFREEZE, target.getName()));
        } else {
            freezePlayer(target);
            sender.sendMessage(String.format(SUCCESS_FREEZE, target.getName()));
        }
    }

    /**
     * Checks if a player is frozen.
     * @param player The player to check.
     * @return true if the player is frozen, false otherwise.
     */
    private boolean isPlayerFrozen(Player player) {
        for (MetadataValue value : player.getMetadata("frozen")) {
            if (value.getOwningPlugin() == plugin) {
                return value.asBoolean();
            }
        }
        return false;
    }

    /**
     * Freezes a player by setting their metadata and sending a title.
     * @param player The player to freeze.
     */
    private void freezePlayer(Player player) {
        player.setMetadata("frozen", new FixedMetadataValue(plugin, true));
        player.sendTitle(TARGET_FROZEN_TITLE, TARGET_FROZEN_SUBTITLE, TITLE_FADE_IN, TITLE_STAY, TITLE_FADE_OUT);
    }

    /**
     * Unfreezes a player by removing their metadata and sending a title.
     * @param player The player to unfreeze.
     */
    private void unfreezePlayer(Player player) {
        player.removeMetadata("frozen", plugin);
        player.sendTitle(TARGET_UNFROZEN_TITLE, TARGET_UNFROZEN_SUBTITLE, TITLE_FADE_IN, TITLE_STAY, TITLE_FADE_OUT);
    }
}
package dev.aledlb.commands.staff.gamemode;

import dev.aledlb.utilities.Logger;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Base class for gamemode commands.
 * Provides common functionality for all gamemode commands.
 */
public abstract class BaseGameModeCommand implements CommandExecutor {
    private static final String PERMISSION = "core.gamemode";
    private static final String ERROR_PLAYER_ONLY = "§cThis command can only be run by a player.";
    private static final String ERROR_NO_PERMISSION = "§cYou don't have permission to use this command.";
    private static final String SUCCESS_GAMEMODE = "§aGamemode updated to %s";

    /**
     * Gets the gamemode to set.
     * @return The gamemode to set.
     */
    protected abstract GameMode getGameMode();

    /**
     * Gets the name of the gamemode.
     * @return The name of the gamemode.
     */
    protected abstract String getGameModeName();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ERROR_PLAYER_ONLY);
            return true;
        }

        if (!player.hasPermission(PERMISSION)) {
            player.sendMessage(ERROR_NO_PERMISSION);
            return true;
        }

        handleGameModeCommand(player);
        return true;
    }

    /**
     * Handles the gamemode command execution.
     * @param player The player executing the command.
     */
    private void handleGameModeCommand(Player player) {
        player.setGameMode(getGameMode());
        Logger.player(player, String.format(SUCCESS_GAMEMODE, getGameModeName()));
    }
} 
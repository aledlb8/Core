package dev.aledlb.commands.staff.gamemode;

import dev.aledlb.utilities.Logger;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Handles the /gmc command which sets a player's gamemode to Creative.
 */
public class GMC extends BaseGameModeCommand {
    @Override
    protected GameMode getGameMode() {
        return GameMode.CREATIVE;
    }

    @Override
    protected String getGameModeName() {
        return "Creative";
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be run by a player.");
            return true;
        }

        if (!player.hasPermission("core.gamemode")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return true;
        }

        player.setGameMode(GameMode.CREATIVE);
        Logger.player(player, ChatColor.GREEN + "Gamemode updated to Creative");
        return true;
    }
}

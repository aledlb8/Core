package dev.aledlb.commands.staff;

import dev.aledlb.utilities.Logger;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Fly implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be run by a player.");
            return true;
        }

        if (!player.hasPermission("core.fly")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return true;
        }

        if (player.getAllowFlight()) {
            player.setAllowFlight(false);
            player.setFlying(false);
            Logger.player(player, ChatColor.GREEN + "Flight mode disabled");
        } else {
            player.setAllowFlight(true);
            player.setFlying(true);
            Logger.player(player, ChatColor.GREEN + "Flight mode enabled");
        }
        return true;
    }
}

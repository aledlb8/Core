package dev.aledlb.commands.staff;

import dev.aledlb.utilities.Logger;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static dev.aledlb.features.kits.KitManager.plugin;

public class Unmute implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("core.mute")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "Usage: /unmute <player>");
            return true;
        }
        if (args.length == 1) {
            Player target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                Logger.player(sender, ChatColor.RED + "Player not found.");
                return true;
            }
            target.removeMetadata("muted", plugin);
            Logger.player(sender, ChatColor.GREEN + "Player " + target.getName() + " has been un muted.");
            target.sendMessage(ChatColor.GREEN + "You have been un muted.");
            return true;
        }
        return true;
    }
}

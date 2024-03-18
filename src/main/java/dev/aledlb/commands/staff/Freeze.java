package dev.aledlb.commands.staff;

import dev.aledlb.utilities.Logger;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static dev.aledlb.features.kits.KitManager.plugin;

public class Freeze implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("core.freeze")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "Usage: /freeze <player>");
            return true;
        }
        if (args.length == 1) {
            Player target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                sender.sendMessage(ChatColor.RED + "Player not found.");
                return true;
            }

            if (target.hasPermission("core.freeze.bypass") || target.isOp()) {
                sender.sendMessage(ChatColor.RED + "You can't freeze this player.");
                return true;
            }

            if (target.hasMetadata("frozen")) {
                target.removeMetadata("frozen", plugin);
                Logger.player(sender, ChatColor.GREEN + "Player " + target.getName() + " has been unfrozen.");
                target.sendTitle(ChatColor.GREEN + "You have been unfrozen", ChatColor.GREEN + "You can move", 10, 40, 10);
                return true;
            }

            target.setMetadata("frozen", new org.bukkit.metadata.FixedMetadataValue(plugin, true));
            Logger.player(sender, ChatColor.GREEN + "Player " + target.getName() + " has been frozen.");
            target.sendTitle(ChatColor.RED + "You have been frozen", ChatColor.RED + "You can't move, don't disconnect or you will be banned", 10, 40, 10);
            return true;
        }
        return true;
    }
}

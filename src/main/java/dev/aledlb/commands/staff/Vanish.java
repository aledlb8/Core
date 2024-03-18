package dev.aledlb.commands.staff;

import dev.aledlb.utilities.Logger;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static dev.aledlb.features.kits.KitManager.plugin;

public class Vanish implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be run by a player.");
            return true;
        }

        if (!player.hasPermission("core.mute")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return true;
        }

        if (player.hasMetadata("vanished")) {
            player.removeMetadata("vanished", plugin);
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                onlinePlayer.showPlayer(plugin, player);
            }
            Logger.player(player, ChatColor.GREEN + "Vanish mode disabled");
        } else {
            player.setMetadata("vanished", new org.bukkit.metadata.FixedMetadataValue(plugin, true));
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                onlinePlayer.hidePlayer(plugin, player);
            }
            Logger.player(player, ChatColor.GREEN + "Vanish mode enabled");
        }
        return true;
    }
}

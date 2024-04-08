package dev.aledlb.commands.staff;

import dev.aledlb.utilities.Logger;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class More implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be run by a player.");
            return true;
        }

        if (!player.hasPermission("core.more")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return true;
        }

        if (player.getItemInUse().getType()  == Material.AIR) {
            player.sendMessage(ChatColor.RED + "You must be holding an item to use this command.");
            return true;

        }

        if (player.getInventory().getItemInMainHand().getAmount() == player.getInventory().getItemInMainHand().getMaxStackSize()) {
            player.sendMessage(ChatColor.RED + "The item in your hand is already at its maximum stack size.");
            return true;
        }

        player.getInventory().getItemInMainHand().setAmount(player.getInventory().getItemInMainHand().getMaxStackSize());
        return true;
    }
}

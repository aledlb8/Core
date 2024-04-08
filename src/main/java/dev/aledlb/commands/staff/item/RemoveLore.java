package dev.aledlb.commands.staff.item;

import dev.aledlb.Core;
import dev.aledlb.utilities.Logger;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class RemoveLore implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be run by a player.");
            return true;
        }

        if (!player.hasPermission("core.removelore")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return true;
        }

        if (args.length == 0) {
            player.sendMessage(ChatColor.RED + "Correct usage: /addlore <number>");
            return true;
        }

        if (!Core.isInteger(args[0])) {
            player.sendMessage(ChatColor.RED + "Use numbers.");
            return true;
        }

        ItemStack item = player.getItemInHand();
        if (item == null || item.getType() == Material.AIR) {
            player.sendMessage(ChatColor.RED + "You must hold an item.");
            return true;
        }

        ItemMeta itemMeta = item.getItemMeta();
        if (!itemMeta.hasLore() || Integer.parseInt(args[0]) >= itemMeta.getLore().size()) {
            player.sendMessage(ChatColor.RED + "It seems that your item doesn't have that lore.");
            return true;
        }
        List<String> lore = itemMeta.getLore();
        lore.remove(Integer.parseInt(args[0]));
        itemMeta.setLore(lore);
        item.setItemMeta(itemMeta);

        player.updateInventory();

        Logger.player(player, ChatColor.GREEN + "You have added a new lore to your item.");

        return true;
    }
}
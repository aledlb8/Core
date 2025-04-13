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

import java.util.List;

/**
 * Handles the removelore command which allows players to remove lore lines from items in their hand.
 * This command can only be used by players with the 'core.removelore' permission.
 */
public class RemoveLore implements CommandExecutor {
    private static final String PERMISSION = "core.removelore";
    private static final String USAGE = "/removelore <number>";
    private static final String ERROR_PLAYER_ONLY = "§cThis command can only be run by a player.";
    private static final String ERROR_NO_PERMISSION = "§cYou don't have permission to use this command.";
    private static final String ERROR_USAGE = "§cCorrect usage: " + USAGE;
    private static final String ERROR_NO_ITEM = "§cYou must hold an item.";
    private static final String ERROR_NOT_NUMBER = "§cUse numbers.";
    private static final String ERROR_NO_LORE = "§cIt seems that your item doesn't have that lore.";
    private static final String SUCCESS_REMOVE_LORE = "§aYou have removed a lore line from your item.";

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

        if (args.length == 0) {
            player.sendMessage(ERROR_USAGE);
            return true;
        }

        handleRemoveLoreCommand(player, args);
        return true;
    }

    /**
     * Handles the removelore command execution.
     * @param player The player executing the command.
     * @param args The command arguments.
     */
    private void handleRemoveLoreCommand(Player player, String[] args) {
        if (!Core.isInteger(args[0])) {
            player.sendMessage(ERROR_NOT_NUMBER);
            return;
        }

        ItemStack item = player.getItemInHand();
        if (item == null || item.getType() == Material.AIR) {
            player.sendMessage(ERROR_NO_ITEM);
            return;
        }

        removeLoreFromItem(player, item, Integer.parseInt(args[0]));
    }

    /**
     * Removes a lore line from an item.
     * @param player The player executing the command.
     * @param item The item to remove lore from.
     * @param lineNumber The line number to remove.
     */
    private void removeLoreFromItem(Player player, ItemStack item, int lineNumber) {
        ItemMeta itemMeta = item.getItemMeta();
        if (!itemMeta.hasLore() || lineNumber >= itemMeta.getLore().size()) {
            player.sendMessage(ERROR_NO_LORE);
            return;
        }

        List<String> lore = itemMeta.getLore();
        lore.remove(lineNumber);
        itemMeta.setLore(lore);
        item.setItemMeta(itemMeta);
        player.updateInventory();
        Logger.player(player, SUCCESS_REMOVE_LORE);
    }
}
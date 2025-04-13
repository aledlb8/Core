package dev.aledlb.commands.staff.item;

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

/**
 * Handles the addlore command which allows players to add lore lines to items in their hand.
 * This command can only be used by players with the 'core.addlore' permission.
 */
public class AddLore implements CommandExecutor {
    private static final String PERMISSION = "core.addlore";
    private static final String USAGE = "/addlore <lore>";
    private static final String ERROR_PLAYER_ONLY = "§cThis command can only be run by a player.";
    private static final String ERROR_NO_PERMISSION = "§cYou don't have permission to use this command.";
    private static final String ERROR_USAGE = "§cCorrect usage: " + USAGE;
    private static final String ERROR_NO_ITEM = "§cYou must hold an item.";
    private static final String SUCCESS_ADD_LORE = "§aYou have added a new lore to your item.";

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

        handleAddLoreCommand(player, args);
        return true;
    }

    /**
     * Handles the addlore command execution.
     * @param player The player executing the command.
     * @param args The command arguments.
     */
    private void handleAddLoreCommand(Player player, String[] args) {
        ItemStack item = player.getItemInHand();
        if (item == null || item.getType() == Material.AIR) {
            player.sendMessage(ERROR_NO_ITEM);
            return;
        }

        addLoreToItem(player, item, args);
    }

    /**
     * Adds a new lore line to an item.
     * @param player The player executing the command.
     * @param item The item to add lore to.
     * @param args The command arguments containing the lore text.
     */
    private void addLoreToItem(Player player, ItemStack item, String[] args) {
        ItemMeta itemMeta = item.getItemMeta();
        if (!itemMeta.hasLore()) {
            itemMeta.setLore(new ArrayList<>());
        }

        String loreText = ChatColor.translateAlternateColorCodes('&', String.join("", args));
        List<String> lore = itemMeta.getLore();
        lore.add(loreText);
        itemMeta.setLore(lore);
        item.setItemMeta(itemMeta);
        player.updateInventory();
        Logger.player(player, SUCCESS_ADD_LORE);
    }
}
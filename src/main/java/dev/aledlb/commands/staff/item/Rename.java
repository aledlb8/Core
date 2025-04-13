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

/**
 * Handles the rename command which allows players to rename items in their hand.
 * This command can only be used by players with the 'core.rename' permission.
 */
public class Rename implements CommandExecutor {
    private static final String PERMISSION = "core.rename";
    private static final String USAGE = "/rename <displayName>";
    private static final String ERROR_PLAYER_ONLY = "§cThis command can only be run by a player.";
    private static final String ERROR_NO_PERMISSION = "§cYou don't have permission to use this command.";
    private static final String ERROR_USAGE = "§cCorrect usage: " + USAGE;
    private static final String ERROR_NO_ITEM = "§cYou must hold an item.";
    private static final String SUCCESS_RENAME = "§aItem has been renamed.";

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

        handleRenameCommand(player, args);
        return true;
    }

    /**
     * Handles the rename command execution.
     * @param player The player executing the command.
     * @param args The command arguments.
     */
    private void handleRenameCommand(Player player, String[] args) {
        ItemStack item = player.getItemInHand();
        if (item == null || item.getType() == Material.AIR) {
            player.sendMessage(ERROR_NO_ITEM);
            return;
        }

        renameItem(player, item, args);
    }

    /**
     * Renames an item with the given display name.
     * @param player The player executing the command.
     * @param item The item to rename.
     * @param args The command arguments containing the new display name.
     */
    private void renameItem(Player player, ItemStack item, String[] args) {
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', String.join("", args)));
        item.setItemMeta(itemMeta);
        player.updateInventory();
        Logger.player(player, SUCCESS_RENAME);
    }
}

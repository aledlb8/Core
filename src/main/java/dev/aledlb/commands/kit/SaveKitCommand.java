package dev.aledlb.commands.kit;

import dev.aledlb.features.kits.KitManager;
import dev.aledlb.utilities.Logger;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Handles the save kit sub-command.
 * This command allows players to save their current inventory as a kit.
 */
public class SaveKitCommand implements KitSubCommand {

    private static final String ERROR_PREFIX = "§c";
    private static final String SUCCESS_PREFIX = "§a";
    private static final String ERROR_NO_PERMISSION = ERROR_PREFIX + "You don't have permission to use this command.";
    private static final String ERROR_USAGE = ERROR_PREFIX + "Usage: /kit save <kitName>";
    private static final String ERROR_EMPTY_INVENTORY = ERROR_PREFIX + "You can't save an empty kit.";
    private static final String ERROR_NO_ITEMS = ERROR_PREFIX + "You must have armor and inventory items to save a kit.";
    private static final String SUCCESS_SAVE = SUCCESS_PREFIX + "Kit saved as %s";

    private final KitManager kitManager;

    /**
     * Creates a new SaveKitCommand instance
     * @param kitManager The KitManager instance
     */
    public SaveKitCommand(KitManager kitManager) {
        this.kitManager = kitManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Player player, String[] args) {
        try {
            if (!player.hasPermission("core.kit.save")) {
                player.sendMessage(ERROR_NO_PERMISSION);
                return true;
            }

            if (args.length < 2) {
                player.sendMessage(ERROR_USAGE);
                return true;
            }

            if (player.getInventory().isEmpty()) {
                player.sendMessage(ERROR_EMPTY_INVENTORY);
                return true;
            }

            if (player.getInventory().getArmorContents().length == 0 || player.getInventory().getContents().length == 0) {
                player.sendMessage(ERROR_NO_ITEMS);
                return true;
            }

            String kitName = args[1];
            kitManager.saveKit(player, kitName);
            Logger.player(player, String.format(SUCCESS_SAVE, kitName));
            return true;
        } catch (Exception e) {
            Logger.severe("Error saving kit: " + e.getMessage());
            player.sendMessage(ERROR_PREFIX + "An error occurred while saving the kit.");
            return true;
        }
    }
}
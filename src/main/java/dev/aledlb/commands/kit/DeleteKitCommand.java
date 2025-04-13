package dev.aledlb.commands.kit;

import dev.aledlb.features.kits.KitManager;
import dev.aledlb.utilities.Logger;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Handles the delete kit sub-command.
 * This command allows players to delete existing kits.
 */
public class DeleteKitCommand implements KitSubCommand {

    private static final String ERROR_PREFIX = "§c";
    private static final String SUCCESS_PREFIX = "§a";
    private static final String ERROR_NO_PERMISSION = ERROR_PREFIX + "You don't have permission to use this command.";
    private static final String ERROR_USAGE = ERROR_PREFIX + "Usage: /kit delete <kitName>";
    private static final String SUCCESS_DELETE = SUCCESS_PREFIX + "Kit %s deleted successfully";

    private final KitManager kitManager;

    /**
     * Creates a new DeleteKitCommand instance
     * @param kitManager The KitManager instance
     */
    public DeleteKitCommand(KitManager kitManager) {
        this.kitManager = kitManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Player player, String[] args) {
        try {
            if (!player.hasPermission("core.kit.delete")) {
                player.sendMessage(ERROR_NO_PERMISSION);
                return true;
            }

            if (args.length < 2) {
                player.sendMessage(ERROR_USAGE);
                return true;
            }

            String kitName = args[1];
            kitManager.deleteKit(player, kitName);
            Logger.player(player, String.format(SUCCESS_DELETE, kitName));
            return true;
        } catch (Exception e) {
            Logger.severe("Error deleting kit: " + e.getMessage());
            player.sendMessage(ERROR_PREFIX + "An error occurred while deleting the kit.");
            return true;
        }
    }
}
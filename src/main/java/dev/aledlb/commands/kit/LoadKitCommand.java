package dev.aledlb.commands.kit;

import dev.aledlb.features.kits.KitManager;
import dev.aledlb.utilities.Logger;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Handles the load kit sub-command.
 * This command allows players to load a kit for themselves or another player.
 */
public class LoadKitCommand implements KitSubCommand {

    private static final String ERROR_PREFIX = "§c";
    private static final String SUCCESS_PREFIX = "§a";
    private static final String ERROR_NO_PERMISSION = ERROR_PREFIX + "You don't have permission to use this command.";
    private static final String ERROR_USAGE = ERROR_PREFIX + "Usage: /kit load <kitName> [playerName]";
    private static final String ERROR_PLAYER_NOT_FOUND = ERROR_PREFIX + "Player not found: %s";
    private static final String SUCCESS_LOAD = SUCCESS_PREFIX + "Kit %s loaded for %s";

    private final KitManager kitManager;

    /**
     * Creates a new LoadKitCommand instance
     * @param kitManager The KitManager instance
     */
    public LoadKitCommand(KitManager kitManager) {
        this.kitManager = kitManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Player player, String[] args) {
        try {
            if (!player.hasPermission("core.kit.load")) {
                player.sendMessage(ERROR_NO_PERMISSION);
                return true;
            }

            if (args.length < 2) {
                player.sendMessage(ERROR_USAGE);
                return true;
            }

            String kitName = args[1];
            Player target = player;

            if (args.length >= 3) {
                if (!player.hasPermission("core.kit.load.others")) {
                    player.sendMessage(ERROR_NO_PERMISSION);
                    return true;
                }

                target = Bukkit.getPlayer(args[2]);
                if (target == null) {
                    player.sendMessage(String.format(ERROR_PLAYER_NOT_FOUND, args[2]));
                    return true;
                }
            }

            kitManager.loadKitForPlayer(target, kitName);
            Logger.player(player, String.format(SUCCESS_LOAD, kitName, target.getName()));
            return true;
        } catch (Exception e) {
            Logger.severe("Error loading kit: " + e.getMessage());
            player.sendMessage(ERROR_PREFIX + "An error occurred while loading the kit.");
            return true;
        }
    }
}
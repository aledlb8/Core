package dev.aledlb.commands.kit;

import dev.aledlb.features.kits.KitManager;
import dev.aledlb.utilities.Logger;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

/**
 * Handles all kit-related commands for the Core plugin.
 * This class provides functionality to:
 * - Save kits
 * - Load kits
 * - Delete kits
 * - List kits
 * - Open kit GUI
 */
public class KitCommand implements CommandExecutor {

    private static final String HEADER = "§3====[§bCore Kits§3]====";
    private static final String SUCCESS_PREFIX = "§2";
    private static final String ERROR_PREFIX = "§c";
    private static final String INFO_PREFIX = "§3";
    private static final String HIGHLIGHT = "§a";

    private final Map<String, KitSubCommand> subCommands;
    private final KitManager kitManager;

    /**
     * Creates a new KitCommand instance
     * @param kitManager The KitManager instance
     */
    public KitCommand(KitManager kitManager) {
        this.kitManager = kitManager;
        this.subCommands = new HashMap<>();
        registerSubCommands();
    }

    /**
     * Registers all sub-commands
     */
    private void registerSubCommands() {
        subCommands.put("save", new SaveKitCommand(kitManager));
        subCommands.put("load", new LoadKitCommand(kitManager));
        subCommands.put("delete", new DeleteKitCommand(kitManager));
        subCommands.put("list", new ListKitCommand(kitManager));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        try {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(ERROR_PREFIX + "This command can only be run by a player.");
                return true;
            }

            if (args.length == 0) {
                kitManager.showKitsGUI(player);
                return true;
            }

            String subCommand = args[0].toLowerCase();
            KitSubCommand cmd = subCommands.get(subCommand);

            if (cmd == null) {
                sendUsage(player);
                return true;
            }

            return cmd.onCommand(sender, player, args);
        } catch (Exception e) {
            Logger.severe("Error executing kit command: " + e.getMessage());
            sender.sendMessage(ERROR_PREFIX + "An error occurred while executing the command.");
            return true;
        }
    }

    /**
     * Sends the command usage to the player
     * @param player The player to send the usage to
     */
    private void sendUsage(Player player) {
        player.sendMessage(HEADER);
        player.sendMessage(INFO_PREFIX + "Kit Commands:");
        player.sendMessage(HIGHLIGHT + "/kit" + INFO_PREFIX + " - Open the kit GUI");
        player.sendMessage(HIGHLIGHT + "/kit save <kitName>" + INFO_PREFIX + " - Save your current inventory as a kit");
        player.sendMessage(HIGHLIGHT + "/kit load <kitName> [playerName]" + INFO_PREFIX + " - Load a kit");
        player.sendMessage(HIGHLIGHT + "/kit delete <kitName>" + INFO_PREFIX + " - Delete a kit");
        player.sendMessage(HIGHLIGHT + "/kit list" + INFO_PREFIX + " - List all available kits");
    }
}
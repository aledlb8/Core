package dev.aledlb.commands.kit;

import dev.aledlb.Core;
import dev.aledlb.features.kits.KitManager;
import dev.aledlb.utilities.Logger;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.Arrays;
import java.util.List;

/**
 * Handles the list kit sub-command.
 * This command allows players to view all available kits.
 */
public class ListKitCommand implements KitSubCommand {

    private static final String ERROR_PREFIX = "§c";
    private static final String SUCCESS_PREFIX = "§a";
    private static final String INFO_PREFIX = "§3";
    private static final String HIGHLIGHT = "§a";
    private static final String HEADER = "§3====[§bAvailable Kits§3]====";
    private static final String ERROR_NO_PERMISSION = ERROR_PREFIX + "You don't have permission to use this command.";
    private static final String ERROR_NO_KITS = ERROR_PREFIX + "There are no kits available.";
    private static final String SUCCESS_LIST = SUCCESS_PREFIX + "Found %d kit(s):";

    private final KitManager kitManager;
    private final Core plugin;

    /**
     * Creates a new ListKitCommand instance
     * @param kitManager The KitManager instance
     */
    public ListKitCommand(KitManager kitManager) {
        this.kitManager = kitManager;
        this.plugin = Core.getInstance();
    }

    @Override
    public boolean onCommand(CommandSender sender, Player player, String[] args) {
        try {
            if (!player.hasPermission("core.kit.list")) {
                player.sendMessage(ERROR_NO_PERMISSION);
                return true;
            }

            File kitsFolder = new File(plugin.getDataFolder(), "kits");
            File[] kitFiles = kitsFolder.listFiles();

            if (kitFiles == null || kitFiles.length == 0) {
                player.sendMessage(ERROR_NO_KITS);
                return true;
            }

            player.sendMessage(HEADER);
            Logger.player(player, String.format(SUCCESS_LIST, kitFiles.length));

            List<String> kitNames = Arrays.stream(kitFiles)
                .filter(file -> file.isFile() && file.getName().endsWith(".yml"))
                .map(file -> file.getName().replace(".yml", ""))
                .sorted()
                .toList();

            for (String kitName : kitNames) {
                player.sendMessage(HIGHLIGHT + "- " + INFO_PREFIX + kitName);
            }

            return true;
        } catch (Exception e) {
            Logger.severe("Error listing kits: " + e.getMessage());
            player.sendMessage(ERROR_PREFIX + "An error occurred while listing the kits.");
            return true;
        }
    }
}
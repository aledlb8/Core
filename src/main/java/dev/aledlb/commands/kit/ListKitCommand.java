package dev.aledlb.commands.kit;

import dev.aledlb.features.kits.KitManager;
import dev.aledlb.utilities.Logger;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.File;

import static dev.aledlb.features.kits.KitManager.plugin;

public class ListKitCommand implements KitSubCommand {
    private final KitManager kitManager;

    public ListKitCommand(KitManager kitManager) {
        this.kitManager = kitManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Player player, String[] args) {
        if (!sender.hasPermission("core.kit.list")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return true;
        }

        File kitsFolder = new File(plugin.getDataFolder() + File.separator + "kits");
        File[] kitFiles = kitsFolder.listFiles();

        if (kitFiles == null || kitFiles.length == 0) {
            sender.sendMessage(ChatColor.RED + "There are no kits available.");

            return true;
        }

        Logger.player(player, ChatColor.GREEN + "Available Kits:");
        for (File kitFile : kitFiles) {
            String kitName = kitFile.getName().replace(".yml", "");
            sender.sendMessage(ChatColor.YELLOW + kitName);
        }
        return true;
    }
}
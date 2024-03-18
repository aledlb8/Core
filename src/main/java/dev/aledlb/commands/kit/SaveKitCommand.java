package dev.aledlb.commands.kit;

import dev.aledlb.features.kits.KitManager;
import dev.aledlb.utilities.Logger;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SaveKitCommand implements KitSubCommand {
    private final KitManager kitManager;

    public SaveKitCommand(KitManager kitManager) {
        this.kitManager = kitManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Player player, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be run by a player.");
            return true;
        }

        if (!player.hasPermission("core.kit.save")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return true;
        }

        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /kit save <kitName>");
            return true;
        }

        if (player.getInventory().isEmpty()) {
            player.sendMessage(ChatColor.RED + "You can't save an empty kit.");
            return true;
        }

        if (player.getInventory().getArmorContents().length == 0 || player.getInventory().getContents().length == 0) {
            player.sendMessage(ChatColor.RED + "You must have armor and inventory items to save a kit.");
            return true;
        }

        String kitName = args[1];
        kitManager.saveKit(player, kitName);
        Logger.player(player, ChatColor.GREEN + "Kit saved as " + kitName);
        return true;
    }
}
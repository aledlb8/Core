package dev.aledlb.commands.kit;

import dev.aledlb.features.kits.KitManager;
import dev.aledlb.utilities.Logger;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DeleteKitCommand implements KitSubCommand {
    private final KitManager kitManager;

    public DeleteKitCommand(KitManager kitManager) {
        this.kitManager = kitManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Player player, String[] args) {
        if (!sender.hasPermission("core.kit.delete")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /kit delete <kitName>");
            return true;
        }
        String kitName = args[1];
        kitManager.deleteKit(player, kitName);
        Logger.player(player, ChatColor.GREEN + "Kit deleted: " + kitName);
        return true;
    }
}
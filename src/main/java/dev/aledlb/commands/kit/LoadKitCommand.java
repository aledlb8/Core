package dev.aledlb.commands.kit;

import dev.aledlb.features.kits.KitManager;
import dev.aledlb.utilities.Logger;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LoadKitCommand implements KitSubCommand {
    private final KitManager kitManager;

    public LoadKitCommand(KitManager kitManager) {
        this.kitManager = kitManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Player player, String[] args) {
        if (!sender.hasPermission("core.kit.load")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return true;
        }

        if (args.length < 3) {
            sender.sendMessage(ChatColor.RED + "Usage: /kit load <kitName> <playerName>");
            return true;
        }

        String kitName = args[1];
        Player target = Bukkit.getPlayer(args[2]);

        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Player not found.");
            return true;
        }

        kitManager.loadKitForPlayer(target, kitName);

        Logger.player(player, ChatColor.GREEN + "Kit loaded: " + kitName + " for " + target.getName());
        return true;
    }
}
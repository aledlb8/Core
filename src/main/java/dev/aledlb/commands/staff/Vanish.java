package dev.aledlb.commands.staff;

import dev.aledlb.Core;
import dev.aledlb.utilities.Logger;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Handles the vanish command.
 * This command allows players to toggle their visibility to other players.
 */
public class Vanish implements CommandExecutor {
    
    private final Core plugin;
    
    /**
     * Creates a new Vanish command executor
     * @param plugin The Core plugin instance
     */
    public Vanish(Core plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be run by a player.");
            return true;
        }

        if (!player.hasPermission("core.vanish")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return true;
        }

        if (player.hasMetadata("vanished")) {
            player.removeMetadata("vanished", plugin);
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                onlinePlayer.showPlayer(plugin, player);
                onlinePlayer.setFlying(false);
                onlinePlayer.setAllowFlight(false);
                onlinePlayer.setInvulnerable(false);
            }
            Logger.player(player, ChatColor.GREEN + "Vanish mode disabled");
        } else {
            player.setMetadata("vanished", new org.bukkit.metadata.FixedMetadataValue(plugin, true));
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                onlinePlayer.hidePlayer(plugin, player);
                onlinePlayer.setFlying(true);
                onlinePlayer.setAllowFlight(true);
                onlinePlayer.setInvulnerable(true);
            }
            Logger.player(player, ChatColor.GREEN + "Vanish mode enabled");
        }
        return true;
    }
}

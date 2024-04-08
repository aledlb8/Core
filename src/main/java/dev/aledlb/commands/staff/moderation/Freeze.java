package dev.aledlb.commands.staff.moderation;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;

public class Freeze implements CommandExecutor {
    private JavaPlugin plugin;

    public Freeze(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("core.freeze")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return true;
        }

        if (args.length != 1) {
            sender.sendMessage(ChatColor.RED + "Usage: /freeze <player>");
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Player not found.");
            return true;
        }

        if (cannotBeFrozen(target)) {
            sender.sendMessage(ChatColor.RED + "You can't freeze this player.");
            return true;
        }

        toggleFreezeState(target, sender);
        return true;
    }

    private boolean cannotBeFrozen(Player player) {
        return player.hasPermission("core.freeze.bypass") || player.isOp();
    }

    private void toggleFreezeState(Player target, CommandSender sender) {
        if (isPlayerFrozen(target)) {
            unfreezePlayer(target);
            sender.sendMessage(ChatColor.GREEN + "Player " + target.getName() + " has been unfrozen.");
        } else {
            freezePlayer(target);
            sender.sendMessage(ChatColor.GREEN + "Player " + target.getName() + " has been frozen.");
        }
    }

    private boolean isPlayerFrozen(Player player) {
        for (MetadataValue value : player.getMetadata("frozen")) {
            if (value.getOwningPlugin() == plugin) {
                return value.asBoolean();
            }
        }
        return false;
    }

    private void freezePlayer(Player player) {
        player.setMetadata("frozen", new FixedMetadataValue(plugin, true));
        player.sendTitle(ChatColor.RED + "You have been frozen", ChatColor.RED + "You can't move, don't disconnect or you will be banned", 10, 70, 20);
    }

    private void unfreezePlayer(Player player) {
        player.removeMetadata("frozen", plugin);
        player.sendTitle(ChatColor.GREEN + "You have been unfrozen", ChatColor.GREEN + "You can move again", 10, 70, 20);
    }
}
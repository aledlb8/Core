package dev.aledlb.commands;

import dev.aledlb.utilities.Logger;
import org.bukkit.ChatColor;
import org.bukkit.GameRule;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class KeepInventory implements CommandExecutor {
    private JavaPlugin plugin;

    public KeepInventory(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player player = (Player) sender;

        if (!player.hasPermission("core.keepinvetory")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return true;
        }

        boolean currentValue = this.plugin.getServer().getWorlds().get(0).getGameRuleValue(GameRule.KEEP_INVENTORY);
        boolean newValue = !currentValue;

        this.plugin.getServer().getWorlds().forEach(world -> world.setGameRule(GameRule.KEEP_INVENTORY, newValue));

        if (!newValue) {
            Logger.player(player, ChatColor.RED + "Keep Inventory is now off.");
            return true;
        }

        Logger.player(player, ChatColor.GREEN + "Keep Inventory is now on.");
        return true;
    }
}

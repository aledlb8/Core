package dev.aledlb.commands.staff;

import dev.aledlb.features.enchantments.EnchantmentGUI;
import dev.aledlb.utilities.Logger;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class EnchantGUI implements CommandExecutor {
    private final EnchantmentGUI enchantmentGUI;

    public EnchantGUI(EnchantmentGUI enchantmentGUI) {
        this.enchantmentGUI = enchantmentGUI;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be run by a player.");
            return true;
        }

        if (!player.hasPermission("core.enchantgui")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return true;
        }

        enchantmentGUI.openGUI(player);
        return true;
    }
}

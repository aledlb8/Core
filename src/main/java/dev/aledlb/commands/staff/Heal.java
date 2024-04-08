package dev.aledlb.commands.staff;

import dev.aledlb.utilities.Logger;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Heal implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be run by a player.");
            return true;
        }

        if (!player.hasPermission("core.heal")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return true;
        }

        player.setHealth(player.getMaxHealth());

        player.getActivePotionEffects().forEach(potionEffect -> {
            NegativePotionEffectTypes.valueOf(potionEffect.getType().getName().toUpperCase());
            player.removePotionEffect(potionEffect.getType());
        });

        player.setFireTicks(0);

        Logger.player(player, ChatColor.GREEN + "You have been healed.");

        return true;
    }

    private enum NegativePotionEffectTypes {

        WITHER, SLOW, SLOW_DIGGING, HARM, CONFUSION, DAMAGE_RESISTANCE, BLINDNESS, HUNGER, WEAKNESS, POISON, UNLUCK
    }
}

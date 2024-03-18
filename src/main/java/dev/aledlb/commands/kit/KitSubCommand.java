package dev.aledlb.commands.kit;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public interface KitSubCommand {
    boolean onCommand(CommandSender sender, Player player, String[] args);
}
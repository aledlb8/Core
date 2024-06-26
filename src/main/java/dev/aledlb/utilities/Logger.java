package dev.aledlb.utilities;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public class Logger {
    static FileConfiguration config;
    public Logger(FileConfiguration config) {
        Logger.config = config;
    }

    public static void console(String message) {
        message = "&8[&cCore&8] &f" + message;
        Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', message));
    }

    public static void player(CommandSender sender, String message) {
        sender.sendMessage(config.getString("prefix") + " " + message);
    }
}

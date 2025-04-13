package dev.aledlb.utilities;

import dev.aledlb.Core;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;

/**
 * Enhanced logging utility for the Core plugin.
 * Provides console and file logging with different log levels and formatting.
 */
public class Logger {
    private static final String PREFIX = "&8[&6Core&8]&r ";
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static File logFile;
    private static boolean debugMode;
    private static PrintWriter logWriter;
    private static FileConfiguration config;

    /**
     * Initializes the logger with the plugin configuration
     * @param config The plugin configuration
     */
    public Logger(FileConfiguration config) {
        Logger.config = config;
        debugMode = config.getBoolean("debug-mode", false);
        setupLogFile();
    }

    /**
     * Sets up the log file
     */
    private void setupLogFile() {
        try {
            File logsDir = new File(Core.getInstance().getDataFolder(), "logs");
            if (!logsDir.exists()) {
                logsDir.mkdirs();
            }

            String fileName = "core-" + new SimpleDateFormat("yyyy-MM-dd").format(new Date()) + ".log";
            logFile = new File(logsDir, fileName);
            
            if (!logFile.exists()) {
                logFile.createNewFile();
            }

            logWriter = new PrintWriter(new FileWriter(logFile, true), true);
        } catch (IOException e) {
            Bukkit.getLogger().log(Level.SEVERE, "Failed to setup log file: " + e.getMessage());
        }
    }

    /**
     * Logs a message to the console with the specified level
     * @param level The log level
     * @param message The message to log
     */
    private static void log(Level level, String message) {
        String formattedMessage = colorize(PREFIX + message);
        Bukkit.getLogger().log(level, stripColor(formattedMessage));
        
        if (logWriter != null) {
            logWriter.println(String.format("[%s] [%s] %s",
                DATE_FORMAT.format(new Date()),
                level.getName(),
                stripColor(message)));
        }
    }

    /**
     * Logs an info message to the console
     * @param message The message to log
     */
    public static void info(String message) {
        log(Level.INFO, message);
    }

    /**
     * Logs a warning message to the console
     * @param message The message to log
     */
    public static void warning(String message) {
        log(Level.WARNING, "&e" + message);
    }

    /**
     * Logs a severe error message to the console
     * @param message The message to log
     */
    public static void severe(String message) {
        log(Level.SEVERE, "&c" + message);
    }

    /**
     * Logs a debug message to the console (only if debug mode is enabled)
     * @param message The message to log
     */
    public static void debug(String message) {
        if (debugMode) {
            log(Level.INFO, "&7[DEBUG] " + message);
        }
    }

    /**
     * Logs a message to the console
     * @param message The message to log
     */
    public static void console(String message) {
        info(message);
    }

    /**
     * Converts color codes in a string to ChatColor
     * @param message The message to colorize
     * @return The colorized message
     */
    private static String colorize(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    /**
     * Removes color codes from a string
     * @param message The message to strip colors from
     * @return The stripped message
     */
    private static String stripColor(String message) {
        return message.replaceAll("§[0-9a-fk-or]", "");
    }

    /**
     * Closes the log writer
     */
    public static void close() {
        if (logWriter != null) {
            logWriter.close();
        }
    }

    /**
     * Sends a message to a player with the configured prefix
     * @param sender The command sender to send the message to
     * @param message The message to send
     */
    public static void player(CommandSender sender, String message) {
        String prefix = config != null ? config.getString("prefix", "&8[&6Core&8]&r") : "&8[&6Core&8]&r";
        sender.sendMessage(colorize(prefix + " " + message));
    }
}

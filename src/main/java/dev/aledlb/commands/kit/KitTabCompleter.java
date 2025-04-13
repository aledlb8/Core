package dev.aledlb.commands.kit;

import dev.aledlb.Core;
import dev.aledlb.utilities.Logger;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Provides tab completion for kit-related commands.
 * This class handles suggestions for:
 * - Main kit commands
 * - Kit names for load/delete commands
 * - Player names for load command
 */
public class KitTabCompleter implements TabCompleter {

    private static final String[] MAIN_COMMANDS = {"save", "list", "delete", "load"};
    private final Core plugin;

    /**
     * Creates a new KitTabCompleter instance
     * @param plugin The Core plugin instance
     */
    public KitTabCompleter(Core plugin) {
        this.plugin = plugin;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        try {
            if (args.length == 1) {
                return filterCompletions(MAIN_COMMANDS, args[0]);
            }

            if (args.length == 2 && (args[0].equalsIgnoreCase("delete") || args[0].equalsIgnoreCase("load"))) {
                return getKitNames();
            }

            if (args.length == 3 && args[0].equalsIgnoreCase("load")) {
                return getOnlinePlayerNames(args[2]);
            }

            return new ArrayList<>();
        } catch (Exception e) {
            Logger.severe("Error in tab completion: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Filters completions based on the current input
     * @param completions The list of possible completions
     * @param input The current input
     * @return Filtered list of completions
     */
    private List<String> filterCompletions(String[] completions, String input) {
        return Arrays.stream(completions)
            .filter(completion -> completion.toLowerCase().startsWith(input.toLowerCase()))
            .collect(Collectors.toList());
    }

    /**
     * Gets a list of kit names from the kits folder
     * @return List of kit names
     */
    private List<String> getKitNames() {
        try {
            File kitsFolder = new File(plugin.getDataFolder(), "kits");
            File[] kitFiles = kitsFolder.listFiles();
            List<String> kitNames = new ArrayList<>();

            if (kitFiles != null) {
                for (File file : kitFiles) {
                    if (file.isFile() && file.getName().endsWith(".yml")) {
                        kitNames.add(file.getName().replace(".yml", ""));
                    }
                }
            }

            return kitNames;
        } catch (Exception e) {
            Logger.severe("Error getting kit names: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Gets a list of online player names
     * @param input The current input
     * @return List of matching player names
     */
    private List<String> getOnlinePlayerNames(String input) {
        return Bukkit.getOnlinePlayers().stream()
            .map(Player::getName)
            .filter(name -> name.toLowerCase().startsWith(input.toLowerCase()))
            .collect(Collectors.toList());
    }
}
package dev.aledlb.commands.kit;

import dev.aledlb.Core;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class KitTabCompleter implements TabCompleter {

    final Core main;

    public KitTabCompleter(Core main) {
        this.main = main;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("save", "list", "delete", "load");
        } else if (args.length == 2 && (args[0].equalsIgnoreCase("delete") || args[0].equalsIgnoreCase("load"))) {
            return getKitNames();
        } else if (args.length == 3 && args[0].equalsIgnoreCase("load")) {
            return Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    private List<String> getKitNames() {
        File kitsFolder = new File(main.getDataFolder(), "kits");
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
    }

}
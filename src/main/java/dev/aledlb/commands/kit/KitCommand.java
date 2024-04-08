package dev.aledlb.commands.kit;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import dev.aledlb.features.kits.KitManager;

import java.util.HashMap;
import java.util.Map;

public class KitCommand implements CommandExecutor {
    private final Map<String, KitSubCommand> subCommands = new HashMap<>();
    private final KitManager kitManager;

    public KitCommand(KitManager kitManager) {
        this.kitManager = kitManager;
        registerSubCommands();
    }

    private void registerSubCommands() {
        subCommands.put("save", new SaveKitCommand(kitManager));
        subCommands.put("load", new LoadKitCommand(kitManager));
        subCommands.put("delete", new DeleteKitCommand(kitManager));
        subCommands.put("list", new ListKitCommand(kitManager));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player player = (Player) sender;
        if (sender instanceof Player && args.length == 0) {
            KitManager.showKitsGUI(player);
            return true;
        } else {
            KitSubCommand subCommand = subCommands.get(args[0].toLowerCase());

            return subCommand.onCommand(sender, player, args);
        }
    }
}
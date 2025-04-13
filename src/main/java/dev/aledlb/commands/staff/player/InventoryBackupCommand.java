package dev.aledlb.commands.staff.player;

import dev.aledlb.features.player.PlayerData;
import dev.aledlb.features.player.PlayerDataManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Handles the /invbackup command which manages player inventory backups.
 */
public class InventoryBackupCommand implements CommandExecutor {
    private static final String PERMISSION = "core.invbackup";
    private static final String USAGE = "/invbackup <player> [save|restore|list] [backup_number]";
    private static final String ERROR_NO_PERMISSION = "§cYou don't have permission to use this command.";
    private static final String ERROR_PLAYER_NOT_FOUND = "§cPlayer not found.";
    private static final String ERROR_USAGE = "§cUsage: " + USAGE;
    private static final String ERROR_NO_BACKUPS = "§cNo backups found for this player.";
    private static final String ERROR_INVALID_BACKUP = "§cInvalid backup number.";
    private static final String SUCCESS_SAVE = "§aInventory backup saved.";
    private static final String SUCCESS_RESTORE = "§aInventory restored from backup.";
    private static final String HEADER = "§6=== Inventory Backups ===";
    private static final String BACKUP_FORMAT = "§7%d: §f%s";

    private final PlayerDataManager dataManager;
    private final SimpleDateFormat dateFormat;

    /**
     * Creates a new InventoryBackupCommand instance.
     * @param dataManager The player data manager.
     */
    public InventoryBackupCommand(PlayerDataManager dataManager) {
        this.dataManager = dataManager;
        this.dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission(PERMISSION)) {
            sender.sendMessage(ERROR_NO_PERMISSION);
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage(ERROR_USAGE);
            return true;
        }

        String targetName = args[0];
        Player target = Bukkit.getPlayer(targetName);
        if (target == null) {
            sender.sendMessage(ERROR_PLAYER_NOT_FOUND);
            return true;
        }

        if (args.length == 1) {
            listBackups(sender, target.getUniqueId());
            return true;
        }

        String action = args[1].toLowerCase();
        switch (action) {
            case "save":
                saveBackup(sender, target);
                break;
            case "restore":
                if (args.length < 3) {
                    sender.sendMessage(ERROR_USAGE);
                    return true;
                }
                try {
                    int backupNumber = Integer.parseInt(args[2]);
                    restoreBackup(sender, target, backupNumber);
                } catch (NumberFormatException e) {
                    sender.sendMessage(ERROR_INVALID_BACKUP);
                }
                break;
            case "list":
                listBackups(sender, target.getUniqueId());
                break;
            default:
                sender.sendMessage(ERROR_USAGE);
        }

        return true;
    }

    /**
     * Saves a player's inventory backup.
     * @param sender The command sender.
     * @param player The player to backup.
     */
    private void saveBackup(CommandSender sender, Player player) {
        PlayerData data = dataManager.getPlayerData(player.getUniqueId());
        data.addInventoryBackup(player.getInventory().getContents());
        dataManager.savePlayerData(data);
        sender.sendMessage(SUCCESS_SAVE);
    }

    /**
     * Restores a player's inventory from a backup.
     * @param sender The command sender.
     * @param player The player to restore.
     * @param backupNumber The backup number to restore.
     */
    private void restoreBackup(CommandSender sender, Player player, int backupNumber) {
        PlayerData data = dataManager.getPlayerData(player.getUniqueId());
        List<ItemStack[]> backups = data.getInventoryBackups();
        
        if (backups.isEmpty()) {
            sender.sendMessage(ERROR_NO_BACKUPS);
            return;
        }

        if (backupNumber < 0 || backupNumber >= backups.size()) {
            sender.sendMessage(ERROR_INVALID_BACKUP);
            return;
        }

        player.getInventory().setContents(backups.get(backupNumber));
        sender.sendMessage(SUCCESS_RESTORE);
    }

    /**
     * Lists a player's inventory backups.
     * @param sender The command sender.
     * @param uuid The player's UUID.
     */
    private void listBackups(CommandSender sender, UUID uuid) {
        PlayerData data = dataManager.getPlayerData(uuid);
        List<ItemStack[]> backups = data.getInventoryBackups();
        
        if (backups.isEmpty()) {
            sender.sendMessage(ERROR_NO_BACKUPS);
            return;
        }

        sender.sendMessage(HEADER);
        for (int i = 0; i < backups.size(); i++) {
            String timestamp = dateFormat.format(new Date(data.getLastBackup()));
            sender.sendMessage(String.format(BACKUP_FORMAT, i, timestamp));
        }
    }
} 
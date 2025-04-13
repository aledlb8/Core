package dev.aledlb;

import dev.aledlb.commands.KeepInventory;
import dev.aledlb.commands.kit.KitTabCompleter;
import dev.aledlb.commands.permissions.PermissionCommands;
import dev.aledlb.commands.permissions.PermissionTabCompleter;
import dev.aledlb.commands.staff.item.AddLore;
import dev.aledlb.commands.staff.item.RemoveLore;
import dev.aledlb.commands.staff.item.Rename;
import dev.aledlb.commands.staff.moderation.*;
import dev.aledlb.commands.staff.player.*;
import dev.aledlb.features.motd.MOTDManager;
import dev.aledlb.features.placeholder.CorePlaceholderExpansion;
import dev.aledlb.features.moderation.ChatFilter;
import dev.aledlb.features.moderation.TempBanManager;
import dev.aledlb.features.moderation.WarningManager;
import dev.aledlb.listeners.ChatListener;
import dev.aledlb.utilities.ConfigManager;
import dev.aledlb.utilities.PermissionManager;
import net.milkbowl.vault.economy.Economy;

import dev.aledlb.commands.staff.gamemode.GMA;
import dev.aledlb.commands.staff.gamemode.GMC;
import dev.aledlb.commands.staff.gamemode.GMS;
import dev.aledlb.commands.staff.gamemode.GMSP;
import dev.aledlb.commands.kit.KitCommand;
import dev.aledlb.commands.staff.*;
import dev.aledlb.features.enchantments.EnchantmentGUI;
import dev.aledlb.features.kits.KitManager;
import dev.aledlb.listeners.PlayerEvent;
import dev.aledlb.utilities.Logger;
import dev.aledlb.utilities.UpdateChecker;
import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import dev.aledlb.features.player.PlayerDataManager;

/**
 * Core plugin class for managing server functionality.
 * This plugin provides various features including:
 * - Permission management
 * - Economy integration
 * - Kit system
 * - Staff commands
 * - Player management
 * - Chat filtering
 * - Warning system
 * - Temporary bans
 */
public final class Core extends JavaPlugin implements CommandExecutor, Listener {

    private static final int RESOURCE_ID = 12345;
    private static final Pattern VERSION_PATTERN = Pattern.compile("^1\\.(\\d*)\\.");
    
    // Plugin instance
    private static Core instance;
    
    // Configuration
    private ConfigManager configManager;
    
    // Feature managers
    private EnchantmentGUI enchantGUI;
    private KitManager kitManager;
    private MOTDManager motdManager;
    private ChatFilter chatFilter;
    private TempBanManager tempBanManager;
    private WarningManager warningManager;
    
    // Economy
    private static Economy economy;
    
    // Permission management
    private PermissionManager permissionManager;
    
    // Player data management
    private PlayerDataManager dataManager;
    
    // Plugin state
    private boolean isEnabled = false;

    @Override
    public void onEnable() {
        instance = this;
        initializePlugin();
    }

    /**
     * Initializes the plugin and all its components
     */
    private void initializePlugin() {
        try {
            // Initialize configuration
            configManager = new ConfigManager(this);
            
            // Initialize logger
            new Logger(configManager.getConfig());
            Logger.console("Initializing Core plugin...");
            
            // Initialize managers
            initializeManagers();
            
            // Setup dependencies
            setupDependencies();
            
            // Register events and commands
            registerEventsAndCommands();
            
            // Add permissions to online players
            permissionManager.addPermissionsToOnlinePlayers();
            
            // Check for updates
            checkForUpdates();
            
            isEnabled = true;
            logStartupInfo();
            
        } catch (Exception e) {
            Logger.severe("Failed to initialize plugin: " + e.getMessage());
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    /**
     * Initializes all plugin managers
     */
    private void initializeManagers() {
        enchantGUI = new EnchantmentGUI(this);
        kitManager = new KitManager(this);
        motdManager = new MOTDManager(this);
        permissionManager = new PermissionManager(this);
        dataManager = new PlayerDataManager(this);
        chatFilter = new ChatFilter(this);
        tempBanManager = new TempBanManager(this);
        warningManager = new WarningManager(this);
    }

    /**
     * Sets up plugin dependencies
     */
    private void setupDependencies() {
        setupEconomy();
        setupPlaceholderAPI();
    }

    /**
     * Sets up economy integration
     */
    private void setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") != null) {
            RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
            if (rsp != null) {
                economy = rsp.getProvider();
                Logger.console("Economy integration successful");
            } else {
                Logger.warning("Failed to initialize economy - no provider found");
            }
        } else {
            Logger.warning("Vault not found - economy features disabled");
        }
    }

    /**
     * Sets up PlaceholderAPI integration
     */
    private void setupPlaceholderAPI() {
        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new CorePlaceholderExpansion(this).register();
            Logger.console("PlaceholderAPI integration successful");
        } else {
            Logger.warning("PlaceholderAPI not found - placeholders disabled");
        }
    }

    /**
     * Registers all events and commands
     */
    private void registerEventsAndCommands() {
        // Register events
        getServer().getPluginManager().registerEvents(this, this);
        getServer().getPluginManager().registerEvents(motdManager, this);
        getServer().getPluginManager().registerEvents(new PlayerEvent(this), this);
        getServer().getPluginManager().registerEvents(new ChatListener(dataManager), this);
        getServer().getPluginManager().registerEvents(chatFilter, this);

        // Register commands
        registerCommands();
    }

    /**
     * Registers all plugin commands
     */
    private void registerCommands() {
        // Permission commands
        getCommand("permission").setTabCompleter(new PermissionTabCompleter(this));
        getCommand("permission").setExecutor(new PermissionCommands(this));

        // Core commands
        getCommand("core").setExecutor(this);

        // GameMode commands
        registerGameModeCommands();

        // Staff commands
        registerStaffCommands();

        // Kit commands
        getCommand("kit").setTabCompleter(new KitTabCompleter(this));
        getCommand("kit").setExecutor(new KitCommand(kitManager));

        // Moderation commands
        registerModerationCommands();

        // Player commands
        registerPlayerCommands();
    }

    /**
     * Registers gamemode commands
     */
    private void registerGameModeCommands() {
        getCommand("gmc").setExecutor(new GMC());
        getCommand("gms").setExecutor(new GMS());
        getCommand("gma").setExecutor(new GMA());
        getCommand("gmsp").setExecutor(new GMSP());
    }

    /**
     * Registers staff commands
     */
    private void registerStaffCommands() {
        getCommand("freeze").setExecutor(new Freeze(this));
        getCommand("mute").setExecutor(new Mute(this));
        getCommand("unmute").setExecutor(new Unmute(this));
        getCommand("fly").setExecutor(new Fly());
        getCommand("vanish").setExecutor(new Vanish(this));
        getCommand("enchantgui").setExecutor(new EnchantGUI(enchantGUI));
        getCommand("broadcast").setExecutor(new Broadcast());
        getCommand("feed").setExecutor(new Feed());
        getCommand("heal").setExecutor(new Heal());
        getCommand("more").setExecutor(new More());
        getCommand("rename").setExecutor(new Rename());
        getCommand("addlore").setExecutor(new AddLore());
        getCommand("removelore").setExecutor(new RemoveLore());
        getCommand("keepinventory").setExecutor(new KeepInventory(this));
    }

    /**
     * Registers moderation commands
     */
    private void registerModerationCommands() {
        getCommand("tempban").setExecutor(new TempBanCommand(tempBanManager));
        getCommand("unban").setExecutor(new UnbanCommand(tempBanManager));
        getCommand("warn").setExecutor(new WarnCommand(warningManager));
        getCommand("unwarn").setExecutor(new UnwarnCommand(warningManager));
        getCommand("chatfilter").setExecutor(new ChatFilterCommand(chatFilter));
    }

    /**
     * Registers player commands
     */
    private void registerPlayerCommands() {
        getCommand("chathistory").setExecutor(new ChatHistoryCommand(dataManager));
        getCommand("inventorybackup").setExecutor(new InventoryBackupCommand(dataManager));
        getCommand("location").setExecutor(new LocationHistoryCommand(dataManager));
        getCommand("playerstats").setExecutor(new PlayerStatsCommand(dataManager));
        getCommand("keepinventory").setExecutor(new KeepInventory(this));
    }

    /**
     * Checks for plugin updates
     */
    private void checkForUpdates() {
        if (configManager.getConfig().getBoolean("check-for-updates", true)) {
            String currentVersion = getDescription().getVersion();
            UpdateChecker updateChecker = new UpdateChecker(RESOURCE_ID, currentVersion);
            
            if (updateChecker.isUpToDate()) {
                Logger.console("You are running the latest version of Core.");
            } else {
                Logger.console("There is a new version of Core available.");
                Logger.console("Download at: https://www.spigotmc.org/resources/" + RESOURCE_ID);
            }
        }
    }

    /**
     * Logs startup information
     */
    private void logStartupInfo() {
        Logger.console("&4==========&7=======&7[&a&lActivated&7]=======&4==========");
        Logger.console("&7Core Plugin has been enabled");
        Logger.console("");
        Logger.console("&7Version: &6" + getDescription().getVersion());
        Logger.console("&4==========&7=======&7[&a&lActivated&7]=======&4==========");
        Logger.console("&4==========&7===================================&4==========");
        Logger.console("&eName: &6" + getDescription().getName());
        Logger.console("&eAuthor: &6" + getDescription().getAuthors());
        Logger.console("&4==========&7===================================&4==========");
    }

    @Override
    public void onDisable() {
        if (!isEnabled) return;
        
        try {
            permissionManager.removeAllPermissions();
            configManager.saveConfigs();
            HandlerList.unregisterAll((Plugin) this);
            Logger.console("Core plugin has been disabled");
            Logger.close();
        } catch (Exception e) {
            Logger.severe("Error during plugin shutdown: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        permissionManager.addPermissions(event.getPlayer());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        permissionManager.removePermissions(event.getPlayer());
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("core")) {
            sender.sendMessage(ChatColor.DARK_AQUA + "====== " + ChatColor.GREEN + "Core Plugin" + ChatColor.DARK_AQUA + " ======");
            sender.sendMessage(ChatColor.AQUA + "Developed by: " + ChatColor.YELLOW + "aledlb");
            sender.sendMessage("");

            // Gamemode Commands
            sender.sendMessage(ChatColor.GREEN + "Gamemode Commands:");
            sender.sendMessage(ChatColor.AQUA + "/gmc" + ChatColor.WHITE + " - Change your gamemode to Creative");
            sender.sendMessage(ChatColor.AQUA + "/gms" + ChatColor.WHITE + " - Change your gamemode to Survival");
            sender.sendMessage(ChatColor.AQUA + "/gma" + ChatColor.WHITE + " - Change your gamemode to Adventure");
            sender.sendMessage(ChatColor.AQUA + "/gmsp" + ChatColor.WHITE + " - Change your gamemode to Spectator");

            // Staff Commands
            sender.sendMessage(ChatColor.GREEN + "Staff Commands:");
            sender.sendMessage(ChatColor.AQUA + "/freeze <player>" + ChatColor.WHITE + " - Freeze a player");
            sender.sendMessage(ChatColor.AQUA + "/mute <player>" + ChatColor.WHITE + " - Mute a player");
            sender.sendMessage(ChatColor.AQUA + "/unmute <player>" + ChatColor.WHITE + " - Unmute a player");
            sender.sendMessage(ChatColor.AQUA + "/fly" + ChatColor.WHITE + " - Enable or disable flight mode");
            sender.sendMessage(ChatColor.AQUA + "/vanish" + ChatColor.WHITE + " - Enable or disable vanish mode");
            sender.sendMessage(ChatColor.AQUA + "/enchantgui" + ChatColor.WHITE + " - Open the enchantment GUI");

            // Kit Commands
            sender.sendMessage(ChatColor.GREEN + "Kit Commands:");
            sender.sendMessage(ChatColor.AQUA + "/kit" + ChatColor.WHITE + " - Open the kit GUI");
            sender.sendMessage(ChatColor.AQUA + "/kit save <kitName>" + ChatColor.WHITE + " - Save your current inventory as a kit");
            sender.sendMessage(ChatColor.AQUA + "/kit load <kitName> <playerName>" + ChatColor.WHITE + " - Load a kit");
            sender.sendMessage(ChatColor.AQUA + "/kit delete <kitName>" + ChatColor.WHITE + " - Delete a kit");
            sender.sendMessage(ChatColor.AQUA + "/kit list" + ChatColor.WHITE + " - List all available kits");

            // Moderation Commands
            sender.sendMessage(ChatColor.GREEN + "Moderation Commands:");
            sender.sendMessage(ChatColor.AQUA + "/tempban <player> <duration> <reason>" + ChatColor.WHITE + " - Temporarily ban a player");
            sender.sendMessage(ChatColor.AQUA + "/unban <player>" + ChatColor.WHITE + " - Unban a player");
            sender.sendMessage(ChatColor.AQUA + "/warn <player> <reason>" + ChatColor.WHITE + " - Warn a player");
            sender.sendMessage(ChatColor.AQUA + "/unwarn <player>" + ChatColor.WHITE + " - Unwarn a player");
            sender.sendMessage(ChatColor.AQUA + "/chatfilter" + ChatColor.WHITE + " - Manage chat filtering");

            // Player Commands
            sender.sendMessage(ChatColor.GREEN + "Player Commands:");
            sender.sendMessage(ChatColor.AQUA + "/chathistory" + ChatColor.WHITE + " - View chat history");
            sender.sendMessage(ChatColor.AQUA + "/inventorybackup" + ChatColor.WHITE + " - Backup your inventory");
            sender.sendMessage(ChatColor.AQUA + "/location" + ChatColor.WHITE + " - View your location history");
            sender.sendMessage(ChatColor.AQUA + "/playerstats" + ChatColor.WHITE + " - View player statistics");

            sender.sendMessage(ChatColor.DARK_AQUA + "=================================");

            return true;
        }

        return false;
    }

    public static int getMcVersion() {
        String bukkitVersionString = Bukkit.getBukkitVersion();
        Matcher m = VERSION_PATTERN.matcher((bukkitVersionString));
        int version = -1;
        while (m.find()) {
            if (NumberUtils.isNumber(m.group(1)))
                version = Integer.parseInt(m.group(1));
        }
        return version;
    }

    /**
     * Gets the plugin instance
     * @return Core plugin instance
     */
    public static Core getInstance() {
        return instance;
    }

    /**
     * Gets the economy instance
     * @return Economy instance or null if not available
     */
    public static Economy getEconomy() {
        return economy;
    }

    /**
     * Checks if the plugin is fully enabled
     * @return true if plugin is enabled and initialized
     */
    public boolean isPluginEnabled() {
        return isEnabled;
    }

    /**
     * Gets the permission manager
     * @return The permission manager instance
     */
    public PermissionManager getPermissionManager() {
        return permissionManager;
    }

    /**
     * Gets the configuration manager
     * @return The configuration manager instance
     */
    public ConfigManager getConfigManager() {
        return configManager;
    }
    
    /**
     * Checks if a string is a valid integer
     * @param str The string to check
     * @return true if the string is a valid integer, false otherwise
     */
    public static boolean isInteger(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
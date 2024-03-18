package dev.aledlb;

import dev.aledlb.commands.kit.KitTabCompleter;
import dev.aledlb.commands.permissions.PermissionCommands;
import dev.aledlb.commands.permissions.PermissionTabCompleter;
import dev.aledlb.features.motd.MOTDManager;
import dev.aledlb.features.placeholder.CorePlaceholderExpansion;
import net.milkbowl.vault.economy.Economy;

import dev.aledlb.commands.gamemode.GMA;
import dev.aledlb.commands.gamemode.GMC;
import dev.aledlb.commands.gamemode.GMS;
import dev.aledlb.commands.gamemode.GMSP;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Core extends JavaPlugin implements CommandExecutor, Listener {

    Plugin plugin = this;
    FileConfiguration config = this.getConfig();
    EnchantmentGUI enchantGUI;
    KitManager kitManager;

    private static Economy economy = null;

    public static HashMap<UUID, PermissionAttachment> permissions;


    @Override
    public void onEnable() {
        new Logger(config);

        Logger.console("has been enabled");
        saveDefaultConfig();

        permissions = new HashMap<>();

        Plugin[] plugins = Bukkit.getPluginManager().getPlugins();
        for (Plugin plugin : plugins) {
            if (plugin.getName().equals("Vault")) {
                Logger.console("Vault found, enabling economy features");
                setupEconomy();
                break;
            }

            if (plugin.getName().equals("PlaceholderAPI")) {
                Logger.console("PlaceholderAPI found, enabling placeholders");
                new CorePlaceholderExpansion(this).register();
                break;
            }
        }

//        if (!setupEconomy() ) {
//            Logger.console("No Vault dependency found!");
//            Logger.console("Disabling economy features");
//            //getServer().getPluginManager().disablePlugin(this);
//            return;
//        }

//        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
//
//            Logger.console("PlaceholderAPI found, enabling placeholders");
//        }

        getServer().getPluginManager().registerEvents(new MOTDManager(this), this);

        getServer().getPluginManager().registerEvents(this, this);
        enchantGUI = new EnchantmentGUI(this);
        kitManager = new KitManager(this);
        new PlayerEvent(this);

        // Permission
        getCommand("permission").setTabCompleter(new PermissionTabCompleter(this));
        getCommand("permission").setExecutor(new PermissionCommands(this));

        // Core
        getCommand("core").setExecutor(this);

        // GameMode
        getCommand("gmc").setExecutor(new GMC());
        getCommand("gms").setExecutor(new GMS());
        getCommand("gma").setExecutor(new GMA());
        getCommand("gmsp").setExecutor(new GMSP());

        // Staff
        getCommand("freeze").setExecutor(new Freeze());
        getCommand("mute").setExecutor(new Mute());
        getCommand("unmute").setExecutor(new Unmute());
        getCommand("fly").setExecutor(new Fly());
        getCommand("vanish").setExecutor(new Vanish());
        getCommand("enchantgui").setExecutor(new EnchantGUI(enchantGUI));

        // Kit
        getCommand("kit").setTabCompleter(new KitTabCompleter(this));
        getCommand("kit").setExecutor(new KitCommand(kitManager));

        addPermsToOnlinePlayers();


        if (config.getBoolean("check-for-updates")) {
            int resourceId = 12345;
            String currentVersion = getDescription().getVersion();
            UpdateChecker updateChecker = new UpdateChecker(resourceId, currentVersion);
            if (updateChecker.isUpToDate()) {
                Logger.console("You are running the latest version of Core.");
            } else {
                Logger.console("There is a new version of Core available.");
                Logger.console("You can download it at: https://www.spigotmc.org/resources/" + resourceId);
            }
        }
    }

    @Override
    public void onDisable() {
        removePermissions();
        saveConfig();
        HandlerList.unregisterAll((Plugin) this);
        Logger.console("has been disabled");
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        economy = rsp.getProvider();
        return true;
    }

    static int getMcVersion() {
        String bukkitVersionString = Bukkit.getBukkitVersion();
        Pattern p = Pattern.compile("^1\\.(\\d*)\\.");
        Matcher m = p.matcher((bukkitVersionString));
        int version = -1;
        while (m.find()) {
            if (NumberUtils.isNumber(m.group(1)))
                version = Integer.parseInt(m.group(1));
        }
        return version;
    }

    private void addPermsToOnlinePlayers() {
        for (Player player : getServer().getOnlinePlayers()) {
            if (player != null)
                addPermissions(player);
        }
    }

    public void addPermissions(Player p) {
        PermissionAttachment attachment = p.addAttachment(this);

        for (String permission : config.getStringList("default.permissions")) {
            addPermission(attachment, permission);
            //System.out.println("Adding " + permission + " to " + p.getName() + " because default");
        }

        for (String group : getConfig().getStringList("users." + p.getName() + ".groups")) {
            System.out.println("User " + p.getName() + " is in group " + group);
            for (String perm : getConfig().getStringList("groups." + group + ".permissions")) {
                addPermission(attachment, perm);
                //System.out.println(" Adding " + perm + " to " + p.getName() + " because group " + group);
            }
            for (String parent : getConfig().getStringList("groups." + group + ".parents"))
                for (String perm : getConfig().getStringList("groups." + parent + ".permissions")) {
                    addPermission(attachment, perm);
                    //System.out.println(" Adding " + perm + " to " + p.getName() + " because group " + group);
                }
        }

        for (String perm : getConfig().getStringList("users." + p.getName() + ".permissions")) {
            addPermission(attachment, perm);
            //System.out.println("Adding " + perm + " to " + p.getName() + " because player");
        }

        permissions.put(p.getUniqueId(), attachment);
        if (getMcVersion()>=13) {
            p.updateCommands();
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        addPermissions(event.getPlayer());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        PermissionAttachment attachment = permissions.get(event.getPlayer().getUniqueId());
        if(attachment != null) {
            try {
                event.getPlayer().removeAttachment(attachment);
            } catch (Throwable ignored) {

            }
        }
    }

    private void addPermission(PermissionAttachment attachment, String permission) {
        boolean positive = !permission.startsWith("-");
        if (!positive) {
            permission = permission.substring(1);
        }
        attachment.setPermission(permission, positive);
    }

    void removePermissions() {
        for (Player player : getServer().getOnlinePlayers()) {
            PermissionAttachment attachment = permissions.get(player.getUniqueId());
            player.removeAttachment(attachment);
        }
    }

    public void reloadPermissions() {
        saveConfig();
        reloadPermissionsWithoutSaving();
    }

    public void reloadPermissionsWithoutSaving() {
        removePermissions();
        reloadConfig();
        addPermsToOnlinePlayers();
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

            sender.sendMessage(ChatColor.DARK_AQUA + "=================================");

            return true;
        }

        return false;
    }

//    public static Economy getEconomy() {
//        return economy;
//    }
//
//    public static Permission getPermissions() {
//        return permission;
//    }
//
//    public static Chat getChat() {
//        return chat;
//    }
}
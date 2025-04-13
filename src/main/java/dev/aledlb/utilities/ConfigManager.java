package dev.aledlb.utilities;

import dev.aledlb.Core;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Manages plugin configuration files.
 * Handles loading, saving, and accessing configuration data.
 */
public class ConfigManager {
    private final Core plugin;
    private final Map<String, FileConfiguration> configs;
    private final Map<String, File> configFiles;

    public ConfigManager(Core plugin) {
        this.plugin = plugin;
        this.configs = new HashMap<>();
        this.configFiles = new HashMap<>();
        loadConfigs();
    }

    /**
     * Loads all configuration files
     */
    private void loadConfigs() {
        // Load main config
        plugin.saveDefaultConfig();
        configs.put("config", plugin.getConfig());
        configFiles.put("config", new File(plugin.getDataFolder(), "config.yml"));

        // Load messages config
        File messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        if (!messagesFile.exists()) {
            plugin.saveResource("messages.yml", false);
        }
        configs.put("messages", YamlConfiguration.loadConfiguration(messagesFile));
        configFiles.put("messages", messagesFile);

        // Load kits config
        File kitsFile = new File(plugin.getDataFolder(), "kits.yml");
        if (!kitsFile.exists()) {
            plugin.saveResource("kits.yml", false);
        }
        configs.put("kits", YamlConfiguration.loadConfiguration(kitsFile));
        configFiles.put("kits", kitsFile);
    }

    /**
     * Gets a configuration by name
     * @param name The name of the configuration
     * @return The configuration or null if not found
     */
    public FileConfiguration getConfig(String name) {
        return configs.get(name);
    }

    /**
     * Gets the main configuration
     * @return The main configuration
     */
    public FileConfiguration getConfig() {
        return getConfig("config");
    }

    /**
     * Saves a configuration by name
     * @param name The name of the configuration to save
     */
    public void saveConfig(String name) {
        try {
            FileConfiguration config = configs.get(name);
            File configFile = configFiles.get(name);
            if (config != null && configFile != null) {
                config.save(configFile);
            }
        } catch (IOException e) {
            Logger.severe("Failed to save config " + name + ": " + e.getMessage());
        }
    }

    /**
     * Saves all configurations
     */
    public void saveConfigs() {
        for (String name : configs.keySet()) {
            saveConfig(name);
        }
    }

    /**
     * Reloads a configuration by name
     * @param name The name of the configuration to reload
     */
    public void reloadConfig(String name) {
        File configFile = configFiles.get(name);
        if (configFile != null) {
            configs.put(name, YamlConfiguration.loadConfiguration(configFile));
        }
    }

    /**
     * Reloads all configurations
     */
    public void reloadConfigs() {
        plugin.reloadConfig();
        configs.put("config", plugin.getConfig());
        
        for (String name : configFiles.keySet()) {
            if (!name.equals("config")) {
                reloadConfig(name);
            }
        }
    }

    /**
     * Gets a string from the messages configuration
     * @param path The path to the message
     * @return The message or null if not found
     */
    public String getMessage(String path) {
        return getConfig("messages").getString(path);
    }

    /**
     * Gets a string from the messages configuration with color codes
     * @param path The path to the message
     * @return The colored message or null if not found
     */
    public String getColoredMessage(String path) {
        String message = getMessage(path);
        return message != null ? message.replace('&', '§') : null;
    }
} 
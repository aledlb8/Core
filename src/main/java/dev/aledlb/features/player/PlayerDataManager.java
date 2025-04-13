package dev.aledlb.features.player;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Manages player data including inventory backups, statistics, and location history.
 */
public class PlayerDataManager {
    private final JavaPlugin plugin;
    private final Map<UUID, PlayerData> playerDataMap;
    private final File dataFolder;
    private final FileConfiguration config;

    /**
     * Creates a new PlayerDataManager instance.
     * @param plugin The plugin instance.
     */
    public PlayerDataManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.playerDataMap = new HashMap<>();
        this.dataFolder = new File(plugin.getDataFolder(), "playerdata");
        this.config = plugin.getConfig();
        
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
    }

    /**
     * Loads player data from file.
     * @param uuid The player's UUID.
     * @return The player's data.
     */
    public PlayerData loadPlayerData(UUID uuid) {
        File playerFile = new File(dataFolder, uuid.toString() + ".yml");
        if (!playerFile.exists()) {
            return new PlayerData(uuid);
        }

        FileConfiguration playerConfig = YamlConfiguration.loadConfiguration(playerFile);
        PlayerData data = new PlayerData(uuid);
        
        // Load statistics
        data.setPlaytime(playerConfig.getLong("statistics.playtime", 0));
        data.setDeaths(playerConfig.getInt("statistics.deaths", 0));
        data.setKills(playerConfig.getInt("statistics.kills", 0));
        
        // Load last seen
        data.setLastSeen(playerConfig.getLong("lastSeen", System.currentTimeMillis()));
        
        // Load location history
        data.setLastLocation(playerConfig.getLocation("lastLocation"));
        
        // Load chat history
        if (playerConfig.contains("chatHistory")) {
            data.getChatHistory().addAll(playerConfig.getStringList("chatHistory"));
        }
        
        return data;
    }

    /**
     * Saves player data to file.
     * @param data The player data to save.
     */
    public void savePlayerData(PlayerData data) {
        File playerFile = new File(dataFolder, data.getUuid().toString() + ".yml");
        FileConfiguration playerConfig = new YamlConfiguration();
        
        // Save statistics
        playerConfig.set("statistics.playtime", data.getPlaytime());
        playerConfig.set("statistics.deaths", data.getDeaths());
        playerConfig.set("statistics.kills", data.getKills());
        
        // Save last seen
        playerConfig.set("lastSeen", data.getLastSeen());
        
        // Save location history
        if (data.getLastLocation() != null) {
            playerConfig.set("lastLocation", data.getLastLocation());
        }
        
        // Save chat history
        playerConfig.set("chatHistory", data.getChatHistory());
        
        try {
            playerConfig.save(playerFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save player data for " + data.getUuid());
            e.printStackTrace();
        }
    }

    /**
     * Saves player data asynchronously.
     * @param uuid The player's UUID.
     */
    public void savePlayerDataAsync(UUID uuid) {
        PlayerData data = getPlayerData(uuid);
        if (data != null) {
            plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> savePlayerData(data));
        }
    }

    /**
     * Gets or creates player data.
     * @param uuid The player's UUID.
     * @return The player's data.
     */
    public PlayerData getPlayerData(UUID uuid) {
        return playerDataMap.computeIfAbsent(uuid, this::loadPlayerData);
    }

    /**
     * Updates player data for a player.
     * @param player The player to update data for.
     */
    public void updatePlayerData(Player player) {
        PlayerData data = getPlayerData(player.getUniqueId());
        data.setLastSeen(System.currentTimeMillis());
        data.setLastLocation(player.getLocation());
        savePlayerData(data);
    }

    /**
     * Saves all player data.
     */
    public void saveAllPlayerData() {
        playerDataMap.values().forEach(this::savePlayerData);
    }
} 
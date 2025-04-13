package dev.aledlb.features.player;

import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Stores player data including statistics, inventory backups, and location history.
 */
public class PlayerData {
    private final UUID uuid;
    private long playtime;
    private int deaths;
    private int kills;
    private long lastSeen;
    private Location lastLocation;
    private List<Location> locationHistory;
    private List<ItemStack[]> inventoryBackups;
    private long lastBackup;
    private List<String> chatHistory;

    /**
     * Creates a new PlayerData instance.
     * @param uuid The player's UUID.
     */
    public PlayerData(UUID uuid) {
        this.uuid = uuid;
        this.playtime = 0;
        this.deaths = 0;
        this.kills = 0;
        this.lastSeen = System.currentTimeMillis();
        this.locationHistory = new ArrayList<>();
        this.inventoryBackups = new ArrayList<>();
        this.lastBackup = 0;
        this.chatHistory = new ArrayList<>();
    }

    /**
     * Gets the player's UUID.
     * @return The player's UUID.
     */
    public UUID getUuid() {
        return uuid;
    }

    /**
     * Gets the player's total playtime in milliseconds.
     * @return The player's playtime.
     */
    public long getPlaytime() {
        return playtime;
    }

    /**
     * Sets the player's playtime.
     * @param playtime The new playtime value.
     */
    public void setPlaytime(long playtime) {
        this.playtime = playtime;
    }

    /**
     * Gets the player's death count.
     * @return The player's death count.
     */
    public int getDeaths() {
        return deaths;
    }

    /**
     * Sets the player's death count.
     * @param deaths The new death count.
     */
    public void setDeaths(int deaths) {
        this.deaths = deaths;
    }

    /**
     * Gets the player's kill count.
     * @return The player's kill count.
     */
    public int getKills() {
        return kills;
    }

    /**
     * Sets the player's kill count.
     * @param kills The new kill count.
     */
    public void setKills(int kills) {
        this.kills = kills;
    }

    /**
     * Gets the timestamp when the player was last seen.
     * @return The last seen timestamp.
     */
    public long getLastSeen() {
        return lastSeen;
    }

    /**
     * Sets the last seen timestamp.
     * @param lastSeen The new last seen timestamp.
     */
    public void setLastSeen(long lastSeen) {
        this.lastSeen = lastSeen;
    }

    /**
     * Gets the player's last known location.
     * @return The last location.
     */
    public Location getLastLocation() {
        return lastLocation;
    }

    /**
     * Sets the player's last known location.
     * @param lastLocation The new last location.
     */
    public void setLastLocation(Location lastLocation) {
        this.lastLocation = lastLocation;
        if (lastLocation != null) {
            this.locationHistory.add(lastLocation);
            // Keep only the last 10 locations
            if (this.locationHistory.size() > 10) {
                this.locationHistory.remove(0);
            }
        }
    }

    /**
     * Gets the player's location history.
     * @return The location history.
     */
    public List<Location> getLocationHistory() {
        return new ArrayList<>(locationHistory);
    }

    /**
     * Adds an inventory backup.
     * @param inventory The inventory to backup.
     */
    public void addInventoryBackup(ItemStack[] inventory) {
        this.inventoryBackups.add(inventory.clone());
        this.lastBackup = System.currentTimeMillis();
        // Keep only the last 5 backups
        if (this.inventoryBackups.size() > 5) {
            this.inventoryBackups.remove(0);
        }
    }

    /**
     * Gets the player's inventory backups.
     * @return The inventory backups.
     */
    public List<ItemStack[]> getInventoryBackups() {
        return new ArrayList<>(inventoryBackups);
    }

    /**
     * Gets the timestamp of the last inventory backup.
     * @return The last backup timestamp.
     */
    public long getLastBackup() {
        return lastBackup;
    }

    /**
     * Gets the player's chat history.
     * @return The chat history.
     */
    public List<String> getChatHistory() {
        return new ArrayList<>(chatHistory);
    }

    /**
     * Adds a message to the player's chat history.
     * @param message The message to add.
     */
    public void addChatMessage(String message) {
        this.chatHistory.add(message);
        // Keep only the last 100 messages
        if (this.chatHistory.size() > 100) {
            this.chatHistory.remove(0);
        }
    }

    /**
     * Clears the player's chat history.
     */
    public void clearChatHistory() {
        this.chatHistory.clear();
    }
} 
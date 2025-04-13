package dev.aledlb.features.player;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Listens for player events and updates their data accordingly.
 */
public class PlayerDataListener implements Listener {
    private final JavaPlugin plugin;
    private final PlayerDataManager dataManager;
    private final Map<UUID, Long> joinTimes;

    /**
     * Creates a new PlayerDataListener instance.
     * @param plugin The plugin instance.
     * @param dataManager The player data manager.
     */
    public PlayerDataListener(JavaPlugin plugin, PlayerDataManager dataManager) {
        this.plugin = plugin;
        this.dataManager = dataManager;
        this.joinTimes = new HashMap<>();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        joinTimes.put(player.getUniqueId(), System.currentTimeMillis());
        dataManager.updatePlayerData(player);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        Long joinTime = joinTimes.remove(player.getUniqueId());
        if (joinTime != null) {
            long playtime = System.currentTimeMillis() - joinTime;
            PlayerData data = dataManager.getPlayerData(player.getUniqueId());
            data.setPlaytime(data.getPlaytime() + playtime);
            dataManager.savePlayerData(data);
        }
        dataManager.updatePlayerData(player);
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        PlayerData data = dataManager.getPlayerData(player.getUniqueId());
        data.setDeaths(data.getDeaths() + 1);
        
        // Check if the death was caused by another player
        if (event.getEntity().getKiller() != null) {
            Player killer = event.getEntity().getKiller();
            PlayerData killerData = dataManager.getPlayerData(killer.getUniqueId());
            killerData.setKills(killerData.getKills() + 1);
            dataManager.savePlayerData(killerData);
        }
        
        dataManager.savePlayerData(data);
    }
} 
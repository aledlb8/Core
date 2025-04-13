package dev.aledlb.listeners;

import dev.aledlb.features.player.PlayerData;
import dev.aledlb.features.player.PlayerDataManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.UUID;

/**
 * Listens for chat events and stores messages in player history.
 */
public class ChatListener implements Listener {
    private final PlayerDataManager dataManager;
    private static final int MAX_HISTORY_SIZE = 100;

    /**
     * Creates a new ChatListener instance.
     * @param dataManager The player data manager.
     */
    public ChatListener(PlayerDataManager dataManager) {
        this.dataManager = dataManager;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        if (event.isCancelled()) {
            return;
        }

        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        String message = event.getMessage();
        
        // Store message with timestamp
        String formattedMessage = System.currentTimeMillis() + "|" + message;
        
        PlayerData data = dataManager.getPlayerData(uuid);
        data.getChatHistory().add(formattedMessage);
        
        // Limit history size
        while (data.getChatHistory().size() > MAX_HISTORY_SIZE) {
            data.getChatHistory().remove(0);
        }
        
        // Save data asynchronously
        dataManager.savePlayerDataAsync(uuid);
    }
} 
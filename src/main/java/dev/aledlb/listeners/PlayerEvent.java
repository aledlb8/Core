package dev.aledlb.listeners;

import dev.aledlb.Core;
import dev.aledlb.utilities.Logger;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Handles all player-related events in the Core plugin.
 * This includes:
 * - Player join/quit messages
 * - Death messages
 * - Chat formatting
 * - Name tag updates
 * - Player freezing
 * - Player muting
 */
public class PlayerEvent implements Listener {

    private static final String JOIN_FORMAT = "&8[&a+&8] &a%s &8has joined the server.";
    private static final String QUIT_FORMAT = "&8[&c-&8] &c%s &8has left the server.";
    private static final String DEATH_PREFIX = "&8[&4☠&8] ";
    private static final String MUTED_MESSAGE = "&cYou are muted.";
    private static final String CHAT_FORMAT_WITH_PREFIX = "%s &f%s: %s";
    private static final String CHAT_FORMAT_WITHOUT_PREFIX = "&f%s: %s";

    private static final List<String> DEATH_MESSAGES = Arrays.asList(
            "Oops, %s did it again.",
            "RIP %s, you won't be missed.",
            "Goodbye, %s.",
            "Another one bites the dust. Goodbye, %s.",
            "You can't respawn, %s.",
            "You died, %s.",
            "%s got yeeted.",
            "%s was killed by a player who is better than them.",
            "Goodnight, sweet prince. %s has died.",
            "Goodbye, %s. You won't be remembered.",
            "It's a sad day for %s.",
            "%s died. What a shame.",
            "RIP %s."
    );

    private final Core plugin;
    private final Random random;

    /**
     * Creates a new PlayerEvent listener
     * @param plugin The Core plugin instance
     */
    public PlayerEvent(Core plugin) {
        this.plugin = plugin;
        this.random = new Random();
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    /**
     * Handles player join events
     * @param event The join event
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onJoin(PlayerJoinEvent event) {
        try {
            event.setJoinMessage(null);
            Player player = event.getPlayer();
            updateNameTag(player);
            broadcastMessage(String.format(JOIN_FORMAT, player.getDisplayName()));
        } catch (Exception e) {
            Logger.severe("Error handling player join event: " + e.getMessage());
        }
    }

    /**
     * Handles player quit events
     * @param event The quit event
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onQuit(PlayerQuitEvent event) {
        try {
            event.setQuitMessage(null);
            Player player = event.getPlayer();
            broadcastMessage(String.format(QUIT_FORMAT, player.getName()));

            if (player.hasMetadata("frozen")) {
                Bukkit.getBanList(org.bukkit.BanList.Type.NAME).addBan(
                    player.getName(),
                    "You have been banned for leaving while frozen.",
                    null,
                    null
                );
            }
        } catch (Exception e) {
            Logger.severe("Error handling player quit event: " + e.getMessage());
        }
    }

    /**
     * Handles player death events
     * @param event The death event
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onDeath(PlayerDeathEvent event) {
        try {
            event.setDeathMessage(null);
            Player player = event.getEntity();
            String deathMessage = DEATH_MESSAGES.get(random.nextInt(DEATH_MESSAGES.size()));
            broadcastMessage(DEATH_PREFIX + String.format(deathMessage, player.getName()));
        } catch (Exception e) {
            Logger.severe("Error handling player death event: " + e.getMessage());
        }
    }

    /**
     * Handles player move events (for freezing)
     * @param event The move event
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onMove(PlayerMoveEvent event) {
        try {
            Player player = event.getPlayer();
            if (player.hasMetadata("frozen")) {
                event.setCancelled(true);
            }
        } catch (Exception e) {
            Logger.severe("Error handling player move event: " + e.getMessage());
        }
    }

    /**
     * Handles player chat events
     * @param event The chat event
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onMessage(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        
        // Check if player is muted
        if (player.hasMetadata("muted")) {
            event.setCancelled(true);
            Logger.player(player, ChatColor.RED + "You are muted and cannot chat.");
            return;
        }
        
        // Apply chat format
        String format = plugin.getConfig().getString("chat-format", "<%player%> %message%");
        format = format.replace("%player%", player.getDisplayName());
        format = format.replace("%message%", "%s");
        event.setFormat(format);
    }

    /**
     * Updates a player's name tag with their prefix
     * @param player The player to update
     */
    public static void updateNameTag(Player player) {
        try {
            ScoreboardManager manager = Bukkit.getScoreboardManager();
            if (manager == null) {
                Logger.warning("Could not get scoreboard manager for player: " + player.getName());
                return;
            }

            String prefix = PlaceholderAPI.setPlaceholders(player, "%core_prefix%");
            if (prefix.isEmpty()) {
                return;
            }

            Scoreboard scoreboard = manager.getNewScoreboard();
            Team team = scoreboard.getTeam(player.getName());
            if (team == null) {
                team = scoreboard.registerNewTeam(player.getName());
            }

            prefix = colorize(prefix);
            String formattedPlayerName = String.format("%s &f%s", prefix, player.getDisplayName());
            player.setPlayerListName(colorize(formattedPlayerName));

            team.setPrefix(colorize(prefix + " "));
            team.addEntry(player.getName());

            player.setScoreboard(scoreboard);
        } catch (Exception e) {
            Logger.severe("Error updating name tag for player " + player.getName() + ": " + e.getMessage());
        }
    }

    /**
     * Broadcasts a message to all players
     * @param message The message to broadcast
     */
    private void broadcastMessage(String message) {
        Bukkit.broadcastMessage(colorize(message));
    }

    /**
     * Translates color codes in a string
     * @param text The text to colorize
     * @return The colorized text
     */
    private static String colorize(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }
}

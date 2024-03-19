package dev.aledlb.listeners;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;

import java.util.List;

public class PlayerEvent implements Listener {

    private final JavaPlugin plugin;

    public PlayerEvent(JavaPlugin plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void OnJoin(PlayerJoinEvent event) {
        event.setJoinMessage(null);
        Player player = event.getPlayer();
        updateNameTag(player);
        Bukkit.broadcastMessage(ChatColor.GRAY + "[" + ChatColor.GREEN + "+" + ChatColor.GRAY + "] " + ChatColor.GREEN + player.getDisplayName() + ChatColor.GRAY + " has joined the server.");
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        event.setQuitMessage(null);
        Player player = event.getPlayer();
        Bukkit.broadcastMessage(ChatColor.GRAY + "[" + ChatColor.RED + "-" + ChatColor.GRAY + "] " + ChatColor.RED + player.getName() + ChatColor.GRAY + " has left the server.");
        if (player.hasMetadata("frozen")) {
            Bukkit.getBanList(org.bukkit.BanList.Type.NAME).addBan(player.getName(), "You have been banned for leaving while frozen.", null, null);
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        event.setDeathMessage(null);
        Player player = event.getEntity();

        String[] deadMessages = {
                ChatColor.GRAY + "Oops, " + ChatColor.RED + player.getName() + ChatColor.GRAY + " " + "did it again.",
                ChatColor.GRAY + "RIP " + ChatColor.RED + player.getName() + ChatColor.GRAY + ", you won't be missed.",
                ChatColor.GRAY + "Goodbye, " + ChatColor.RED + player.getName() + ChatColor.GRAY + ".",
                ChatColor.GRAY + "Another one bites the dust. Goodbye, " + ChatColor.RED + player.getName() + ChatColor.GRAY + ".",
                ChatColor.GRAY + "You can't respawn, " + ChatColor.RED + player.getName() + ChatColor.GRAY + ".",
                ChatColor.GRAY + "You died, " + ChatColor.RED + player.getName() + ChatColor.GRAY + ".",
                ChatColor.RED + player.getName() + ChatColor.GRAY + " got yeeted.",
                ChatColor.RED + player.getName() + ChatColor.GRAY + " was killed by a player who is better than them.",
                ChatColor.GRAY + "Goodnight, sweet prince. " + ChatColor.RED + player.getName() + ChatColor.GRAY + " has died.",
                ChatColor.GRAY + "Goodbye, " + ChatColor.RED + player.getName() + ChatColor.GRAY + ". You won't be remembered.",
                ChatColor.GRAY + "It's a sad day for " + ChatColor.RED + player.getName() + ChatColor.GRAY + ".",
                ChatColor.RED + player.getName() + ChatColor.GRAY + " died. What a shame.",
                ChatColor.GRAY + "RIP " + ChatColor.RED + player.getName() + ChatColor.GRAY + ".",
        };

        String randomMessage = deadMessages[(int) (Math.random() * deadMessages.length)];
        Bukkit.broadcastMessage(ChatColor.GRAY + "[" + ChatColor.RED + "☠" + ChatColor.GRAY + "] " + randomMessage);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (player.hasMetadata("frozen")) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onMessage(PlayerChatEvent event) {
        Player player = event.getPlayer();

        if (player.hasMetadata("muted")) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "You are muted.");
            return;
        }

        String prefix = PlaceholderAPI.setPlaceholders(player, "%core_prefix%");
        String formattedMessage;

        String message = event.getMessage();

        prefix = ChatColor.translateAlternateColorCodes('&', prefix);
        message = ChatColor.translateAlternateColorCodes('&', message);

        if (prefix.isEmpty()) {
            formattedMessage = String.format("&f%s: %s", player.getDisplayName(), message);
        } else {
            formattedMessage = String.format("%s &f%s: %s", prefix, player.getDisplayName(), message);
        }

        String coloredFormattedMessage = ChatColor.translateAlternateColorCodes('&', formattedMessage);

        event.setFormat(coloredFormattedMessage);
    }

    public static void updateNameTag(Player player) {
        ScoreboardManager manager = Bukkit.getScoreboardManager();
        if (manager == null) return;

        String prefix = PlaceholderAPI.setPlaceholders(player, "%core_prefix%");
        if (prefix.isEmpty()) return;

        Scoreboard scoreboard = manager.getNewScoreboard();
        Team team = scoreboard.getTeam(player.getName()) != null ? scoreboard.getTeam(player.getName()) : scoreboard.registerNewTeam(player.getName());

        prefix = ChatColor.translateAlternateColorCodes('&', prefix);
        String formattedPlayerName = String.format("%s &f%s", prefix, player.getDisplayName());
        String coloredFormattedPlayerName = ChatColor.translateAlternateColorCodes('&', formattedPlayerName);

        player.setPlayerListName(coloredFormattedPlayerName);

        prefix = ChatColor.translateAlternateColorCodes('&', prefix + " ");

        team.setPrefix(ChatColor.translateAlternateColorCodes('&', prefix));
        team.addEntry(player.getName());

        player.setScoreboard(scoreboard);
    }
}

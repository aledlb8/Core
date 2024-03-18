package dev.aledlb.listeners;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class PlayerEvent implements Listener {

    public PlayerEvent(JavaPlugin plugin) {
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void OnJoin(PlayerJoinEvent event) {
        event.setJoinMessage(null);
        Player player = event.getPlayer();
        Bukkit.broadcastMessage(ChatColor.GRAY + "[" + ChatColor.GREEN + "+" + ChatColor.GRAY + "] " + ChatColor.GREEN + player.getName() + ChatColor.GRAY + " has joined the server.");
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
    public void onMessage(PlayerChatEvent event) {
        Player player = event.getPlayer();

        if (player.hasMetadata("muted")) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "You are muted.");
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (player.hasMetadata("frozen")) {
            event.setCancelled(true);
        }
    }
}

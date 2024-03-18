package dev.aledlb.features.motd;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.CachedServerIcon;

import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class MOTDManager implements Listener {

    private JavaPlugin plugin;
    private CachedServerIcon serverIcon;

    public MOTDManager(JavaPlugin plugin) {
        this.plugin = plugin;
        loadServerIcon();
    }

    private void loadServerIcon() {
        try {
            File file = new File(plugin.getConfig().getString("motd.icon-path"));
            if (file.exists()) {
                BufferedImage image = ImageIO.read(file);
                serverIcon = plugin.getServer().loadServerIcon(image);
            } else {
                plugin.getLogger().warning("Server icon file not found at: " + file.getPath());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @EventHandler
    public void onServerListPing(ServerListPingEvent event) {
        String firstLine = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("motd.first-line"));
        String secondLine = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("motd.second-line"));
        event.setMotd(firstLine + "\n" + secondLine);

        if (serverIcon != null) {
            event.setServerIcon(serverIcon);
        }
    }
}
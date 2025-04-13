package dev.aledlb.features.motd;

import dev.aledlb.Core;
import dev.aledlb.utilities.Logger;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.util.CachedServerIcon;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Objects;

/**
 * Manages the server's MOTD (Message of the Day) and server icon.
 * This includes:
 * - Loading and caching the server icon
 * - Setting custom MOTD messages
 * - Handling server list ping events
 */
public class MOTDManager implements Listener {

    private static final String ICON_PATH_CONFIG = "motd.icon-path";
    private static final String FIRST_LINE_CONFIG = "motd.first-line";
    private static final String SECOND_LINE_CONFIG = "motd.second-line";

    private final Core plugin;
    private CachedServerIcon serverIcon;

    /**
     * Creates a new MOTDManager instance
     * @param plugin The Core plugin instance
     */
    public MOTDManager(Core plugin) {
        this.plugin = plugin;
        loadServerIcon();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    /**
     * Loads and caches the server icon from the configured path
     */
    private void loadServerIcon() {
        try {
            String iconPath = plugin.getConfig().getString(ICON_PATH_CONFIG);
            if (iconPath == null) {
                Logger.warning("Server icon path not configured in config.yml");
                return;
            }

            File iconFile = new File(iconPath);
            if (!iconFile.exists()) {
                Logger.warning("Server icon file not found at: " + iconFile.getAbsolutePath());
                return;
            }

            BufferedImage image = ImageIO.read(iconFile);
            if (image == null) {
                Logger.warning("Failed to load server icon image");
                return;
            }

            serverIcon = plugin.getServer().loadServerIcon(image);
            Logger.info("Server icon loaded successfully");
        } catch (Exception e) {
            Logger.severe("Error loading server icon: " + e.getMessage());
        }
    }

    /**
     * Handles server list ping events to set custom MOTD and icon
     * @param event The server list ping event
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onServerListPing(ServerListPingEvent event) {
        try {
            String firstLine = plugin.getConfig().getString(FIRST_LINE_CONFIG);
            String secondLine = plugin.getConfig().getString(SECOND_LINE_CONFIG);

            if (firstLine == null || secondLine == null) {
                Logger.warning("MOTD lines not configured in config.yml");
                return;
            }

            firstLine = ChatColor.translateAlternateColorCodes('&', firstLine);
            secondLine = ChatColor.translateAlternateColorCodes('&', secondLine);

            event.setMotd(firstLine + "\n" + secondLine);

            if (serverIcon != null) {
                event.setServerIcon(serverIcon);
            }
        } catch (Exception e) {
            Logger.severe("Error handling server list ping: " + e.getMessage());
        }
    }

    /**
     * Reloads the server icon and MOTD configuration
     */
    public void reload() {
        loadServerIcon();
    }
}
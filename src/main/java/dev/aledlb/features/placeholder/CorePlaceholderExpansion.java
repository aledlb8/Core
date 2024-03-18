package dev.aledlb.features.placeholder;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class CorePlaceholderExpansion extends PlaceholderExpansion {

    private final JavaPlugin plugin;

    public CorePlaceholderExpansion(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public @NotNull String getAuthor() {
        return plugin.getDescription().getAuthors().toString();
    }

    @Override
    public @NotNull String getIdentifier() {
        return "core"; // This will be used in placeholders %core_{placeholder}%
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public String onPlaceholderRequest(Player player, String identifier) {
        if (player == null) {
            return "";
        }

        if (identifier.equals("prefix")) {
            FileConfiguration config = plugin.getConfig();
            ConfigurationSection usersSection = config.getConfigurationSection("users");
            if (usersSection == null) {
                return "";
            }

            List<String> playerGroups = usersSection.getStringList(player.getName() + ".groups");
            if (playerGroups.isEmpty()) {
                return "";
            }

            String playerGroup = playerGroups.get(0);
            String prefix = config.getString("groups." + playerGroup + ".prefix", "");

            return ChatColor.translateAlternateColorCodes('&', prefix);
        }

        return null;
    }
}
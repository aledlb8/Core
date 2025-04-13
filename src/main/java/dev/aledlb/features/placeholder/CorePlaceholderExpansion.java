package dev.aledlb.features.placeholder;

import dev.aledlb.Core;
import dev.aledlb.utilities.Logger;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Provides placeholder expansions for the Core plugin.
 * This includes:
 * - Player prefix placeholders
 * - Group prefix placeholders
 * - Permission placeholders
 * - Custom placeholders
 */
public class CorePlaceholderExpansion extends PlaceholderExpansion {

    private static final String PREFIX_PLACEHOLDER = "prefix";
    private static final String USERS_SECTION = "users";
    private static final String GROUPS_SECTION = "groups";

    private final Core plugin;

    /**
     * Creates a new CorePlaceholderExpansion instance
     * @param plugin The Core plugin instance
     */
    public CorePlaceholderExpansion(Core plugin) {
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
        return String.join(", ", plugin.getDescription().getAuthors());
    }

    @Override
    public @NotNull String getIdentifier() {
        return "core";
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String identifier) {
        try {
            if (player == null) {
                return "";
            }

            switch (identifier.toLowerCase()) {
                case PREFIX_PLACEHOLDER:
                    return getPlayerPrefix(player);
                default:
                    return null;
            }
        } catch (Exception e) {
            Logger.severe("Error processing placeholder request: " + e.getMessage());
            return "";
        }
    }

    /**
     * Gets the prefix for a player
     * @param player The player to get the prefix for
     * @return The player's prefix
     */
    private String getPlayerPrefix(Player player) {
        try {
            ConfigurationSection usersSection = plugin.getConfig().getConfigurationSection(USERS_SECTION);
            if (usersSection == null) {
                Logger.warning("Users section not found in config");
                return "";
            }

            List<String> playerGroups = usersSection.getStringList(player.getName() + ".groups");
            if (playerGroups.isEmpty()) {
                return "";
            }

            String playerGroup = playerGroups.get(0);
            String prefix = plugin.getConfig().getString(GROUPS_SECTION + "." + playerGroup + ".prefix", "");
            return ChatColor.translateAlternateColorCodes('&', prefix);
        } catch (Exception e) {
            Logger.severe("Error getting player prefix: " + e.getMessage());
            return "";
        }
    }
}
package dev.aledlb.features.moderation;

import dev.aledlb.Core;
import dev.aledlb.utilities.Logger;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Manages chat filtering with customizable rules.
 */
public class ChatFilter implements Listener {
    private final Core plugin;
    private final Map<String, FilterRule> filterRules;
    private final Map<Player, Long> lastMessageTime;
    private final Map<Player, String> lastMessage;
    private static final long MESSAGE_COOLDOWN = 1000; // 1 second cooldown between messages

    /**
     * Creates a new ChatFilter instance.
     * @param plugin The Core plugin instance.
     */
    public ChatFilter(Core plugin) {
        this.plugin = plugin;
        this.filterRules = new HashMap<>();
        this.lastMessageTime = new HashMap<>();
        this.lastMessage = new HashMap<>();
        loadFilterRules();
    }

    /**
     * Loads filter rules from the configuration.
     */
    public void loadFilterRules() {
        filterRules.clear();
        ConfigurationSection rulesSection = plugin.getConfig().getConfigurationSection("chat-filter.rules");
        
        if (rulesSection == null) {
            // Add default rules if none exist
            addDefaultRules();
            return;
        }

        for (String key : rulesSection.getKeys(false)) {
            ConfigurationSection ruleSection = rulesSection.getConfigurationSection(key);
            if (ruleSection != null) {
                try {
                    String pattern = ruleSection.getString("pattern");
                    String action = ruleSection.getString("action", "block");
                    String message = ruleSection.getString("message", "Your message was blocked.");
                    
                    addFilterRule(key, pattern, action, message);
                } catch (Exception e) {
                    Logger.warning("Failed to load filter rule '" + key + "': " + e.getMessage());
                }
            }
        }
    }

    /**
     * Adds default filter rules.
     */
    private void addDefaultRules() {
        addFilterRule("swearing", "(?i).*\\b(bad|words|here)\\b.*", "block", "Please watch your language!");
        addFilterRule("spam", "(?i).*\\b(spam|words|here)\\b.*", "warn", "Please don't spam!");
    }

    /**
     * Adds a new filter rule.
     * @param name The name of the rule.
     * @param pattern The regex pattern to match.
     * @param action The action to take when matched.
     * @param message The message to display when matched.
     * @return true if the rule was added successfully, false otherwise.
     */
    public boolean addFilterRule(String name, String pattern, String action, String message) {
        try {
            Pattern.compile(pattern);
            filterRules.put(name, new FilterRule(pattern, action, message));
            
            // Save to config
            plugin.getConfig().set("chat-filter.rules." + name + ".pattern", pattern);
            plugin.getConfig().set("chat-filter.rules." + name + ".action", action);
            plugin.getConfig().set("chat-filter.rules." + name + ".message", message);
            plugin.saveConfig();
            
            return true;
        } catch (PatternSyntaxException e) {
            Logger.warning("Invalid regex pattern for filter rule '" + name + "': " + e.getMessage());
            return false;
        }
    }

    /**
     * Removes a filter rule.
     * @param name The name of the rule to remove.
     * @return true if the rule was removed, false if it didn't exist.
     */
    public boolean removeFilterRule(String name) {
        if (filterRules.remove(name) != null) {
            // Remove from config
            plugin.getConfig().set("chat-filter.rules." + name, null);
            plugin.saveConfig();
            return true;
        }
        return false;
    }

    /**
     * Gets a list of all filter rules.
     * @return A list of filter rule names and their details.
     */
    public List<String> getFilterRules() {
        List<String> rules = new ArrayList<>();
        for (Map.Entry<String, FilterRule> entry : filterRules.entrySet()) {
            FilterRule rule = entry.getValue();
            rules.add(ChatColor.YELLOW + entry.getKey() + ":");
            rules.add(ChatColor.GRAY + "  Pattern: " + rule.getPattern());
            rules.add(ChatColor.GRAY + "  Action: " + rule.getAction());
            rules.add(ChatColor.GRAY + "  Message: " + rule.getMessage());
        }
        return rules;
    }

    /**
     * Reloads the filter rules from the configuration.
     */
    public void reload() {
        loadFilterRules();
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage();

        // Check for spam
        if (isSpamming(player, message)) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "Please wait before sending another message.");
            return;
        }

        // Check against filter rules
        for (FilterRule rule : filterRules.values()) {
            if (rule.matches(message)) {
                handleFilterMatch(player, rule);
                event.setCancelled(true);
                return;
            }
        }

        // Update last message info
        lastMessageTime.put(player, System.currentTimeMillis());
        lastMessage.put(player, message);
    }

    /**
     * Checks if a player is spamming.
     * @param player The player to check.
     * @param message The message to check.
     * @return true if the player is spamming, false otherwise.
     */
    private boolean isSpamming(Player player, String message) {
        Long lastTime = lastMessageTime.get(player);
        String lastMsg = lastMessage.get(player);

        if (lastTime != null && lastMsg != null) {
            long timeDiff = System.currentTimeMillis() - lastTime;
            if (timeDiff < MESSAGE_COOLDOWN || message.equals(lastMsg)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Handles a filter rule match.
     * @param player The player who triggered the rule.
     * @param rule The rule that was matched.
     */
    private void handleFilterMatch(Player player, FilterRule rule) {
        switch (rule.getAction().toLowerCase()) {
            case "block":
                player.sendMessage(ChatColor.RED + rule.getMessage());
                break;
            case "warn":
                player.sendMessage(ChatColor.YELLOW + "Warning: " + rule.getMessage());
                break;
            case "mute":
                // This would need to be implemented with your mute system
                player.sendMessage(ChatColor.RED + "You have been muted: " + rule.getMessage());
                break;
        }
    }

    /**
     * Represents a chat filter rule.
     */
    private static class FilterRule {
        private final Pattern pattern;
        private final String action;
        private final String message;

        /**
         * Creates a new FilterRule.
         * @param pattern The regex pattern to match.
         * @param action The action to take when matched.
         * @param message The message to display when matched.
         */
        public FilterRule(String pattern, String action, String message) {
            this.pattern = Pattern.compile(pattern);
            this.action = action;
            this.message = message;
        }

        /**
         * Checks if a message matches this rule.
         * @param message The message to check.
         * @return true if the message matches, false otherwise.
         */
        public boolean matches(String message) {
            return pattern.matcher(message).matches();
        }

        /**
         * Gets the pattern for this rule.
         * @return The pattern.
         */
        public String getPattern() {
            return pattern.pattern();
        }

        /**
         * Gets the action for this rule.
         * @return The action.
         */
        public String getAction() {
            return action;
        }

        /**
         * Gets the message for this rule.
         * @return The message.
         */
        public String getMessage() {
            return message;
        }
    }
} 
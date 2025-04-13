package dev.aledlb.features.kits;

import dev.aledlb.Core;
import dev.aledlb.utilities.Logger;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Manages kit-related functionality including:
 * - Saving and loading kits
 * - Managing kit GUI
 * - Handling kit permissions
 * - Managing kit contents
 */
public class KitManager implements Listener {

    private static final String KITS_FOLDER = "kits";
    private static final String GUI_TITLE = "Kits";
    private static final int GUI_SIZE = 9;
    private static final int MAX_PREVIEW_ITEMS = 10;

    private static final String ERROR_KIT_EXISTS = "&cA kit with that name already exists.";
    private static final String ERROR_KIT_NOT_FOUND = "&cNo kit with that name exists.";
    private static final String ERROR_DELETE = "&cAn error occurred while deleting the kit.";
    private static final String ERROR_SAVE = "&cAn error occurred while saving the kit: %s";
    private static final String ERROR_LOAD = "&cAn error occurred while loading the kit: %s";
    private static final String ERROR_PERMISSION = "&cYou don't have permission to use that kit.";
    private static final String ERROR_INVENTORY_FULL = "&cYour inventory is full.";
    private static final String ERROR_NO_KITS = "&cNo kits found.";
    private static final String ERROR_KIT_MISSING = "&cKit does not exist.";
    private static final String ERROR_CORRUPT_DATA = "&cKit data is missing or corrupt.";

    private static final String SUCCESS_LOAD = "&aKit %s loaded successfully.";
    private static final String SUCCESS_SAVE = "&aKit %s saved successfully.";
    private static final String SUCCESS_DELETE = "&aKit %s deleted successfully.";

    private final Core plugin;

    /**
     * Creates a new KitManager instance
     * @param plugin The Core plugin instance
     */
    public KitManager(Core plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
        createKitsFolder();
    }

    /**
     * Creates the kits folder if it doesn't exist
     */
    private void createKitsFolder() {
        File kitsFolder = new File(plugin.getDataFolder(), KITS_FOLDER);
        if (!kitsFolder.exists()) {
            if (!kitsFolder.mkdirs()) {
                Logger.severe("Failed to create kits folder");
            }
        }
    }

    /**
     * Saves a player's current inventory as a kit
     * @param player The player whose inventory to save
     * @param kitName The name of the kit
     */
    public void saveKit(Player player, String kitName) {
        try {
            File kitFile = new File(plugin.getDataFolder() + File.separator + KITS_FOLDER, kitName + ".yml");
            if (kitFile.exists()) {
                Logger.player(player, ERROR_KIT_EXISTS);
                return;
            }

            FileConfiguration kitConfig = new YamlConfiguration();
            kitConfig.set("inventory", player.getInventory().getContents());
            kitConfig.set("armor", player.getInventory().getArmorContents());
            kitConfig.set("offhand", player.getInventory().getItemInOffHand());

            kitConfig.save(kitFile);
            Logger.player(player, String.format(SUCCESS_SAVE, kitName));
        } catch (Exception e) {
            Logger.player(player, String.format(ERROR_SAVE, e.getMessage()));
            Logger.severe("Error saving kit: " + e.getMessage());
        }
    }

    /**
     * Deletes a kit
     * @param player The player requesting the deletion
     * @param kitName The name of the kit to delete
     */
    public void deleteKit(Player player, String kitName) {
        try {
            File kitFile = new File(plugin.getDataFolder() + File.separator + KITS_FOLDER, kitName + ".yml");
            if (!kitFile.exists()) {
                Logger.player(player, ERROR_KIT_NOT_FOUND);
                return;
            }

            if (!kitFile.delete()) {
                Logger.player(player, ERROR_DELETE);
                return;
            }

            Logger.player(player, String.format(SUCCESS_DELETE, kitName));
        } catch (Exception e) {
            Logger.player(player, ERROR_DELETE);
            Logger.severe("Error deleting kit: " + e.getMessage());
        }
    }

    /**
     * Shows the kits GUI to a player
     * @param player The player to show the GUI to
     */
    public void showKitsGUI(Player player) {
        try {
            Inventory kitsGUI = Bukkit.createInventory(null, GUI_SIZE, GUI_TITLE);
            File kitsFolder = new File(plugin.getDataFolder(), KITS_FOLDER);
            File[] kitFiles = kitsFolder.listFiles();

            if (kitFiles == null || kitFiles.length == 0) {
                Logger.player(player, ERROR_NO_KITS);
                return;
            }

            for (File kitFile : kitFiles) {
                String kitName = kitFile.getName().replace(".yml", "");
                FileConfiguration kitConfig = YamlConfiguration.loadConfiguration(kitFile);
                ItemStack kitItem = createKitPreviewItem(kitName, kitConfig);
                kitsGUI.addItem(kitItem);
            }

            player.openInventory(kitsGUI);
        } catch (Exception e) {
            Logger.player(player, ERROR_NO_KITS);
            Logger.severe("Error showing kits GUI: " + e.getMessage());
        }
    }

    /**
     * Creates a preview item for a kit
     * @param kitName The name of the kit
     * @param kitConfig The kit's configuration
     * @return The preview item
     */
    private ItemStack createKitPreviewItem(String kitName, FileConfiguration kitConfig) {
        ItemStack kitItem = new ItemStack(Material.CHEST);
        ItemMeta meta = kitItem.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.GREEN + kitName);
            List<String> lore = new ArrayList<>();

            List<?> rawInventoryItems = kitConfig.getList("inventory");
            if (rawInventoryItems != null && !rawInventoryItems.isEmpty()) {
                for (int i = 0; i < Math.min(rawInventoryItems.size(), MAX_PREVIEW_ITEMS); i++) {
                    Object item = rawInventoryItems.get(i);
                    if (item instanceof ItemStack) {
                        ItemStack itemStack = (ItemStack) item;
                        lore.add(ChatColor.YELLOW + itemStack.getType().toString() + " x" + itemStack.getAmount());
                    }
                }
                if (rawInventoryItems.size() > MAX_PREVIEW_ITEMS) {
                    lore.add(ChatColor.GRAY + "...and more");
                }
            } else {
                lore.add(ChatColor.RED + "Empty Kit");
            }

            meta.setLore(lore);
            kitItem.setItemMeta(meta);
        }
        return kitItem;
    }

    /**
     * Handles kit selection from the GUI
     * @param event The inventory click event
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onKitSelect(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals(GUI_TITLE)) return;

        event.setCancelled(true);
        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getType() != Material.CHEST) return;

        Player player = (Player) event.getWhoClicked();
        ItemMeta meta = clickedItem.getItemMeta();
        if (meta == null) return;

        String kitName = ChatColor.stripColor(meta.getDisplayName());
        loadKitForPlayer(player, kitName);
    }

    /**
     * Loads a kit for a player
     * @param player The player to load the kit for
     * @param kitName The name of the kit to load
     */
    public void loadKitForPlayer(Player player, String kitName) {
        try {
            File kitFile = new File(plugin.getDataFolder() + File.separator + KITS_FOLDER, kitName + ".yml");
            if (!kitFile.exists()) {
                Logger.player(player, ERROR_KIT_MISSING);
                return;
            }

            if (!player.hasPermission("core.kit." + kitName) && !player.hasPermission("core.kit.*")) {
                Logger.player(player, ERROR_PERMISSION);
                return;
            }

            if (player.getInventory().firstEmpty() == -1) {
                Logger.player(player, ERROR_INVENTORY_FULL);
                return;
            }

            FileConfiguration kitConfig = YamlConfiguration.loadConfiguration(kitFile);
            loadKitContents(player, kitConfig);
            Logger.player(player, String.format(SUCCESS_LOAD, kitName));
        } catch (Exception e) {
            Logger.player(player, String.format(ERROR_LOAD, e.getMessage()));
            Logger.severe("Error loading kit: " + e.getMessage());
        }
    }

    /**
     * Loads the contents of a kit into a player's inventory
     * @param player The player to load the contents for
     * @param kitConfig The kit's configuration
     */
    private void loadKitContents(Player player, FileConfiguration kitConfig) {
        // Load inventory contents
        List<?> inventoryList = kitConfig.getList("inventory");
        if (inventoryList != null && !inventoryList.isEmpty()) {
            ItemStack[] inventoryContents = inventoryList.stream()
                .filter(item -> item instanceof ItemStack)
                .map(item -> (ItemStack) item)
                .toArray(ItemStack[]::new);
            player.getInventory().setContents(inventoryContents);
        } else {
            Logger.player(player, ERROR_CORRUPT_DATA);
        }

        // Load armor contents
        List<?> armorList = kitConfig.getList("armor");
        if (armorList != null && !armorList.isEmpty()) {
            ItemStack[] armorContents = armorList.stream()
                .filter(item -> item instanceof ItemStack)
                .map(item -> (ItemStack) item)
                .toArray(ItemStack[]::new);
            player.getInventory().setArmorContents(armorContents);
        }

        // Load off-hand item
        ItemStack offHand = kitConfig.getItemStack("offhand");
        if (offHand != null) {
            player.getInventory().setItemInOffHand(offHand);
        }
    }
}

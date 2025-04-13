package dev.aledlb.features.enchantments;

import dev.aledlb.Core;
import dev.aledlb.utilities.Logger;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages the enchantment GUI and related functionality.
 * This includes:
 * - Opening the enchantment selection GUI
 * - Handling enchantment selection
 * - Managing enchantment levels
 * - Applying enchantments to items
 */
public class EnchantmentGUI implements Listener {

    private static final String GUI_TITLE = "Select Enchantment";
    private static final String LEVEL_GUI_TITLE = "Select Level: %s";
    private static final int GUI_SIZE = 27;
    private static final int MAX_ENCHANT_SLOT = 25;
    private static final int REMOVE_ENCHANT_SLOT = 26;

    private static final String ERROR_NO_ITEM = "&cYou must hold an item to enchant.";
    private static final String SUCCESS_MAX_ENCHANT = "&aAll enchantments applied at max level!";
    private static final String SUCCESS_REMOVE_ENCHANT = "&aAll enchantments removed!";
    private static final String SUCCESS_APPLY_ENCHANT = "&aItem enchanted with %s level %d";

    private final Core plugin;
    private final Map<String, Enchantment> pendingEnchantments;
    private final List<Enchantment> availableEnchantments;

    /**
     * Creates a new EnchantmentGUI instance
     * @param plugin The Core plugin instance
     */
    public EnchantmentGUI(Core plugin) {
        this.plugin = plugin;
        this.pendingEnchantments = new HashMap<>();
        this.availableEnchantments = initializeEnchantments();
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    /**
     * Initializes the list of available enchantments
     * @return List of available enchantments
     */
    private List<Enchantment> initializeEnchantments() {
        return Arrays.asList(
            Enchantment.PROTECTION_ENVIRONMENTAL,
            Enchantment.PROTECTION_FIRE,
            Enchantment.PROTECTION_FALL,
            Enchantment.PROTECTION_EXPLOSIONS,
            Enchantment.PROTECTION_PROJECTILE,
            Enchantment.OXYGEN,
            Enchantment.WATER_WORKER,
            Enchantment.THORNS,
            Enchantment.DEPTH_STRIDER,
            Enchantment.FROST_WALKER,
            Enchantment.DAMAGE_ALL,
            Enchantment.DAMAGE_UNDEAD,
            Enchantment.DAMAGE_ARTHROPODS,
            Enchantment.KNOCKBACK,
            Enchantment.FIRE_ASPECT,
            Enchantment.LOOT_BONUS_MOBS,
            Enchantment.SWEEPING_EDGE,
            Enchantment.DIG_SPEED,
            Enchantment.SILK_TOUCH,
            Enchantment.DURABILITY,
            Enchantment.LOOT_BONUS_BLOCKS,
            Enchantment.ARROW_DAMAGE,
            Enchantment.ARROW_KNOCKBACK,
            Enchantment.ARROW_FIRE,
            Enchantment.ARROW_INFINITE,
            Enchantment.LUCK,
            Enchantment.LURE,
            Enchantment.LOYALTY,
            Enchantment.IMPALING,
            Enchantment.RIPTIDE,
            Enchantment.CHANNELING,
            Enchantment.MULTISHOT,
            Enchantment.QUICK_CHARGE,
            Enchantment.PIERCING,
            Enchantment.MENDING,
            Enchantment.SOUL_SPEED
        );
    }

    /**
     * Opens the enchantment GUI for a player
     * @param player The player to open the GUI for
     */
    public void openGUI(Player player) {
        try {
            ItemStack itemInHand = player.getInventory().getItemInMainHand();
            if (itemInHand.getType() == Material.AIR) {
                Logger.player(player, ERROR_NO_ITEM);
                return;
            }

            Inventory inv = Bukkit.createInventory(null, GUI_SIZE, GUI_TITLE);
            populateEnchantmentGUI(inv, itemInHand);
            addUtilityItems(inv);
            player.openInventory(inv);
        } catch (Exception e) {
            Logger.severe("Error opening enchantment GUI: " + e.getMessage());
        }
    }

    /**
     * Populates the enchantment GUI with available enchantments
     * @param inv The inventory to populate
     * @param itemToEnchant The item to enchant
     */
    private void populateEnchantmentGUI(Inventory inv, ItemStack itemToEnchant) {
        for (Enchantment enchantment : availableEnchantments) {
            if (enchantment.canEnchantItem(itemToEnchant)) {
                ItemStack enchantedBook = createEnchantedBook(enchantment);
                inv.addItem(enchantedBook);
            }
        }
    }

    /**
     * Creates an enchanted book item for an enchantment
     * @param enchantment The enchantment to create the book for
     * @return The enchanted book item
     */
    private ItemStack createEnchantedBook(Enchantment enchantment) {
        ItemStack enchantedBook = new ItemStack(Material.ENCHANTED_BOOK);
        ItemMeta meta = enchantedBook.getItemMeta();
        if (meta != null) {
            meta.addEnchant(enchantment, 1, true);
            meta.setDisplayName(enchantment.getKey().getKey());
            enchantedBook.setItemMeta(meta);
        }
        return enchantedBook;
    }

    /**
     * Adds utility items to the enchantment GUI
     * @param inv The inventory to add items to
     */
    private void addUtilityItems(Inventory inv) {
        // Add max enchantments book
        ItemStack maxEnchantsBook = new ItemStack(Material.ENCHANTED_BOOK);
        ItemMeta maxMeta = maxEnchantsBook.getItemMeta();
        if (maxMeta != null) {
            maxMeta.setDisplayName("Max Enchantments");
            maxEnchantsBook.setItemMeta(maxMeta);
        }
        inv.setItem(MAX_ENCHANT_SLOT, maxEnchantsBook);

        // Add remove enchantments book
        ItemStack removeEnchantsBook = new ItemStack(Material.BOOK);
        ItemMeta removeMeta = removeEnchantsBook.getItemMeta();
        if (removeMeta != null) {
            removeMeta.setDisplayName("Remove Enchantments");
            removeEnchantsBook.setItemMeta(removeMeta);
        }
        inv.setItem(REMOVE_ENCHANT_SLOT, removeEnchantsBook);
    }

    /**
     * Handles enchantment selection from the GUI
     * @param event The inventory click event
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onEnchantmentSelect(InventoryClickEvent event) {
        try {
            if (!event.getView().getTitle().equals(GUI_TITLE)) {
                handleLevelSelection(event);
                return;
            }

            event.setCancelled(true);
            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem == null || clickedItem.getType() == Material.AIR) return;

            Player player = (Player) event.getWhoClicked();
            ItemMeta meta = clickedItem.getItemMeta();
            if (meta == null) return;

            String displayName = meta.getDisplayName();
            ItemStack itemToEnchant = player.getInventory().getItemInMainHand();

            if ("Max Enchantments".equals(displayName)) {
                applyMaxEnchantments(player, itemToEnchant);
                return;
            }

            if ("Remove Enchantments".equals(displayName)) {
                removeAllEnchantments(player, itemToEnchant);
                return;
            }

            if (meta.hasEnchants()) {
                handleEnchantmentSelection(player, meta);
            }
        } catch (Exception e) {
            Logger.severe("Error handling enchantment selection: " + e.getMessage());
        }
    }

    /**
     * Handles level selection from the GUI
     * @param event The inventory click event
     */
    private void handleLevelSelection(InventoryClickEvent event) {
        try {
            if (!event.getView().getTitle().startsWith("Select Level: ")) return;

            event.setCancelled(true);
            Player player = (Player) event.getWhoClicked();
            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem == null || clickedItem.getType() == Material.AIR) return;

            ItemMeta meta = clickedItem.getItemMeta();
            if (meta == null) return;

            Enchantment selectedEnchantment = pendingEnchantments.remove(player.getUniqueId().toString());
            if (selectedEnchantment != null) {
                int level = Integer.parseInt(meta.getDisplayName());
                ItemStack itemToEnchant = player.getInventory().getItemInMainHand();
                itemToEnchant.addUnsafeEnchantment(selectedEnchantment, level);
                Logger.player(player, String.format(SUCCESS_APPLY_ENCHANT, 
                    selectedEnchantment.getKey().getKey(), level));
                player.closeInventory();
            }
        } catch (Exception e) {
            Logger.severe("Error handling level selection: " + e.getMessage());
        }
    }

    /**
     * Applies all possible enchantments at max level to an item
     * @param player The player whose item to enchant
     * @param itemToEnchant The item to enchant
     */
    private void applyMaxEnchantments(Player player, ItemStack itemToEnchant) {
        for (Enchantment enchantment : availableEnchantments) {
            if (enchantment.canEnchantItem(itemToEnchant)) {
                itemToEnchant.addUnsafeEnchantment(enchantment, enchantment.getMaxLevel());
            }
        }
        Logger.player(player, SUCCESS_MAX_ENCHANT);
        player.closeInventory();
    }

    /**
     * Removes all enchantments from an item
     * @param player The player whose item to modify
     * @param itemToEnchant The item to modify
     */
    private void removeAllEnchantments(Player player, ItemStack itemToEnchant) {
        for (Enchantment enchantment : itemToEnchant.getEnchantments().keySet()) {
            itemToEnchant.removeEnchantment(enchantment);
        }
        Logger.player(player, SUCCESS_REMOVE_ENCHANT);
        player.closeInventory();
    }

    /**
     * Handles the selection of an enchantment
     * @param player The player who selected the enchantment
     * @param meta The item meta containing the enchantment
     */
    private void handleEnchantmentSelection(Player player, ItemMeta meta) {
        for (Enchantment enchantment : meta.getEnchants().keySet()) {
            pendingEnchantments.put(player.getUniqueId().toString(), enchantment);
            openLevelSelectionGUI(player, enchantment);
            return;
        }
    }

    /**
     * Opens the level selection GUI for an enchantment
     * @param player The player to open the GUI for
     * @param enchantment The enchantment to select levels for
     */
    private void openLevelSelectionGUI(Player player, Enchantment enchantment) {
        try {
            int maxLevel = enchantment.getMaxLevel();
            Inventory inv = Bukkit.createInventory(null, 9, 
                String.format(LEVEL_GUI_TITLE, enchantment.getKey().getKey()));

            for (int i = 1; i <= maxLevel; i++) {
                ItemStack levelOption = new ItemStack(Material.PAPER);
                ItemMeta meta = levelOption.getItemMeta();
                if (meta != null) {
                    meta.setDisplayName(String.valueOf(i));
                    levelOption.setItemMeta(meta);
                }
                inv.addItem(levelOption);
            }

            player.openInventory(inv);
        } catch (Exception e) {
            Logger.severe("Error opening level selection GUI: " + e.getMessage());
        }
    }
}

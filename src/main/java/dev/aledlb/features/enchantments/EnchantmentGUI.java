package dev.aledlb.features.enchantments;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EnchantmentGUI implements Listener {
    private final JavaPlugin plugin;
    private final Map<String, Enchantment> pendingEnchantments = new HashMap<>();

    List<Enchantment> enchantments = Arrays.asList(
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
            //Enchantment.BINDING_CURSE,
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
            //Enchantment.VANISHING_CURSE,
            Enchantment.SOUL_SPEED
            //Enchantment.SWIFT_SNEAK
    );

    public EnchantmentGUI(JavaPlugin plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void openGUI(Player player) {
        ItemStack itemInHand = player.getInventory().getItemInMainHand();
        if (itemInHand.getType() == Material.AIR) {
            player.sendMessage("You must hold an item to enchant.");
            return;
        }

        Inventory inv = Bukkit.createInventory(null, 27, "Select Enchantment");
        for (Enchantment enchantment : enchantments) {
            if (enchantment.canEnchantItem(itemInHand)) {
                ItemStack enchantedBook = new ItemStack(Material.ENCHANTED_BOOK);
                ItemMeta meta = enchantedBook.getItemMeta();
                if (meta != null) {
                    meta.addEnchant(enchantment, 1, true);
                    meta.setDisplayName(enchantment.getKey().getKey());
                    enchantedBook.setItemMeta(meta);
                }
                inv.addItem(enchantedBook);
            }
        }

        // Add "Max Enchantments" book
        ItemStack maxEnchantsBook = new ItemStack(Material.ENCHANTED_BOOK);
        ItemMeta maxMeta = maxEnchantsBook.getItemMeta();
        if (maxMeta != null) {
            maxMeta.setDisplayName("Max Enchantments");
            maxEnchantsBook.setItemMeta(maxMeta);
        }
        inv.setItem(25, maxEnchantsBook); // Position 25 for bottom right

        // Add "Remove Enchantments" book
        ItemStack removeEnchantsBook = new ItemStack(Material.BOOK);
        ItemMeta removeMeta = removeEnchantsBook.getItemMeta();
        if (removeMeta != null) {
            removeMeta.setDisplayName("Remove Enchantments");
            removeEnchantsBook.setItemMeta(removeMeta);
        }
        inv.setItem(26, removeEnchantsBook); // Position 26 for bottom right next to the other


        player.openInventory(inv);
    }

    @EventHandler
    public void onEnchantmentSelect(InventoryClickEvent event) {
        if (event.getView().getTitle().equals("Select Enchantment")) {
            event.setCancelled(true);

            ItemStack clickedItem = event.getCurrentItem();
            Player player = (Player) event.getWhoClicked();
            ItemMeta meta = clickedItem.getItemMeta();

            if (clickedItem.getType() == Material.AIR) return;

            if (clickedItem.hasItemMeta()) {
                String displayName = meta.getDisplayName();

                ItemStack itemToEnchant = player.getInventory().getItemInMainHand();
                if ("Max Enchantments".equals(displayName)) {
                    for (Enchantment enchantment : enchantments) {
                        if (enchantment.canEnchantItem(itemToEnchant)) {
                            itemToEnchant.addUnsafeEnchantment(enchantment, enchantment.getMaxLevel());
                        }
                    }
                    player.sendMessage("All enchantments applied at max level!");
                    player.closeInventory();
                    return;
                }

                // Check if "Remove Enchantments" book was clicked
                if ("Remove Enchantments".equals(displayName)) {
                    for (Enchantment enchantment : itemToEnchant.getEnchantments().keySet()) {
                        itemToEnchant.removeEnchantment(enchantment);
                    }
                    player.sendMessage("All enchantments removed!");
                    player.closeInventory();
                    return;
                }
            }

            if (meta != null && meta.hasEnchants()) {
                for (Enchantment enchantment : meta.getEnchants().keySet()) {
                    // Store the selected enchantment temporarily
                    pendingEnchantments.put(player.getUniqueId().toString(), enchantment);
                    openLevelSelectionGUI(player, enchantment);
                    return;
                }
            }
        } else if (event.getView().getTitle().startsWith("Select Level: ")) {
            event.setCancelled(true);
            Player player = (Player) event.getWhoClicked();
            Enchantment selectedEnchantment = pendingEnchantments.remove(player.getUniqueId().toString());
            if (selectedEnchantment != null) {
                int level = Integer.parseInt(event.getCurrentItem().getItemMeta().getDisplayName());
                ItemStack itemToEnchant = player.getInventory().getItemInMainHand();
                itemToEnchant.addUnsafeEnchantment(selectedEnchantment, level);
                player.sendMessage("Item enchanted with " + selectedEnchantment.getKey().getKey() + " level " + level);
                player.closeInventory();
            }
        }
    }

    private void openLevelSelectionGUI(Player player, Enchantment enchantment) {
        int maxLevel = enchantment.getMaxLevel();
        Inventory inv = Bukkit.createInventory(null, 9, "Select Level: " + enchantment.getKey().getKey());

        // Populate the inventory with options for enchantment levels up to the max level
        for (int i = 1; i <= maxLevel; i++) {
            ItemStack levelOption = new ItemStack(Material.PAPER); // Using PAPER as a placeholder; consider using a more appropriate item.
            ItemMeta meta = levelOption.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(String.valueOf(i)); // Set the display name to indicate the level
                levelOption.setItemMeta(meta);
            }
            inv.addItem(levelOption);
        }

        player.openInventory(inv);
    }

}

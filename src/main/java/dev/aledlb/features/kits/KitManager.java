package dev.aledlb.features.kits;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class KitManager implements Listener {
    public static JavaPlugin plugin = null;

    public KitManager(JavaPlugin plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void saveKit(Player player, String kitName) {
        File kitFile = new File(plugin.getDataFolder() + File.separator + "kits", kitName + ".yml");
        FileConfiguration kitConfig = YamlConfiguration.loadConfiguration(kitFile);

        // check if the kit already exists
        if (kitFile.exists()) {
            player.sendMessage(ChatColor.RED + "A kit with that name already exists.");
            return;
        }

        // Save inventory, armor, and off-hand items
        kitConfig.set("inventory", player.getInventory().getContents());
        kitConfig.set("armor", player.getInventory().getArmorContents());
        kitConfig.set("offhand", player.getInventory().getItemInOffHand());

        try {
            kitConfig.save(kitFile);
        } catch (Exception e) {
            e.printStackTrace();
            player.sendMessage(ChatColor.RED + "An error occurred while saving the kit" + e.getMessage());
        }
    }

    public void deleteKit(Player player, String kitName) {
        File kitFile = new File(plugin.getDataFolder() + File.separator + "kits", kitName + ".yml");

        if (!kitFile.exists()) {
            player.sendMessage(ChatColor.RED + "No kit with that name exists.");
            return;
        }

        if (!kitFile.delete()) {
            player.sendMessage(ChatColor.RED + "An error occurred while deleting the kit.");
            return;
        }

        kitFile.delete();
    }

    public static void showKitsGUI(Player player) {
        Inventory kitsGUI = plugin.getServer().createInventory(null, 9, "Kits");

        File kitsFolder = new File(plugin.getDataFolder() + File.separator + "kits");
        File[] kitFiles = kitsFolder.listFiles();

        if (kitFiles != null) {
            for (File kitFile : kitFiles) {
                String kitName = kitFile.getName().replace(".yml", "");
                FileConfiguration kitConfig = YamlConfiguration.loadConfiguration(kitFile);

                ItemStack kitItem = new ItemStack(Material.CHEST);
                ItemMeta meta = kitItem.getItemMeta();
                if (meta != null) {
                    meta.setDisplayName(ChatColor.GREEN + kitName);

                    // Add lore for previewing kit contents
                    List<String> lore = new ArrayList<>();
                    List<ItemStack> inventoryItems = (List<ItemStack>) kitConfig.getList("inventory");
                    if (inventoryItems != null) {
                        for (int i = 0; i < Math.min(inventoryItems.size(), 10); i++) { // Preview up to 10 items
                            ItemStack item = inventoryItems.get(i);
                            if (item != null) {
                                lore.add(ChatColor.YELLOW + item.getType().toString() + " x" + item.getAmount());
                            }
                        }
                        if (inventoryItems.size() > 10) {
                            lore.add(ChatColor.GRAY + "...and more");
                        }
                    } else {
                        lore.add(ChatColor.RED + "Empty Kit");
                    }

                    meta.setLore(lore);
                    kitItem.setItemMeta(meta);
                }
                kitsGUI.addItem(kitItem);
            }

            player.openInventory(kitsGUI);
        } else {
            player.sendMessage(ChatColor.RED + "No kits found.");
        }
    }

    @EventHandler
    public void loadKit(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals("Kits")) return;

        event.setCancelled(true);
        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getType() != Material.CHEST) return;

        Player player = (Player) event.getWhoClicked();
        ItemMeta meta = clickedItem.getItemMeta();
        if (meta == null) return;

        String kitName = ChatColor.stripColor(meta.getDisplayName());
        File kitFile = new File(plugin.getDataFolder() + File.separator + "kits", kitName + ".yml");
        if (!kitFile.exists()) {
            player.sendMessage(ChatColor.RED + "Kit does not exist.");
            return;
        }

        if (!player.hasPermission("core.kit." + kitName) || player.hasPermission("core.kit.*")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to use that kit.");
            return;
        }

        if (player.getInventory().firstEmpty() == -1) {
            player.sendMessage(ChatColor.RED + "Your inventory is full.");
            return;
        }

        FileConfiguration kitConfig = YamlConfiguration.loadConfiguration(kitFile);

        try {
            // Handle inventory contents
            List<?> inventoryList = kitConfig.getList("inventory");
            if (inventoryList != null && !inventoryList.isEmpty()) {
                ItemStack[] inventoryContents = inventoryList.stream().filter(item -> item instanceof ItemStack).toArray(ItemStack[]::new);
                player.getInventory().setContents(inventoryContents);
            }

            // Handle armor contents
            List<?> armorList = kitConfig.getList("armor");
            if (armorList != null && !armorList.isEmpty()) {
                ItemStack[] armorContents = armorList.stream().filter(item -> item instanceof ItemStack).toArray(ItemStack[]::new);
                player.getInventory().setArmorContents(armorContents);
            }

            // Handle off-hand item
            ItemStack offHand = kitConfig.getItemStack("offhand");
            if (offHand != null) {
                player.getInventory().setItemInOffHand(offHand);
            }

            player.sendMessage(ChatColor.GREEN + "Kit " + kitName + " loaded.");
        } catch (Exception e) {
            player.sendMessage(ChatColor.RED + "An error occurred while loading the kit.");
            e.printStackTrace();
        }
    }

    public void loadKitForPlayer(Player player, String kitName) {
        File kitFile = new File(plugin.getDataFolder() + File.separator + "kits", kitName + ".yml");
        if (!kitFile.exists()) {
            player.sendMessage(ChatColor.RED + "Kit " + kitName + " does not exist.");
            return;
        }

        FileConfiguration kitConfig = YamlConfiguration.loadConfiguration(kitFile);

        try {
            // Handle inventory contents
            List<?> inventoryList = kitConfig.getList("inventory");
            if (inventoryList != null && !inventoryList.isEmpty()) {
                ItemStack[] inventoryContents = inventoryList.stream().filter(item -> item instanceof ItemStack).toArray(ItemStack[]::new);
                player.getInventory().setContents(inventoryContents);
            } else {
                player.sendMessage(ChatColor.RED + "Kit inventory data is missing or corrupt.");
            }

            // Handle armor contents
            List<?> armorList = kitConfig.getList("armor");
            if (armorList != null && !armorList.isEmpty()) {
                ItemStack[] armorContents = armorList.stream().filter(item -> item instanceof ItemStack).toArray(ItemStack[]::new);
                player.getInventory().setArmorContents(armorContents);
            } else {
                player.sendMessage(ChatColor.RED + "Kit armor data is missing or corrupt.");
            }

            // Handle off-hand item
            ItemStack offHand = kitConfig.getItemStack("offhand");
            if (offHand != null) {
                player.getInventory().setItemInOffHand(offHand);
            }

            player.sendMessage(ChatColor.GREEN + "Kit " + kitName + " loaded successfully.");
        } catch (Exception e) {
            player.sendMessage(ChatColor.RED + "An error occurred while loading the kit: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
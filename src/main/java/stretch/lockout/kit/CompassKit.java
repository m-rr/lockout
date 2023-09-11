package stretch.lockout.kit;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class CompassKit implements Kit {
    @Override
    public void apply(Player player) {
        ItemStack compass = new ItemStack(Material.COMPASS);
        ItemMeta itemMeta = compass.getItemMeta();

        if (itemMeta != null) {
            itemMeta.addEnchant(Enchantment.LUCK, 1, true);
            itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            itemMeta.setDisplayName(ChatColor.LIGHT_PURPLE + "Lockout");
            compass.setItemMeta(itemMeta);
        }

        Inventory inv = player.getInventory();
        if (!inv.contains(compass)) {
            inv.addItem(compass);
        }
    }
}

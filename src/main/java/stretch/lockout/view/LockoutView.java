package stretch.lockout.view;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public abstract class LockoutView {
    protected ItemStack makeRegularItem(ItemStack completedItem) {
        ItemMeta itemMeta = completedItem.getItemMeta();

        if (itemMeta != null) {
            itemMeta.removeEnchant(Enchantment.BINDING_CURSE);
        }
        completedItem.setItemMeta(itemMeta);
        return completedItem;
    }

    protected ItemStack makeSpecialItem(ItemStack completedItem) {
        ItemMeta itemMeta = completedItem.getItemMeta();

        if (itemMeta != null) {
            itemMeta.addEnchant(Enchantment.BINDING_CURSE, 1, true);
            itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            itemMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        }
        completedItem.setItemMeta(itemMeta);

        return completedItem;
    }
}

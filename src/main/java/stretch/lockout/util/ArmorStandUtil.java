package stretch.lockout.util;

import org.bukkit.inventory.ItemStack;

// Simulates what would happen with a player interacting with an armor stand to
// determine the outcome when PlayerArmorStandManipulateEvent is called
public class ArmorStandUtil {

    enum ArmorIndex {
        BOOT_INDEX, LEGGING_INDEX, CHEST_INDEX, HELMET_INDEX
    }
    public static ItemStack[] addArmorStandItem(ItemStack[] armorContents, ItemStack playerItem) {

        ItemStack[] copyArmorContents = armorContents.clone();

        switch (playerItem.getType()) {
            case DIAMOND_BOOTS, IRON_BOOTS, GOLDEN_BOOTS,
                    CHAINMAIL_BOOTS, LEATHER_BOOTS, NETHERITE_BOOTS -> copyArmorContents[ArmorIndex.BOOT_INDEX.ordinal()] = playerItem;
            case DIAMOND_LEGGINGS, IRON_LEGGINGS, GOLDEN_LEGGINGS,
                    CHAINMAIL_LEGGINGS, LEATHER_LEGGINGS, NETHERITE_LEGGINGS -> copyArmorContents[ArmorIndex.LEGGING_INDEX.ordinal()] = playerItem;
            case DIAMOND_CHESTPLATE, IRON_CHESTPLATE, GOLDEN_CHESTPLATE,
                    CHAINMAIL_CHESTPLATE, LEATHER_CHESTPLATE, NETHERITE_CHESTPLATE -> copyArmorContents[ArmorIndex.CHEST_INDEX.ordinal()] = playerItem;
            case DIAMOND_HELMET, IRON_HELMET, GOLDEN_HELMET,
                    CHAINMAIL_HELMET, LEATHER_HELMET, NETHERITE_HELMET -> copyArmorContents[ArmorIndex.HELMET_INDEX.ordinal()] = playerItem;
        }

        return copyArmorContents;
    }
}

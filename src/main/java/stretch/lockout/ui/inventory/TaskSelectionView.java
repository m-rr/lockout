package stretch.lockout.ui.inventory;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class TaskSelectionView extends LockoutView implements InventoryHolder {

    private final Inventory inventory;
    private final ItemStack entryItem = new ItemStack(Material.PAPER);

    public TaskSelectionView() {
        inventory = Bukkit.createInventory(this, 18, "Select Tasks");
    }

    private ItemStack entryItemFactory() {
        return new ItemStack(entryItem);
    }

    private ItemStack selectedItemFactory() {
        ItemStack item = entryItemFactory();

        return makeSpecialItem(item);
    }

    public void addTaskListEntry(String taskListName) {
        ItemStack guiItemStack = entryItemFactory();
        ItemMeta itemMeta = guiItemStack.getItemMeta();
        itemMeta.setDisplayName(taskListName);
        guiItemStack.setItemMeta(itemMeta);

        inventory.addItem(guiItemStack);
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}

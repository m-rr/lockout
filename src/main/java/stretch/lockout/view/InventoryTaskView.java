package stretch.lockout.view;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import stretch.lockout.reward.RewardComponent;
import stretch.lockout.task.TaskComponent;

import java.util.ArrayList;
import java.util.List;

public class InventoryTaskView extends LockoutView implements InventoryHolder {
    private Inventory inventory;
    private final ItemStack defaultItem;
    private final ItemStack completedItem;
    private int size = 9;
    private int guiEntries = 0;

    public InventoryTaskView() {
        this.inventory = Bukkit.createInventory(this, size, "Lockout");
        this.defaultItem = new ItemStack(Material.BARRIER);
        this.completedItem = new ItemStack(Material.GRAY_STAINED_GLASS);

    }
    @Override
    public Inventory getInventory() {
        return this.inventory;
    }

    public ItemStack completedItemFactory() {
        ItemStack completedItem = new ItemStack(this.completedItem);
        return makeSpecialItem(completedItem);
    }

    public ItemStack defaultItemFactory() {
        return new ItemStack(defaultItem);
    }

    // Adds item to gui inventory based on task
    private void addTaskEntry(TaskComponent task, ItemStack itemStack) {

        ItemStack guiItemStack;
        ItemMeta itemMeta;

        List<String> loreList = new ArrayList<>();
        if (task.getValue() != 0) {
            loreList.add(ChatColor.GRAY + "Value: " + ChatColor.GOLD + task.getValue());
        }

        if (task.hasReward()) {
            RewardComponent reward = task.getReward();
            loreList.add(ChatColor.GRAY + "Reward: "
                    + ChatColor.LIGHT_PURPLE + reward.getDescription());

            ItemMeta tmpItemMeta = itemStack.getItemMeta();

            tmpItemMeta.addEnchant(Enchantment.BINDING_CURSE, 1, true);
            tmpItemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            itemStack.setItemMeta(tmpItemMeta);
        }

        if (task.isCompleted()) {
            guiItemStack = completedItemFactory();
            loreList.add(ChatColor.DARK_AQUA + "Completed by team "
                    + ChatColor.GREEN + task.getScoredPlayer().getTeam().getName());

            itemMeta = guiItemStack.getItemMeta();
            itemMeta.setDisplayName(ChatColor.RED + task.getDescription());
        }
        else {
            guiItemStack = itemStack;
            itemMeta = itemStack.getItemMeta();
            itemMeta.setDisplayName(ChatColor.BLUE + task.getDescription());
        }

        itemMeta.setLore(loreList);

        guiItemStack.setItemMeta(itemMeta);

        if (++guiEntries > size) {
            ItemStack[] contents = inventory.getContents();
            size += 9;
            inventory = Bukkit.createInventory(this, size, "Lockout");
            inventory.setContents(contents);
        }
        inventory.addItem(guiItemStack);
    }

    // Uses guiItemStack of task by default
    public void addTaskEntry(TaskComponent task) {
        ItemStack itemStack = task.getGuiItemStack();
        if (!task.hasGuiItemStack()) {
            itemStack = defaultItemFactory();
        }

        addTaskEntry(task, itemStack);
    }

}

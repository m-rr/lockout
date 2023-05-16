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
import stretch.lockout.task.TaskInvisible;

import java.util.ArrayList;
import java.util.List;

public class InventoryTaskView extends LockoutView implements InventoryHolder {
    private Inventory inventory;
    private final ItemStack defaultItem;
    private final ItemStack completedItem;
    private int size = 9;
    private int guiEntries = 0;
    private final boolean showRewards;

    public InventoryTaskView(boolean showRewards) {
        this.showRewards = showRewards;

        this.inventory = Bukkit.createInventory(this, size, "Lockout");
        this.defaultItem = new ItemStack(Material.GRAY_STAINED_GLASS);
        this.completedItem = new ItemStack(Material.BARRIER);

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

    private List<String> getTaskTitle(String taskDescription, int maxWidth) {
        String[] split = taskDescription.split(" ");
        List<String> result = new ArrayList<>();
        int width = 0;
        StringBuilder line = new StringBuilder();
        for (int i = 0; i < split.length; i++) {
            width += split[i].length();
            line.append(split[i]).append(" ");
            if (width >= maxWidth) {
                result.add(line.toString());
                width = 0;
                line = new StringBuilder();
            }
        }
        if (!line.isEmpty()) {
            result.add(line.toString());
        }
        return result;
    }

    // Adds item to gui inventory based on task
    private void addTaskEntry(TaskComponent task, ItemStack itemStack) {

        ItemStack guiItemStack;
        ItemMeta itemMeta;
        final int maxStringLength = 25;
        List<String> loreList = new ArrayList<>();
        List<String> description = getTaskTitle(task.getDescription(), maxStringLength);
        if (description.size() > 1) {
            description.subList(1, description.size()).forEach(line -> loreList.add(ChatColor.BLUE + line));
        }

        if (task.getValue() != 0) {
            loreList.add(ChatColor.GRAY + "Value: " + ChatColor.GOLD + task.getValue());
        }

        if (task.hasReward() && showRewards) {
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
            itemMeta.setDisplayName(ChatColor.RED + description.get(0));
        }
        else {
            guiItemStack = itemStack;
            itemMeta = itemStack.getItemMeta();
            itemMeta.setDisplayName(ChatColor.BLUE + description.get(0));
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
        if (task instanceof TaskInvisible) {
            return;
        }
        ItemStack itemStack = task.getGuiItemStack();
        if (!task.hasGuiItemStack()) {
            itemStack = defaultItemFactory();
        }

        addTaskEntry(task, itemStack);
    }

}

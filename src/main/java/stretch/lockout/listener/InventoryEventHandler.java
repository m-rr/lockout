package stretch.lockout.listener;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.inventory.FurnaceExtractEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import stretch.lockout.game.RaceGameContext;
import stretch.lockout.task.file.TaskList;
import stretch.lockout.view.InventoryTaskView;
import stretch.lockout.view.TaskSelectionView;

public record InventoryEventHandler(RaceGameContext taskRaceContext) implements Listener {

    public InventoryEventHandler(RaceGameContext taskRaceContext) {
        this.taskRaceContext = taskRaceContext;
        Bukkit.getPluginManager().registerEvents(this, taskRaceContext.getPlugin());
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent clickEvent) {
        if (clickEvent.isCancelled() || clickEvent.getClickedInventory() == null || clickEvent.getCurrentItem() == null) {
            return;
        }

        if (clickEvent.getClickedInventory().getHolder() instanceof InventoryTaskView) {
            clickEvent.setCancelled(true);
            return;
        }

        var player = (Player) clickEvent.getWhoClicked();

        if (clickEvent.getClickedInventory().getHolder() instanceof TaskSelectionView) {
            ItemStack clickedItem = clickEvent.getCurrentItem();
            if (clickedItem.getType() == Material.PAPER) {
                player.closeInventory();
                if (taskRaceContext.getTaskManager().isTasksLoaded()) {
                    player.sendMessage(ChatColor.RED + "Tasks already loaded");
                    return;
                }
                taskRaceContext.loadTaskList(player, new TaskList(clickedItem.getItemMeta().getDisplayName()));
            }
            clickEvent.setCancelled(true);
            return;
        }

        taskRaceContext.checkTask(player, clickEvent);
    }

    @EventHandler
    public void onInventoryMoveItem(InventoryMoveItemEvent moveItemEvent) {
        if (moveItemEvent.isCancelled()) {
            return;
        }
        Inventory initiator = moveItemEvent.getInitiator();
        if (initiator.getHolder() instanceof PlayerInventory playerInventory) {
            var player = (Player) playerInventory.getHolder();
            taskRaceContext.checkTask(player, moveItemEvent);
        }
    }

    @EventHandler
    public void onEnchantItem(EnchantItemEvent enchantItemEvent) {
        if (enchantItemEvent.isCancelled()) {
            return;
        }

        var player = enchantItemEvent.getEnchanter();
        taskRaceContext.checkTask(player, enchantItemEvent);
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent dragEvent) {
        if (dragEvent.isCancelled()) {
            return;
        }

        if (dragEvent.getInventory().getHolder() instanceof InventoryTaskView || dragEvent.getInventory().getHolder() instanceof TaskSelectionView) {
            dragEvent.setCancelled(true);
        }
    }

    @EventHandler
    public void onFurnaceExtract(FurnaceExtractEvent furnaceExtractEvent) {
        var player = furnaceExtractEvent.getPlayer();
        taskRaceContext.checkTask(player, furnaceExtractEvent);
    }
}

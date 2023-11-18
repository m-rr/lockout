package stretch.lockout.listener;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import stretch.lockout.game.RaceGameContext;
import stretch.lockout.team.TeamManager;
import stretch.lockout.view.InventoryTaskView;
import stretch.lockout.view.TaskSelectionView;
import stretch.lockout.view.TeamSelectionView;

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
                if (taskRaceContext.getCurrentTaskCollection().isTasksLoaded()) {
                    player.sendMessage(ChatColor.RED + "Tasks already loaded");
                    return;
                }
                taskRaceContext.getLuaEnvironment().loadFile(player, clickedItem.getItemMeta().getDisplayName());
            }
            clickEvent.setCancelled(true);
            return;
        }

        if (clickEvent.getClickedInventory().getHolder() instanceof TeamSelectionView) {
            ItemStack clickedItem = clickEvent.getCurrentItem();
            TeamManager teamManager = taskRaceContext.getTeamManager();
            String teamName = clickedItem.getItemMeta().getDisplayName();
            if (!teamManager.isPlayerOnTeam(player)) {
                teamManager.addPlayerToTeam(player, teamName);
            }
            else if (!teamManager.getMappedPlayerStats().get(player).getTeam().getName()
                    .equals(teamName)) {

                teamManager.getTeamByName(teamManager.getMappedPlayerStats().get(player).getTeam().getName())
                        .removePlayer(player);

                teamManager.addPlayerToTeam(player, teamName);
            }

            clickEvent.setCancelled(true);
            return;
        }

        taskRaceContext.checkTask(player, clickEvent);
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent closeEvent) {
        var player = (Player) closeEvent.getPlayer();
        taskRaceContext.checkTask(player, closeEvent);
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

        InventoryHolder holder = dragEvent.getInventory().getHolder();

        if (holder instanceof InventoryTaskView ||
                holder instanceof TaskSelectionView ||
                holder instanceof TeamSelectionView) {
            dragEvent.setCancelled(true);
        }
    }

    @EventHandler
    public void onFurnaceExtract(FurnaceExtractEvent furnaceExtractEvent) {
        var player = furnaceExtractEvent.getPlayer();
        taskRaceContext.checkTask(player, furnaceExtractEvent);
    }

    @EventHandler
    public void onCraftItem(CraftItemEvent craftItemEvent) {
        if (craftItemEvent.isCancelled()) {
            return;
        }

        HumanEntity humanEntity =  craftItemEvent.getWhoClicked();
        if (humanEntity instanceof Player player) {
            taskRaceContext.checkTask(player, craftItemEvent);
        }
    }
}

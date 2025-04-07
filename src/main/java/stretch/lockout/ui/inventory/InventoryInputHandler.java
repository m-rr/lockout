package stretch.lockout.ui.inventory;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import stretch.lockout.game.LockoutContext;
import stretch.lockout.team.TeamManager;
import stretch.lockout.util.SpigotSafeCall;

public record InventoryInputHandler(LockoutContext lockout) implements Listener {

    public InventoryInputHandler(LockoutContext lockout) {
        this.lockout = lockout;
        Bukkit.getPluginManager().registerEvents(this, lockout.getPlugin());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onInventoryClick(InventoryClickEvent clickEvent) {
        if (clickEvent.isCancelled() || clickEvent.getClickedInventory() == null || clickEvent.getCurrentItem() == null) {
            return;
        }

        Player player = (Player) clickEvent.getWhoClicked();

        if (clickEvent.getClickedInventory().getHolder() instanceof InventoryTaskView) {
            clickEvent.setCancelled(true);
            return;
        }


        if (clickEvent.getClickedInventory().getHolder() instanceof TaskSelectionView) {
            ItemStack clickedItem = clickEvent.getCurrentItem();
            if (clickedItem.getType() == Material.PAPER) {
                player.closeInventory();
                if (lockout.getCurrentTaskCollection().isTasksLoaded()) {
                    player.sendMessage(ChatColor.RED + "Tasks already loaded");
                    return;
                }

                String boardName = SpigotSafeCall.callUnsafeSpigotMethod(() ->
                        clickedItem.getItemMeta().getDisplayName(),
                        "");

                lockout.getBoardManager().loadBoard(boardName);
            }
            clickEvent.setCancelled(true);
            return;
        }

        if (clickEvent.getClickedInventory().getHolder() instanceof TeamSelectionView) {
            ItemStack clickedItem = clickEvent.getCurrentItem();
            TeamManager teamManager = lockout.getTeamManager();
            String teamName = SpigotSafeCall.callUnsafeSpigotMethod(() ->
                    clickedItem.getItemMeta().getDisplayName(),
                    "TEAM");

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
        }

    }

    @EventHandler
    public void onInventoryMoveItem(InventoryMoveItemEvent moveItemEvent) {
        if (moveItemEvent.isCancelled()) {
            return;
        }
        Inventory initiator = moveItemEvent.getInitiator();

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

}

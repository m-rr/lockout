package stretch.lockout.listener;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.raid.RaidTriggerEvent;
import stretch.lockout.game.GameRule;
import stretch.lockout.game.GameState;
import stretch.lockout.game.RaceGameContext;
import stretch.lockout.team.PlayerStat;
import stretch.lockout.team.TeamManager;

import java.util.Map;
import java.util.UUID;

public class PlayerEventHandler implements Listener {
    private RaceGameContext taskRaceContext;
    private final int INVULNERABLE_TIME = 140;
    public PlayerEventHandler(RaceGameContext taskRaceContext) {
        this.taskRaceContext = taskRaceContext;
        Bukkit.getPluginManager().registerEvents(this, taskRaceContext.getPlugin());
    }

    @EventHandler
    public void onArmorStandManipulate(PlayerArmorStandManipulateEvent armorStandManipulateEvent) {
        if (armorStandManipulateEvent.isCancelled()) {
            return;
        }
        var player = armorStandManipulateEvent.getPlayer();
        taskRaceContext.checkTask(player, armorStandManipulateEvent);
    }

    @EventHandler
    public void onBucketEntityPickup(PlayerBucketEntityEvent bucketEntityEvent) {
        if (bucketEntityEvent.isCancelled()) {
            return;
        }

        var player = bucketEntityEvent.getPlayer();
        taskRaceContext.checkTask(player, bucketEntityEvent);
    }

    @EventHandler
    public void onBucketFill(PlayerBucketFillEvent bucketFillEvent) {
        if (bucketFillEvent.isCancelled()) {
            return;
        }

        var player = bucketFillEvent.getPlayer();
        taskRaceContext.checkTask(player, bucketFillEvent);
    }

    @EventHandler
    public void onDropItem(PlayerDropItemEvent dropItemEvent) {
        if (dropItemEvent.isCancelled()) {
            return;
        }

        if (taskRaceContext.getGameState() == GameState.STARTING && !taskRaceContext.gameRules().contains(GameRule.COUNTDOWN_MOVE)) {
            dropItemEvent.setCancelled(true);
            return;
        }

        var player = dropItemEvent.getPlayer();
        taskRaceContext.checkTask(player, dropItemEvent);
    }

    @EventHandler
    public void onEditBook(PlayerEditBookEvent editBookEvent) {
        if (editBookEvent.isCancelled()) {
            return;
        }

        var player = editBookEvent.getPlayer();
        taskRaceContext.checkTask(player, editBookEvent);

    }

    @EventHandler
    public void onEggThrow(PlayerEggThrowEvent eggThrowEvent) {
        var player = eggThrowEvent.getPlayer();
        taskRaceContext.checkTask(player, eggThrowEvent);
    }

    @EventHandler
    public void onFishEvent(PlayerFishEvent fishEvent) {
        if (fishEvent.isCancelled()) {
            return;
        }

        var player = fishEvent.getPlayer();
        taskRaceContext.checkTask(player, fishEvent);
    }

    @EventHandler
    public void onHarvestBlock(PlayerHarvestBlockEvent harvestBlockEvent) {
        if (harvestBlockEvent.isCancelled()) {
            return;
        }

        var player = harvestBlockEvent.getPlayer();
        taskRaceContext.checkTask(player, harvestBlockEvent);
    }

    @EventHandler
    public void onInteractEntity(PlayerInteractEntityEvent interactEntityEvent) {
        if (interactEntityEvent.isCancelled()) {
            return;
        }

        var player = interactEntityEvent.getPlayer();
        taskRaceContext.checkTask(player, interactEntityEvent);
    }

    @EventHandler
    public void onItemBreak(PlayerItemBreakEvent itemBreakEvent) {
        var player = itemBreakEvent.getPlayer();
        taskRaceContext.checkTask(player, itemBreakEvent);
    }

    @EventHandler
    public void onItemConsume(PlayerItemConsumeEvent itemConsumeEvent) {
        if (itemConsumeEvent.isCancelled()) {
            return;
        }

        var player = itemConsumeEvent.getPlayer();
        taskRaceContext.checkTask(player, itemConsumeEvent);
    }

    @EventHandler
    public void onRaidTrigger(RaidTriggerEvent raidTriggerEvent) {
        if (raidTriggerEvent.isCancelled()) {
            return;
        }

        var player = raidTriggerEvent.getPlayer();
        taskRaceContext.checkTask(player, raidTriggerEvent);
    }

    @EventHandler
    public void onLevelChange(PlayerLevelChangeEvent levelChangeEvent) {
        var player = levelChangeEvent.getPlayer();
        taskRaceContext.checkTask(player, levelChangeEvent);
    }

    @EventHandler
    public void onArrowPickup(PlayerPickupArrowEvent pickupArrowEvent) {
        if (pickupArrowEvent.isCancelled()) {
            return;
        }

        var player = pickupArrowEvent.getPlayer();
        taskRaceContext.checkTask(player, pickupArrowEvent);
    }

    @EventHandler
    public void onPortal(PlayerPortalEvent portalEvent) {
        if (portalEvent.isCancelled()) {
            return;
        }

        var player = portalEvent.getPlayer();
        taskRaceContext.checkTask(player, portalEvent);
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent respawnEvent) {
        var player = respawnEvent.getPlayer();
        if (taskRaceContext.getTeamManager().getMappedPlayerStats().containsKey(player)) {
            taskRaceContext.gracePeriod(player);
            player.getInventory().addItem(taskRaceContext.getGuiCompass());
        }
        taskRaceContext.checkTask(player, respawnEvent);
    }

    @EventHandler
    public void onRiptide(PlayerRiptideEvent riptideEvent) {
        var player = riptideEvent.getPlayer();
        taskRaceContext.checkTask(player, riptideEvent);
    }

    @EventHandler
    public void onShearEntity(PlayerShearEntityEvent shearEntityEvent) {
        if (shearEntityEvent.isCancelled()) {
            return;
        }

        var player = shearEntityEvent.getPlayer();
        taskRaceContext.checkTask(player, shearEntityEvent);
    }

    @EventHandler
    public void onUnleashEntity(PlayerUnleashEntityEvent unleashEntityEvent) {
        if (unleashEntityEvent.isCancelled()) {
            return;
        }

        var player = unleashEntityEvent.getPlayer();
        taskRaceContext.checkTask(player, unleashEntityEvent);

    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent moveEvent) {
        if (moveEvent.isCancelled()) {
            return;
        }
        if (taskRaceContext.getGameState() == GameState.STARTING && !taskRaceContext.gameRules().contains(GameRule.COUNTDOWN_MOVE)) {
            moveEvent.setCancelled(true);
            return;
        }

        var player = moveEvent.getPlayer();
        taskRaceContext.checkTask(player, moveEvent);
    }

    @EventHandler
    public void onHungerChange(FoodLevelChangeEvent foodLevelChangeEvent) {
        HumanEntity player = foodLevelChangeEvent.getEntity();
        if (player.isInvulnerable()) {
            foodLevelChangeEvent.setCancelled(true);
        }
    }

    @EventHandler
    public void onFire(EntityCombustEvent combustEvent) {
        Entity entity = combustEvent.getEntity();
        if (entity instanceof HumanEntity player && player.isInvulnerable()) {
            combustEvent.setCancelled(true);
        }
    }

    // Contains logic for compass in hand
    @EventHandler
    public void onPlayerInteractEvent(PlayerInteractEvent interactEvent) {
        var player = interactEvent.getPlayer();

        taskRaceContext.checkTask(player, interactEvent);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent joinEvent) {

        var player = joinEvent.getPlayer();
        TeamManager teamManager = taskRaceContext.getTeamManager();

        // Reattach pointer to appropriate PlayerStat
        if (teamManager.isPlayerOnTeam(player)) {
            Map<UUID, PlayerStat> mappedPlayerStats = teamManager.getUUIDMappedPlayerStats();

            if (mappedPlayerStats.containsKey(player.getUniqueId())) {
                player.setScoreboard(taskRaceContext.getScoreboardManager().getBoard());
                var playerStat = mappedPlayerStats.get(player.getUniqueId());
                playerStat.setPlayer(player);
                taskRaceContext.getPlayerTracker().setPlayer(playerStat);
            }
        }
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent quitEvent) {
        // Game should end if all players are disconnected
        // This is when the last player disconnects, and they may rejoin, so we wait a minute first.

        if (Bukkit.getOnlinePlayers().size() < 2) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(taskRaceContext.getPlugin(), () -> {
                if (Bukkit.getOnlinePlayers().size() < 1) {
                    taskRaceContext.setGameState(GameState.END);
                }
            }, 1200);
        }
    }

}

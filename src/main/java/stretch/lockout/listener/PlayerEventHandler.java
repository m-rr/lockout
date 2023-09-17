package stretch.lockout.listener;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.raid.RaidTriggerEvent;
import stretch.lockout.game.GameRule;
import stretch.lockout.game.state.GameState;
import stretch.lockout.game.RaceGameContext;
import stretch.lockout.team.PlayerStat;
import stretch.lockout.team.TeamManager;

import java.util.Map;
import java.util.UUID;

public class PlayerEventHandler implements Listener {
    private final RaceGameContext lockout;
    private final int INVULNERABLE_TIME = 140;
    public PlayerEventHandler(RaceGameContext taskRaceContext) {
        this.lockout = taskRaceContext;
        Bukkit.getPluginManager().registerEvents(this, taskRaceContext.getPlugin());
    }

    @EventHandler
    public void onArmorStandManipulate(PlayerArmorStandManipulateEvent armorStandManipulateEvent) {
        if (armorStandManipulateEvent.isCancelled()) {
            return;
        }
        var player = armorStandManipulateEvent.getPlayer();
        lockout.checkTask(player, armorStandManipulateEvent);
    }

    @EventHandler
    public void onBucketEntityPickup(PlayerBucketEntityEvent bucketEntityEvent) {
        if (bucketEntityEvent.isCancelled()) {
            return;
        }

        var player = bucketEntityEvent.getPlayer();
        lockout.checkTask(player, bucketEntityEvent);
    }

    @EventHandler
    public void onBucketFill(PlayerBucketFillEvent bucketFillEvent) {
        if (bucketFillEvent.isCancelled()) {
            return;
        }

        var player = bucketFillEvent.getPlayer();
        lockout.checkTask(player, bucketFillEvent);
    }

    @EventHandler
    public void onDropItem(PlayerDropItemEvent dropItemEvent) {
        if (dropItemEvent.isCancelled()) {
            return;
        }

        if (lockout.getGameStateHandler().getGameState() == GameState.STARTING && !lockout.gameRules().contains(GameRule.COUNTDOWN_MOVE)) {
            dropItemEvent.setCancelled(true);
            return;
        }

        var player = dropItemEvent.getPlayer();
        lockout.checkTask(player, dropItemEvent);
    }

    @EventHandler
    public void onEditBook(PlayerEditBookEvent editBookEvent) {
        if (editBookEvent.isCancelled()) {
            return;
        }

        var player = editBookEvent.getPlayer();
        lockout.checkTask(player, editBookEvent);

    }

    @EventHandler
    public void onEggThrow(PlayerEggThrowEvent eggThrowEvent) {
        var player = eggThrowEvent.getPlayer();
        lockout.checkTask(player, eggThrowEvent);
    }

    @EventHandler
    public void onFishEvent(PlayerFishEvent fishEvent) {
        if (fishEvent.isCancelled()) {
            return;
        }

        var player = fishEvent.getPlayer();
        lockout.checkTask(player, fishEvent);
    }

    @EventHandler
    public void onHarvestBlock(PlayerHarvestBlockEvent harvestBlockEvent) {
        if (harvestBlockEvent.isCancelled()) {
            return;
        }

        var player = harvestBlockEvent.getPlayer();
        lockout.checkTask(player, harvestBlockEvent);
    }

    @EventHandler
    public void onInteractEntity(PlayerInteractEntityEvent interactEntityEvent) {
        if (interactEntityEvent.isCancelled()) {
            return;
        }

        var player = interactEntityEvent.getPlayer();
        lockout.checkTask(player, interactEntityEvent);
    }

    @EventHandler
    public void onItemBreak(PlayerItemBreakEvent itemBreakEvent) {
        var player = itemBreakEvent.getPlayer();
        lockout.checkTask(player, itemBreakEvent);
    }

    @EventHandler
    public void onItemConsume(PlayerItemConsumeEvent itemConsumeEvent) {
        if (itemConsumeEvent.isCancelled()) {
            return;
        }

        var player = itemConsumeEvent.getPlayer();
        lockout.checkTask(player, itemConsumeEvent);
    }

    @EventHandler
    public void onRaidTrigger(RaidTriggerEvent raidTriggerEvent) {
        if (raidTriggerEvent.isCancelled()) {
            return;
        }

        var player = raidTriggerEvent.getPlayer();
        lockout.checkTask(player, raidTriggerEvent);
    }

    @EventHandler
    public void onLevelChange(PlayerLevelChangeEvent levelChangeEvent) {
        var player = levelChangeEvent.getPlayer();
        lockout.checkTask(player, levelChangeEvent);
    }

    @EventHandler
    public void onArrowPickup(PlayerPickupArrowEvent pickupArrowEvent) {
        if (pickupArrowEvent.isCancelled()) {
            return;
        }

        var player = pickupArrowEvent.getPlayer();
        lockout.checkTask(player, pickupArrowEvent);
    }

    @EventHandler
    public void onPortal(PlayerPortalEvent portalEvent) {
        if (portalEvent.isCancelled()) {
            return;
        }

        var player = portalEvent.getPlayer();
        lockout.checkTask(player, portalEvent);
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent respawnEvent) {
        var player = respawnEvent.getPlayer();
        lockout.checkTask(player, respawnEvent);
    }

    @EventHandler
    public void onRiptide(PlayerRiptideEvent riptideEvent) {
        var player = riptideEvent.getPlayer();
        lockout.checkTask(player, riptideEvent);
    }

    @EventHandler
    public void onShearEntity(PlayerShearEntityEvent shearEntityEvent) {
        if (shearEntityEvent.isCancelled()) {
            return;
        }

        var player = shearEntityEvent.getPlayer();
        lockout.checkTask(player, shearEntityEvent);
    }

    @EventHandler
    public void onUnleashEntity(PlayerUnleashEntityEvent unleashEntityEvent) {
        if (unleashEntityEvent.isCancelled()) {
            return;
        }

        var player = unleashEntityEvent.getPlayer();
        lockout.checkTask(player, unleashEntityEvent);

    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent moveEvent) {
        if (moveEvent.isCancelled()) {
            return;
        }
        if (lockout.getGameStateHandler().getGameState() == GameState.STARTING && !lockout.gameRules().contains(GameRule.COUNTDOWN_MOVE)) {
            moveEvent.setCancelled(true);
            return;
        }

        var player = moveEvent.getPlayer();
        lockout.checkTask(player, moveEvent);
    }

    @EventHandler
    public void onHungerChange(FoodLevelChangeEvent foodLevelChangeEvent) {
        HumanEntity humanEntity = foodLevelChangeEvent.getEntity();
        if (humanEntity.isInvulnerable()) {
            foodLevelChangeEvent.setCancelled(true);
        }
        if (humanEntity instanceof Player player) {
            lockout.checkTask(player, foodLevelChangeEvent);
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

        lockout.checkTask(player, interactEvent);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent joinEvent) {

        var player = joinEvent.getPlayer();
        TeamManager teamManager = lockout.getTeamManager();

        // Reattach pointer to appropriate PlayerStat
        if (teamManager.isPlayerOnTeam(player)) {
            Map<UUID, PlayerStat> mappedPlayerStats = teamManager.getUUIDMappedPlayerStats();

            if (mappedPlayerStats.containsKey(player.getUniqueId())) {
                player.setScoreboard(lockout.getScoreboardManager().getBoard());
                var playerStat = mappedPlayerStats.get(player.getUniqueId());
                playerStat.setPlayer(player);
                lockout.getPlayerTracker().setPlayer(playerStat);
            }
        }

        // Update bossbars
        switch (lockout.getGameStateHandler().getGameState()) {
            case READY -> {
                lockout.getPreGameBar().activate();
            }
            case RUNNING -> {
                if (lockout.gameRules().contains(GameRule.TIMER)) {
                    lockout.getTimer().activate();
                }
            }
            case TIEBREAKER -> {
                if (lockout.gameRules().contains(GameRule.TIE_BREAK)) {
                    lockout.getTieBar().activate();
                }
            }
            default -> {}
        }
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent quitEvent) {
        // Game should end if all players are disconnected
        // This is when the last player disconnects, and they may rejoin, so we wait a minute first.
        if (Bukkit.getOnlinePlayers().size() < 2) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(lockout.getPlugin(), () -> {
                if (Bukkit.getOnlinePlayers().size() < 1) {
                    lockout.getGameStateHandler().setGameState(GameState.END);
                }
            }, 1200);
        }
    }

}

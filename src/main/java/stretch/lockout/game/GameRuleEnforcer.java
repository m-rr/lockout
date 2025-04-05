package stretch.lockout.game;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import stretch.lockout.game.state.GameState;
import stretch.lockout.util.LockoutLogger;

public class GameRuleEnforcer implements Listener {
    private final LockoutContext lockout;
    public GameRuleEnforcer(final LockoutContext lockout) {
        LockoutLogger.debugLog("Game rule enforcer initialized.");
        this.lockout = lockout;
        Bukkit.getPluginManager().registerEvents(this, lockout.getPlugin());
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent blockBreakEvent) {

        if (lockout.getGameStateHandler().getGameState() == GameState.STARTING
                && !lockout.settings().hasRule(LockoutGameRule.COUNTDOWN_MOVE)) {
            blockBreakEvent.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent blockPlaceEvent) {
        if (lockout.getGameStateHandler().getGameState() == GameState.STARTING
                && !lockout.settings().hasRule(LockoutGameRule.COUNTDOWN_MOVE)) {
            blockPlaceEvent.setCancelled(true);
        }
    }

    @EventHandler
    public void onDropItem(PlayerDropItemEvent dropItemEvent) {
        if (dropItemEvent.isCancelled()) {
            return;
        }

        if (lockout.getGameStateHandler().getGameState() == GameState.STARTING
                && !lockout.settings().hasRule(LockoutGameRule.COUNTDOWN_MOVE)) {
            dropItemEvent.setCancelled(true);
        }

    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent moveEvent) {
        if (moveEvent.isCancelled()) {
            return;
        }
        if (lockout.getGameStateHandler().getGameState() == GameState.STARTING
                && !lockout.settings().hasRule(LockoutGameRule.COUNTDOWN_MOVE)) {
            moveEvent.setCancelled(true);
        }

    }

    @EventHandler
    public void onPlayerDamage(EntityDamageEvent entityDamageEvent) {
        if (entityDamageEvent.isCancelled()) {
            return;
        }

        if (lockout.getGameStateHandler().getGameState() == GameState.STARTING
            && !lockout.settings().hasRule(LockoutGameRule.COUNTDOWN_MOVE)) {
            entityDamageEvent.setCancelled(true);
        }
    }

    @EventHandler
    public void onHungerChange(FoodLevelChangeEvent foodLevelChangeEvent) {
        HumanEntity humanEntity = foodLevelChangeEvent.getEntity();
        if (humanEntity.isInvulnerable()) {
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
}

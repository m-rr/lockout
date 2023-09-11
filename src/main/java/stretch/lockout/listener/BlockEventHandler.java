package stretch.lockout.listener;

import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.CauldronLevelChangeEvent;
import stretch.lockout.game.GameRule;
import stretch.lockout.game.GameState;
import stretch.lockout.game.RaceGameContext;

public record BlockEventHandler(RaceGameContext taskRaceContext) implements Listener {
    public BlockEventHandler(RaceGameContext taskRaceContext) {
        this.taskRaceContext = taskRaceContext;
        Bukkit.getPluginManager().registerEvents(this, taskRaceContext.getPlugin());
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent blockBreakEvent) {
        if (blockBreakEvent.isCancelled()) {
            return;
        }

        if (taskRaceContext.getGameState() == GameState.STARTING && !taskRaceContext.gameRules().contains(GameRule.COUNTDOWN_MOVE)) {
            blockBreakEvent.setCancelled(true);
            return;
        }

        var player = blockBreakEvent.getPlayer();
        taskRaceContext.checkTask(player, blockBreakEvent);
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent placeEvent) {
        if (placeEvent.isCancelled()) {
            return;
        }

        var player = placeEvent.getPlayer();
        taskRaceContext.checkTask(player, placeEvent);
    }

    @EventHandler
    public void onBlockIgnite(BlockIgniteEvent igniteEvent) {
        if (igniteEvent.isCancelled()) {
            return;
        }

        var player = igniteEvent.getPlayer();
        if (player == null) {
            return;
        }
        taskRaceContext.checkTask(player, igniteEvent);
    }

    @EventHandler
    public void onCauldronLevelChange(CauldronLevelChangeEvent cauldronLevelChangeEvent) {
        if (cauldronLevelChangeEvent.isCancelled()) {
            return;
        }

        var entity = cauldronLevelChangeEvent.getEntity();
        if (entity instanceof Player player) {
            taskRaceContext.checkTask(player, cauldronLevelChangeEvent);
        }
    }
}

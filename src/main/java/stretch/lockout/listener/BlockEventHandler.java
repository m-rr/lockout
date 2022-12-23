package stretch.lockout.listener;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPlaceEvent;
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
}

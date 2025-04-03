package stretch.lockout.event;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import stretch.lockout.task.TaskComponent;
import stretch.lockout.team.player.PlayerStat;

public class TaskCompletedEvent extends Event implements Cancellable {
    final static private HandlerList handlers = new HandlerList();
    final private TaskComponent task;
    private boolean canceled;

    public TaskCompletedEvent(TaskComponent task) {
        this.task = task;
    }

    public PlayerStat getPlayer() {
        return this.task.getScoredPlayer();
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public TaskComponent getTask() {return this.task;}

    @Override
    public boolean isCancelled() {
        return canceled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.canceled = cancel;
    }
}

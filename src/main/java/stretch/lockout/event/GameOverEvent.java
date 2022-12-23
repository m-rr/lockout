package stretch.lockout.event;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import stretch.lockout.team.LockoutTeam;

public class GameOverEvent extends Event implements Cancellable {
    final static private HandlerList handlers = new HandlerList();
    final private LockoutTeam team;
    private boolean cancelled;
    public GameOverEvent(final LockoutTeam winningTeam) {
        this.team = winningTeam;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public LockoutTeam getTeam() {return this.team;}

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }
}

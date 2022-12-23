package stretch.lockout.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class ResetGameEvent extends Event {
    final private static HandlerList handlers = new HandlerList();
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}

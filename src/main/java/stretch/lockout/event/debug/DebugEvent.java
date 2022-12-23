package stretch.lockout.event.debug;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class DebugEvent extends Event {
    final private static HandlerList handlers = new HandlerList();
    public DebugEvent() {

    }
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}

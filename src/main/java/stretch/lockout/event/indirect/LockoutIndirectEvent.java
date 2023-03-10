package stretch.lockout.event.indirect;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class LockoutIndirectEvent extends Event {
    private final static HandlerList handlers = new HandlerList();
    private final double distance;
    private final Player player;
    private final Event chainedEvent;

    public LockoutIndirectEvent(Player player, Event event, double distance) {
        this.player = player;
        this.chainedEvent = event;
        this.distance = distance;
    }

    public Event getChainedEvent() {return chainedEvent;}
    public Player getPlayer() {return player;}
    public double getDistance() {return distance;}

    @Override
    public HandlerList getHandlers() {return handlers;}
    public static HandlerList getHandlerList() {return handlers;}
}

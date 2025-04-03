package stretch.lockout.event.state;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import stretch.lockout.team.player.PlayerStat;

public class PlayerStateChangeEvent extends PlayerEvent {
    private final static HandlerList handlers =  new HandlerList();
    private final PlayerStat playerStat;

    public PlayerStateChangeEvent(PlayerStat playerStat) {
        super(playerStat.getPlayer());
        this.playerStat = playerStat;
    }

    public PlayerStat getPlayerStat() {return playerStat;}
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
    public static HandlerList getHandlerList() {return handlers;}
}

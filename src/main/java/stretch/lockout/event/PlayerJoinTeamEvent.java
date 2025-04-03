/*
* Called after player successfully joins a team.
* */

package stretch.lockout.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import stretch.lockout.team.player.PlayerStat;

public class PlayerJoinTeamEvent extends Event {
    final static private HandlerList handlers = new HandlerList();
    final private PlayerStat playerStat;
    public PlayerJoinTeamEvent(PlayerStat playerStat) {
        this.playerStat = playerStat;
    }

    public PlayerStat getPlayerStat() {return playerStat;}

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
    public static HandlerList getHandlerList() {return handlers;}
}

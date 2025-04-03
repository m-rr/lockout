package stretch.lockout.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import stretch.lockout.reward.RewardComponent;
import stretch.lockout.team.player.PlayerStat;

public class RewardApplyEvent extends Event {
    final private static HandlerList handlers = new HandlerList();
    final private RewardComponent rewardComponent;
    final private PlayerStat playerStat;
    public RewardApplyEvent(PlayerStat playerStat, RewardComponent rewardComponent) {
        this.rewardComponent = rewardComponent;
        this.playerStat = playerStat;
    }
    public PlayerStat getPlayerStat() {return playerStat;}
    public RewardComponent getReward() {return rewardComponent;}

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
    public static HandlerList getHandlerList() {
        return handlers;
    }
}

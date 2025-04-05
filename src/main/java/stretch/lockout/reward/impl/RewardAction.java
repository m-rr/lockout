package stretch.lockout.reward.impl;

import org.bukkit.entity.Player;
import stretch.lockout.reward.api.RewardType;
import stretch.lockout.reward.base.Reward;

import java.util.function.Consumer;

public class RewardAction extends Reward {
    private final Consumer<Player> action;
    public RewardAction(Consumer<Player> consumer, String description) {
        super(description);
        this.action = consumer;
    }

    public RewardAction(Consumer<Player> consumer, RewardType rewardType, String description) {
        super(rewardType, description);
        this.action = consumer;
    }

    @Override
    protected void giveReward(Player player) {
        action.accept(player);
    }
}
